package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.WriteReviewFragmentBinding
import com.google.firebase.auth.FirebaseAuth

class WriteReviewFragment : Fragment() {

    private var _binding: WriteReviewFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel

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

        // Initialize ViewModel
        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]

        // Observe the LiveData for review posting status
        reviewViewModel.postReviewStatus.observe(viewLifecycleOwner) { result ->
            handlePostReviewResult(result)
        }

        // Post review button
        binding.postReviewButton.setOnClickListener {
            postReview()
        }

        // Go back button
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

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Title and description are required.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a review object
        val review: Map<String, Any> = mapOf(
            "userId" to FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            "location" to mapOf(
                "latitude" to latitude.toDouble(),
                "longitude" to longitude.toDouble()
            ),
            "title" to title,
            "status" to status,
            "category" to category,
            "description" to description,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // Post review using ViewModel
        reviewViewModel.postReview(review)
    }



    private fun handlePostReviewResult(result: Boolean?) {
        when (result) {
            true -> {
                Toast.makeText(requireContext(), "Review posted successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                reviewViewModel.resetPostReviewStatus()
            }
            false -> {
                Toast.makeText(requireContext(), "Failed to post review. Please try again.", Toast.LENGTH_SHORT).show()
                reviewViewModel.resetPostReviewStatus()
            }
            null -> {
                // Do nothing, this is the initial state
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
