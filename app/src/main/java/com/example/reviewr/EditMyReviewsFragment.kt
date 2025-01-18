package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.adapters.ReviewAdapter
import com.example.reviewr.databinding.EditMyReviewsFragmentBinding

class EditMyReviewsFragment : Fragment() {

    private var _binding: EditMyReviewsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditMyReviewsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]

        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userId = reviewViewModel.getCurrentUser()?.uid ?: return

        // Fetch user reviews
        reviewViewModel.fetchUserReviews(userId) { reviews ->
            if (reviews.isEmpty()) {
                binding.noReviewsText.visibility = View.VISIBLE
                binding.reviewsRecyclerView.visibility = View.GONE
            } else {
                binding.noReviewsText.visibility = View.GONE
                binding.reviewsRecyclerView.visibility = View.VISIBLE

                val adapter = ReviewAdapter(reviews,
                    onEditClicked = { review ->
                        // Navigate to an edit screen (or show dialog for editing)
                        val postId = review["postId"] as? String ?: return@ReviewAdapter
                        val action = EditMyReviewsFragmentDirections.actionEditMyReviewsFragmentToEditReviewFragment(postId)
                        findNavController().navigate(action)
                    },
                    onDeleteClicked = { postId ->
                        // Delete the review
                        reviewViewModel.deleteReview(postId) { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Review deleted successfully.", Toast.LENGTH_SHORT).show()
                                reviewViewModel.fetchUserReviews(userId) { updatedReviews ->
                                    binding.reviewsRecyclerView.adapter = ReviewAdapter(updatedReviews, onEditClicked = {}, onDeleteClicked = {})
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete review.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                binding.reviewsRecyclerView.adapter = adapter
            }
        }

        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
