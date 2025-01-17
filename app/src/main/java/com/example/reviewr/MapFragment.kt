package com.example.reviewr.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.MapFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var reviewViewModel: ReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize map configuration
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osm_prefs", 0))
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        // Initialize the Map
        mapView = binding.mapView
        setupMapView()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermission()

        // Observe reviews from ViewModel
        reviewViewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            displayReviewsOnMap(reviews)
        }

        // Fetch reviews from Firestore
        reviewViewModel.fetchReviews()

        // Add Bottom Navigation Actions
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleBottomNavigation(item)
        }

        // Handle long press on the map
        setupLongPressListener()
    }

    private fun setupMapView() {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        // Set a default center location if no location is available
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194)) // San Francisco placeholder
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                showUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(binding.root, "Location permission is required to show your location on the map.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant") {
                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
            }
            else -> {
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
                Log.d("MapFragment", "User location: ${location.latitude}, ${location.longitude}")
                val userLocation = GeoPoint(location.latitude, location.longitude)
                // Add marker for user location
                val marker = Marker(mapView).apply {
                    position = userLocation
                    title = "You are here"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                // Remove existing "self-location" markers to avoid duplication
                mapView.overlays.removeIf { it is Marker && it.title == "You are here" }
                mapView.overlays.add(marker)
                Log.d("MapFragment", "Marker added: ${marker.title}")
                // Center map on user location
                mapView.controller.setCenter(userLocation)
                mapView.controller.setZoom(20.0)
                mapView.invalidate()
            } else {
                Log.e("MapFragment", "Location is null.")
                Snackbar.make(binding.root, "Unable to determine your location.", Snackbar.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("MapFragment", "Failed to retrieve location: ${exception.message}", exception)
            Snackbar.make(binding.root, "Failed to retrieve your location.", Snackbar.LENGTH_SHORT).show()
        }
    }





    private fun handleBottomNavigation(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filterButton -> {
                // Placeholder for filter functionality
                true
            }
            R.id.goBackButton -> {
                findNavController().navigateUp()
                true
            }
            else -> false
        }
    }

    private fun setupLongPressListener() {
        mapView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    startLongPressHandler(motionEvent)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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

    private fun displayReviewsOnMap(reviews: List<Map<String, Any>>) {
        Log.d("MapFragment", "Displaying reviews on map. Existing overlays: ${mapView.overlays.size}")

        for (review in reviews) {
            val location = review["location"] as? Map<String, Double> ?: continue
            val latitude = location["latitude"] ?: continue
            val longitude = location["longitude"] ?: continue
            val title = review["title"] as? String ?: "No Title"
            val status = review["status"] as? String ?: "Unknown"
            val postId = review["postId"] as? String ?: continue

            // Avoid duplicate markers for the same review
            val existingMarker = mapView.overlays.find {
                it is Marker && it.position.latitude == latitude && it.position.longitude == longitude
            }
            if (existingMarker != null) {
                Log.d("MapFragment", "Review marker already exists: $title at $latitude, $longitude")
                continue
            }

            // Add a new marker
            val marker = Marker(mapView).apply {
                position = GeoPoint(latitude, longitude)
                this.title = title
                icon = when (status) {
                    "Good" -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_green_marker)
                    "Bad" -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_red_marker)
                    else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_default_marker)
                }

                // Add click listener for the review
                setOnMarkerClickListener { _, _ ->
                    showReviewDialog(postId) // Trigger review dialog
                    true // Consume the click event
                }
            }
            mapView.overlays.add(marker)
            Log.d("MapFragment", "Review marker added: $title at $latitude, $longitude")
        }
        Log.d("MapFragment", "Final overlays after adding reviews: ${mapView.overlays.size}")
        mapView.invalidate()
    }


    private fun showAddReviewDialog(location: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Review")
        builder.setMessage("Would you like to write a review for this location?")
        builder.setPositiveButton("Yes") { _, _ ->
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

    private fun showReviewDialog(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Review Options")
        builder.setMessage("What would you like to do?")

        builder.setPositiveButton("View Review") { _, _ ->
            val action = MapFragmentDirections.actionMapFragmentToViewReviewFragment(postId)
            findNavController().navigate(action)
        }

        builder.setNegativeButton("Go Back") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        reviewViewModel.fetchReviews() // Re-fetch reviews to ensure the latest data is displayed
        showUserLocation()
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
