package com.app.tourism_app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import com.app.tourism_app.database.data.remote.NetworkMonitor
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var googleMap: GoogleMap? = null
    private lateinit var fabMyLocation: FloatingActionButton
    private var fusedClient: FusedLocationProviderClient? = null

    // Permission launcher
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

        // Safe now: fragment is attached
        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 1) Check Google Play Services before using Maps
        val playOk = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS
        if (!playOk) {
            GoogleApiAvailability.getInstance()
                .getErrorDialog(requireActivity(), ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, 9000)
                ?.show()
            Toast.makeText(requireContext(), "Google Play services not available. Map disabled.", Toast.LENGTH_SHORT).show()
            fabMyLocation.isEnabled = false
            return
        }

        // 2) Initialize Maps with latest renderer (more stable on many devices)
        try {
            MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST) { result ->
                Log.d("MapFragment", "MapsInitializer: $result")
            }
        } catch (t: Throwable) {
            Log.e("MapFragment", "MapsInitializer failed: ${t.message}", t)
            Toast.makeText(requireContext(), "Map engine init failed.", Toast.LENGTH_SHORT).show()
            fabMyLocation.isEnabled = false
            return
        }

        // 3) Use a single SupportMapFragment instance; avoid commitNow/allowingStateLoss
        val tag = "map_fragment"
        val fm = childFragmentManager
        val mapFragment = (fm.findFragmentByTag(tag) as? SupportMapFragment) ?: run {
            val m = SupportMapFragment.newInstance()
            fm.beginTransaction()
                .replace(R.id.map_container, m, tag) // regular async commit
                .commit()
            m
        }
        mapFragment.getMapAsync(this)

        fabMyLocation.setOnClickListener {
            if (hasLocationPermission()) {
                moveCameraToUserLocation()
            } else {
                requestLocationPermission()
            }
        }

        // 4) Disable actions when offline; clear markers if offline
        viewLifecycleOwner.lifecycleScope.launch {
            NetworkMonitor.isOnline.collectLatest { online ->
                fabMyLocation.isEnabled = online
                if (!online) {
                    googleMap?.clear()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMarkerClickListener(this)

        // UI settings
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true

        if (hasLocationPermission()) {
            enableMyLocation()
            moveCameraToUserLocation()
        } else {
            requestLocationPermission()
        }

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
            // gated by hasLocationPermission()
        }
    }

    @SuppressLint("MissingPermission") // we check permission inside
    private fun moveCameraToUserLocation() {
        val ctx = context ?: return
        if (!hasLocationPermission()) return

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

    /** Fetch places and add markers. Offline-aware, friendly errors, view-lifecycle scoped. */
    private fun addPlacesMarkers() {
        val ctx = context ?: return

        if (!NetworkMonitor.isOnlineNow(ctx)) {
            Toast.makeText(ctx, "You’re offline. Connect to the internet to load places.", Toast.LENGTH_SHORT).show()
            return
        }

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

                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null) return@withContext
                    addMarkersFromDtos(response.features)
                }
            } catch (t: Throwable) {
                val safe = context ?: return@launch
                val msg = when (t) {
                    is java.net.UnknownHostException -> "No internet connection."
                    is java.net.SocketTimeoutException -> "Network is slow. Try again."
                    is retrofit2.HttpException -> "Server error: ${t.code()}"
                    else -> t.message ?: "Network error."
                }
                Toast.makeText(safe, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarkersFromDtos(list: List<Feature>) {
        val map = googleMap ?: return
        map.clear()

        for (f in list) {
            val p = f.properties
            val coords = f.geometry?.coordinates
            // GeoJSON coordinates are [lon, lat]
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
        // Avoid rapid re-create/teardown cycles holding native locks
        googleMap = null
        fusedClient = null
        super.onDestroyView()
    }
}
