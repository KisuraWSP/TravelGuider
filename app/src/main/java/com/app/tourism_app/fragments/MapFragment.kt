package com.app.tourism_app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.tourism_app.R
import com.app.tourism_app.activities.PlaceDetailActivity
import com.app.tourism_app.database.data.remote.Feature
import com.app.tourism_app.database.data.remote.NetworkModule
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var googleMap: GoogleMap? = null
    private lateinit var fabMyLocation: FloatingActionButton
    private var fusedClient: FusedLocationProviderClient? = null

    // ActivityResult Launcher for location permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
                moveCameraToUserLocation()
            } else {
                context?.let {
                    Toast.makeText(it, "Location permission required to show your position", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        fabMyLocation = root.findViewById(R.id.fab_my_location)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safe: fragment is attached now
        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Add SupportMapFragment into the container
        val existing = childFragmentManager.findFragmentByTag("map_fragment") as? SupportMapFragment
        val mapFragment = existing ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_container, it, "map_fragment")
                .commitNowAllowingStateLoss()
        }
        mapFragment.getMapAsync(this)

        fabMyLocation.setOnClickListener {
            if (hasLocationPermission()) {
                moveCameraToUserLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMarkerClickListener(this)

        // default UI settings
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true

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
        val ctx = context ?: return false
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = hasLocationPermission()
        } catch (_: SecurityException) {
            // ignore; permission gate above controls this
        }
    }

    @SuppressLint("MissingPermission") // we check permission inside
    private fun moveCameraToUserLocation() {
        val ctx = context ?: return
        val permissionGranted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) return

        try {
            fusedClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val pos = LatLng(it.latitude, it.longitude)
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f))
                    } ?: run {
                        Toast.makeText(ctx, "Unable to obtain last known location", Toast.LENGTH_SHORT).show()
                    }
                }
                ?.addOnFailureListener { exc ->
                    Toast.makeText(ctx, "Could not get location: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (_: SecurityException) {
            Toast.makeText(ctx, "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fetch places from Geoapify and add markers.
     * This is a simple example: it fetches a rectangular area.
     */
    private fun addPlacesMarkers() {
        // tie lifecycle to the VIEW so work is cancelled when the view is destroyed/hidden
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = NetworkModule.provideApiService()
                val response = withContext(Dispatchers.IO) {
                    api.getLocations(
                        categories = "tourism.sights",
                        filter = "rect:79.6617,5.9180,81.9090,9.8341", // Sri Lanka bbox
                        limit = 50,
                        apiKey = "ff8eac3934aa4b74bd1229543e598951"
                    )
                }

                // back to main to touch the map
                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null) return@withContext
                    addMarkersFromDtos(response.features)
                }
            } catch (t: Throwable) {
                context?.let {
                    Toast.makeText(it, "Failed to load places: ${t.message}", Toast.LENGTH_SHORT).show()
                }
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

            val pos = LatLng(lat, lon)
            val title = p?.name ?: p?.formatted ?: "Unknown"
            val snippet = p?.formatted ?: ""
            val tagId = (p?.placeId?.hashCode()?.toLong()) ?: title.hashCode().toLong()

            val marker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
            )
            marker?.tag = tagId
        }
    }

    // When a marker is clicked, open PlaceDetailActivity for that place
    override fun onMarkerClick(marker: Marker): Boolean {
        val ctx = context ?: return true
        val placeId = (marker.tag as? Long) ?: 0L
        val intent = Intent(ctx, PlaceDetailActivity::class.java).apply {
            putExtra("place_id", placeId)
            putExtra("place_title", marker.title)
            putExtra("place_desc", marker.snippet)
        }
        startActivity(intent)
        return true // consume click
    }

    override fun onDestroyView() {
        googleMap = null
        fusedClient = null
        super.onDestroyView()
    }
}
