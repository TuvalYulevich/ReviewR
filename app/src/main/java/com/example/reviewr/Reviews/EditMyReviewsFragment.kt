package com.example.reviewr.Reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reviewr.ViewModel.CommentViewModel
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

                val adapter = ReviewAdapter(
                    reviews = reviews.toMutableList(),
                    showEditDeleteButtons = true, // Show edit/delete buttons
                    onEditClicked = { review ->
                        val postId = review["postId"] as? String ?: return@ReviewAdapter
                        val action = EditMyReviewsFragmentDirections.actionEditMyReviewsFragmentToEditReviewFragment(postId)
                        findNavController().navigate(action)
                    },
                    onDeleteClicked = { postId ->
                        val adapter = binding.reviewsRecyclerView.adapter as? ReviewAdapter ?: return@ReviewAdapter
                        val position = adapter.reviews.indexOfFirst { it["postId"] == postId }
                        if (position != -1) {
                            val commentViewModel = ViewModelProvider(requireActivity())[CommentViewModel::class.java]
                            commentViewModel.deleteCommentsByPostId(postId) { commentDeleteSuccess ->
                                if (commentDeleteSuccess) {
                                    reviewViewModel.deleteReview(postId) { reviewDeleteSuccess ->
                                        if (reviewDeleteSuccess) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Review and associated comments deleted successfully.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Remove the item from the adapter and notify it
                                            adapter.reviews.removeAt(position)
                                            adapter.notifyItemRemoved(position)
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to delete review.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete comments for the review.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    ,
                    onItemClicked = { postId ->
                        val action = EditMyReviewsFragmentDirections.actionEditMyReviewsFragmentToViewReviewFragment(postId)
                        findNavController().navigate(action)
                    }
                )
                binding.reviewsRecyclerView.adapter = adapter
            }
        }
        // Go back button
        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
