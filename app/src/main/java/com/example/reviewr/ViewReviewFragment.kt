package com.example.reviewr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.ViewReviewFragmentBinding

class ViewReviewFragment : Fragment() {

    private lateinit var binding: ViewReviewFragmentBinding
    private lateinit var reviewViewModel: ReviewViewModel
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewReviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]

        // Observe the LiveData for the specific review
        reviewViewModel.selectedReview.observe(viewLifecycleOwner) { review ->
            displayReviewDetails(review)
        }

        // Fetch the review details using the ViewModel
        postId?.let {
            reviewViewModel.fetchReviewDetails(it)
        }

        // Go Back Button
        binding.goBackButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous screen (MapFragment)
        }
    }

    private fun displayReviewDetails(review: Map<String, Any>?) {
        if (review != null) {
            binding.reviewTitle.text = review["title"] as? String ?: "No Title"
            binding.reviewDescription.text = review["description"] as? String ?: "No Description"
            binding.reviewStatus.text = "Status: ${review["status"] as? String ?: "Unknown"}"
            binding.reviewCategory.text = "Category: ${review["category"] as? String ?: "Unknown"}"
        } else {
            Toast.makeText(requireContext(), "Review not found.", Toast.LENGTH_SHORT).show()
        }
    }
}
