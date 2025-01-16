package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.reviewr.R
import com.example.reviewr.databinding.MapFragmentBinding

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!

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

        // Set the user's location (mock location for now)
        val userLocation = GeoPoint(37.7749, -122.4194) // Placeholder: San Francisco
        mapView.controller.setCenter(userLocation)

        // Add Bottom Navigation Actions
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleBottomNavigation(item)
        }

        // Add Marker on Map Click
        mapView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == android.view.MotionEvent.ACTION_UP) {
                val geoPoint = mapView.projection.fromPixels(
                    motionEvent.x.toInt(),
                    motionEvent.y.toInt()
                ) as GeoPoint
                addMarker(geoPoint)
            }
            false
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

    private fun addMarker(location: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = location
        marker.title = "New Recommendation"
        mapView.overlays.add(marker)
        mapView.invalidate()
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
