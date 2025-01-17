package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.WriteReviewFragmentBinding
import org.osmdroid.util.GeoPoint

class WriteReviewFragment : Fragment() {

    private var _binding: WriteReviewFragmentBinding? = null
    private val binding get() = _binding!!
    private var latitude: Float = 0f
    private var longitude: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getFloat("latitude")
            longitude = it.getFloat("longitude")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WriteReviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.postReviewButton.setOnClickListener {
            postReview()
        }

        binding.goBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun postReview() {
        val title = binding.reviewTitle.text.toString()
        val status = when (binding.reviewStatusGroup.checkedRadioButtonId) {
            R.id.statusGood -> "Good"
            R.id.statusBad -> "Bad"
            else -> "Unknown"
        }
        val category = binding.reviewCategory.selectedItem.toString()
        val description = binding.reviewDescription.text.toString()

        // Example logic
        val reviewPoint = GeoPoint(latitude.toDouble(), longitude.toDouble())
        val markerColor = if (status == "Good") "Green" else "Red"

        // Emit a Toast (replace with your logic to save the review)
        Toast.makeText(requireContext(), "Review posted at ($latitude, $longitude)", Toast.LENGTH_SHORT).show()

        // Navigate back to the MapFragment
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
