package com.example.reviewr.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.MapFragmentBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var longPressHandler: Handler? = null
    private var longPressRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the Map
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osm_prefs", 0))
        mapView = binding.mapView
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check and request location permissions
        checkLocationPermission()

        // Add Bottom Navigation Actions
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleBottomNavigation(item)
        }

        // Handle long press on the map
        setupLongPressListener()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                showUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show a rationale for requesting location permissions
                Snackbar.make(binding.root, "Location permission is required to show your location on the map.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant") {
                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
            }
            else -> {
                // Directly request the permission
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showUserLocation()
        } else {
            Snackbar.make(binding.root, "Location permission denied.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = GeoPoint(location.latitude, location.longitude)
                mapView.controller.setCenter(userLocation)
                mapView.controller.setZoom(20.0)

                // Add a marker for the user's location
                val marker = Marker(mapView)
                marker.position = userLocation
                marker.title = "You are here"
                mapView.overlays.add(marker)
                mapView.invalidate()
            } else {
                Snackbar.make(binding.root, "Unable to determine your location.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleBottomNavigation(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filterButton -> {
                // Placeholder: Add filter logic here
            }
            R.id.goBackButton -> {
                findNavController().navigateUp()
            }
        }
        return true
    }

    private fun setupLongPressListener() {
        mapView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start the long press handler
                    startLongPressHandler(motionEvent)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Cancel the long press handler
                    cancelLongPressHandler()
                }
            }
            false
        }
    }

    private fun startLongPressHandler(motionEvent: MotionEvent) {
        longPressHandler = Handler(Looper.getMainLooper())
        longPressRunnable = Runnable {
            val geoPoint = mapView.projection.fromPixels(
                motionEvent.x.toInt(),
                motionEvent.y.toInt()
            ) as GeoPoint
            showAddReviewDialog(geoPoint)
        }
        longPressHandler?.postDelayed(longPressRunnable!!, 1000) // 1 second delay
    }

    private fun cancelLongPressHandler() {
        longPressHandler?.removeCallbacks(longPressRunnable!!)
        longPressHandler = null
        longPressRunnable = null
    }

    private fun addMarker(location: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = location
        marker.title = "New Review"
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun showAddReviewDialog(location: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Review")
        builder.setMessage("Would you like to write a review for this location?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Navigate to the "Write A Review" Fragment
            val action = MapFragmentDirections.actionMapFragmentToWriteReviewFragment(
                location.latitude.toFloat(),
                location.longitude.toFloat()
            )
            findNavController().navigate(action)
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
