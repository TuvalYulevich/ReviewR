package com.example.reviewr.Reviews

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.reviewr.CommentAdapter.CommentAdapter
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.CommentPopupBinding
import com.example.reviewr.databinding.ViewReviewFragmentBinding
import com.google.firebase.auth.FirebaseAuth

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ViewReviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView layout manager
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Bind the Add Comment button
        binding.commentButton.setOnClickListener {
            showCommentPopup()
        }

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

        // Observe Comments LiveData
        reviewViewModel.comments.observe(viewLifecycleOwner) { comments ->
            updateCommentsUI(comments)
        }

        // Fetch existing comments for the review
        postId?.let {
            reviewViewModel.fetchComments(it)
        }
    }

    // Show the review details interface
    private fun displayReviewDetails(review: Map<String, Any>?) {
        if (review != null) {
            binding.reviewTitle.text = review["title"] as? String ?: "No Title"
            binding.reviewDescription.text = review["description"] as? String ?: "No Description"
            binding.reviewStatus.text = "Status: ${review["status"] as? String ?: "Unknown"}"
            binding.reviewCategory.text = "Category: ${review["category"] as? String ?: "Unknown"}"

            // Load the profile picture using Glide
            val imageUrl  = review["imageUrl"] as? String
            if (!imageUrl .isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl ) // Load the review image URL
                    .placeholder(R.drawable.ic_launcher_foreground) // Default image while loading
                    .into(binding.reviewImageView) // Target ImageView
            } else {
                // Show default profile picture if no URL is available
                binding.reviewImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Timestamp for when the review (Post) was created
            val timestamp = review["timestamp"] as? com.google.firebase.Timestamp
            binding.reviewTime.text = "Posted: ${timestamp?.toDate()?.toString() ?: "Unknown Time"}"

            // Timestamp for when the review (Post) was last edited
            val lastEdited = review["lastEdited"] as? com.google.firebase.Timestamp
            if (lastEdited != null) {
                binding.reviewEditedTime.text = "Edited: ${lastEdited.toDate()}"
                binding.reviewEditedTime.visibility = View.VISIBLE
            } else {
                binding.reviewEditedTime.visibility = View.GONE
            }
        }
    }

    // Show comments live when commented on a review interface
    private fun showCommentPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val popupBinding = CommentPopupBinding.inflate(layoutInflater)
        builder.setView(popupBinding.root)

        val dialog = builder.create()
        dialog.show()

        popupBinding.postCommentButton.setOnClickListener {
            val title = popupBinding.commentTitleInput.text.toString()
            val description = popupBinding.commentDescriptionInput.text.toString()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            if (title.isNotEmpty() && description.isNotEmpty()) {
                // Fetch the username of the comment author using ReviewViewModel
                reviewViewModel.fetchReviewAuthor(currentUserId) { username ->
                    val comment = mapOf(
                        "postId" to postId!!,
                        "userId" to currentUserId,
                        "title" to title,
                        "description" to description,
                        "username" to username, // Use the fetched username
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    // Post the comment using ReviewViewModel
                    reviewViewModel.postComment(postId!!, comment) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Comment posted successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Failed to post comment.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Title and description are required.", Toast.LENGTH_SHORT).show()
            }
        }
        popupBinding.goBackCommentButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    // Updating comments UI if anything was done with them
    private fun updateCommentsUI(comments: List<Map<String, Any>>) {
        if (comments.isEmpty()) {
            binding.commentsRecyclerView.visibility = View.GONE
            binding.noCommentsText.visibility = View.VISIBLE
        } else {
            binding.commentsRecyclerView.visibility = View.VISIBLE
            binding.noCommentsText.visibility = View.GONE
            val adapter = CommentAdapter(comments)
            binding.commentsRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged() // Ensure the RecyclerView refreshes
        }
    }
}
