package com.app.tourism_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.tourism_app.database.data.remote.NetworkModule
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import com.app.tourism_app.database.data.remote.LocationDto
import android.annotation.SuppressLint
import com.app.tourism_app.database.data.remote.Feature

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var googleMap: GoogleMap? = null
    private lateinit var fabMyLocation: FloatingActionButton
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(requireContext()) }

    // ActivityResult Launcher for location permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
                moveCameraToUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission required to show your position", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        fabMyLocation = root.findViewById(R.id.fab_my_location)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add SupportMapFragment into the container
        val existing = childFragmentManager.findFragmentByTag("map_fragment") as? SupportMapFragment
        val mapFragment = existing ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_container, it, "map_fragment")
                .commitNowAllowingStateLoss()
        }
        mapFragment.getMapAsync(this)

        fabMyLocation.setOnClickListener {
            // Ensure permission and move camera
            if (hasLocationPermission()) {
                moveCameraToUserLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnMarkerClickListener(this)

        // default UI settings
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.isMapToolbarEnabled = true

        // Enable my-location if permission granted
        if (hasLocationPermission()) {
            enableMyLocation()
            moveCameraToUserLocation()
        } else {
            requestLocationPermission()
        }

        // Load places and add markers
        addPlacesMarkers()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            // ignore - permission must be checked before calling
        }
    }

    @SuppressLint("MissingPermission") // safe because we explicitly check permission below
    private fun moveCameraToUserLocation() {
        // explicit runtime check right here so Lint can see it
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            // Not granted — nothing to do (you already request elsewhere)
            return
        }

        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val pos = LatLng(it.latitude, it.longitude)
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f))
                    } ?: run {
                        Toast.makeText(requireContext(), "Unable to obtain last known location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exc ->
                    Toast.makeText(requireContext(), "Could not get location: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (se: SecurityException) {
            // Defensive: this shouldn't happen because we checked permission, but handle it gracefully.
            Toast.makeText(requireContext(), "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fetch places from Geoapify and add markers.
     * This is a simple example: it fetches a rectangular area.
     * You can change categories/filter as required.
     */
    private fun addPlacesMarkers() {
        lifecycleScope.launch {
            try {
                // Do network on IO
                val api = NetworkModule.provideApiService()
                val response = withContext(Dispatchers.IO) {
                    // Example bounding box (you may use a dynamic rect around user)
                    api.getLocations(
                        categories = "tourism.sights",
                        filter = "rect:79.6617,5.9180,81.9090,9.8341", // Sri Lanka bounding box example
                        limit = 50,
                        apiKey = "YOUR_API_KEY_HERE"
                    )
                }

                // Add markers on main thread
                withContext(Dispatchers.Main) {
                    addMarkersFromDtos(response.features)
                }
            } catch (t: Throwable) {
                // fail gracefully
                Toast.makeText(requireContext(), "Failed to load places: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarkersFromDtos(list: List<Feature>) {
        val map = googleMap ?: return
        map.clear()

        for (f in list) {
            val p = f.properties
            val coords = f.geometry?.coordinates
            // GeoJSON: [lon, lat]
            if (coords == null || coords.size < 2) continue
            val lon = coords[0]
            val lat = coords[1]

            val pos = com.google.android.gms.maps.model.LatLng(lat, lon)
            val title = p?.name ?: p?.formatted ?: "Unknown"
            val snippet = p?.formatted ?: "" // Geoapify doesn't provide `description`
            val tagId = (p?.placeId?.hashCode()?.toLong()) ?: title.hashCode().toLong()

            val marker = map.addMarker(
                com.google.android.gms.maps.model.MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
            )
            marker?.tag = tagId
        }
    }


    // When a marker is clicked, open PlaceDetailActivity for that place
    override fun onMarkerClick(marker: Marker): Boolean {
        val placeId = (marker.tag as? Long) ?: 0L
        val intent = Intent(requireContext(), PlaceDetailActivity::class.java).apply {
            putExtra("place_id", placeId)
            putExtra("place_title", marker.title)
            putExtra("place_desc", marker.snippet)
            // if you have an image url in DTO store it in marker.tag map or use a custom markerTag structure
        }
        startActivity(intent)
        return true // consume click
    }

    override fun onDestroyView() {
        googleMap = null
        super.onDestroyView()
    }
}
