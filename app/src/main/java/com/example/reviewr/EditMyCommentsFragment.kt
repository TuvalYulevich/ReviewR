package com.example.reviewr.ui

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
import com.example.reviewr.CommentAdapter.CommentAdapter
import com.example.reviewr.ViewModel.CommentViewModel
import com.example.reviewr.databinding.EditMyCommentsFragmentBinding
import com.google.firebase.auth.FirebaseAuth

class EditMyCommentsFragment : Fragment() {

    private var _binding: EditMyCommentsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentViewModel: CommentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditMyCommentsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentViewModel = ViewModelProvider(requireActivity())[CommentViewModel::class.java]

        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch user comments
        commentViewModel.fetchUserComments(userId) { comments ->
            Log.d("EditMyCommentsFragment", "Comments passed to adapter: $comments") // Debug log

            if (comments.isEmpty()) {
                binding.noCommentsText.visibility = View.VISIBLE
                binding.commentsRecyclerView.visibility = View.GONE
            } else {
                binding.noCommentsText.visibility = View.GONE
                binding.commentsRecyclerView.visibility = View.VISIBLE

                val adapter = CommentAdapter(
                    comments = comments,
                    onEditClicked = { comment ->
                        val commentId = comment["commentId"] as? String ?: return@CommentAdapter
                        val action = EditMyCommentsFragmentDirections.actionEditMyCommentsFragmentToEditCommentFragment(commentId)
                        findNavController().navigate(action)
                    },
                    onDeleteClicked = { commentId ->
                        commentViewModel.deleteComment(commentId) { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Comment deleted successfully.", Toast.LENGTH_SHORT).show()
                                commentViewModel.fetchUserComments(userId) { updatedComments ->
                                    Log.d("EditMyCommentsFragment", "Updated comments passed to adapter: $updatedComments") // Debug log
                                    binding.commentsRecyclerView.adapter = CommentAdapter(updatedComments)
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete comment.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                binding.commentsRecyclerView.adapter = adapter
            }
        }

        // Go Back button
        binding.goBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
