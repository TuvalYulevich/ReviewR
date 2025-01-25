package com.example.reviewr.Reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

    private var reviewImageUrl: String? = null // URL of the uploaded image

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            reviewViewModel.uploadReviewImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getFloat("latitude")
            longitude = it.getFloat("longitude")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        // Upload image button
        binding.uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        reviewViewModel.imageUploadStatus.observe(viewLifecycleOwner) { (success, imageUrl) ->
            if (success && imageUrl != null) {
                reviewImageUrl = imageUrl
                binding.reviewImageView.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.reviewImageView)
                Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
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


    // Post review function interface
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
            "imageUrl" to (reviewImageUrl ?: ""), // Include image URL if available
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        // Post review using ViewModel
        reviewViewModel.postReview(review)
    }

    // Check is the review was posted to Firebase
    private fun handlePostReviewResult(result: Boolean?) {
        when (result) {
            true -> {
                Toast.makeText(requireContext(), "Review posted successfully!", Toast.LENGTH_SHORT).show()
                // Reset image and variables
                reviewImageUrl = null
                binding.reviewImageView.visibility = View.GONE
                binding.reviewImageView.setImageDrawable(null) // Clear the ImageView
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
