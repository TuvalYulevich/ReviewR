package com.example.reviewr

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.CommentPopupBinding
import com.example.reviewr.databinding.ViewReviewFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.reviewr.CommentAdapter.CommentAdapter
import com.google.firebase.firestore.FirebaseFirestore

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

        postId?.let {
            reviewViewModel.fetchComments(it) // Fetch existing comments for the review
        }
    }

    private fun displayReviewDetails(review: Map<String, Any>?) {
        if (review != null) {
            binding.reviewTitle.text = review["title"] as? String ?: "No Title"
            binding.reviewDescription.text = review["description"] as? String ?: "No Description"
            binding.reviewStatus.text = "Status: ${review["status"] as? String ?: "Unknown"}"
            binding.reviewCategory.text = "Category: ${review["category"] as? String ?: "Unknown"}"

            // Fetch and display the username using the ViewModel
            val userId = review["userId"] as? String
            if (userId != null) {
                reviewViewModel.fetchReviewAuthor(userId) { username ->
                    binding.reviewAuthor.text = "By: $username"
                }
            } else {
                binding.reviewAuthor.text = "By: Unknown"
            }
            // Format and display the timestamp
            val timestamp = review["timestamp"] as? com.google.firebase.Timestamp
            val formattedDate = timestamp?.toDate()?.toString() ?: "Unknown Time"
            binding.reviewTime.text = formattedDate
        } else {
            Toast.makeText(requireContext(), "Review not found.", Toast.LENGTH_SHORT).show()
        }
    }



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



    private fun updateCommentsUI(comments: List<Map<String, Any>>) {
        Log.d("ViewReviewFragment", "Updating comments UI. Comments: $comments")
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
