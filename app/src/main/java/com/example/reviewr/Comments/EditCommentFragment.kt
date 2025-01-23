package com.example.reviewr.Comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reviewr.ViewModel.CommentViewModel
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.EditCommentFragmentBinding

class EditCommentFragment : Fragment() {

    private var _binding: EditCommentFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel
    private val args: EditCommentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditCommentFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var commentViewModel: CommentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentViewModel = ViewModelProvider(requireActivity())[CommentViewModel::class.java]
        val commentId = args.commentId

        // Fetch the comment details
        commentViewModel.fetchComment(commentId) { comment ->
            if (comment != null) {
                binding.commentTitleInput.setText(comment["title"] as? String ?: "")
                binding.commentDescriptionInput.setText(comment["description"] as? String ?: "")
            } else {
                Toast.makeText(requireContext(), "Failed to load comment details.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // Save button logic
        binding.saveButton.setOnClickListener {
            val updatedComment = mapOf(
                "title" to binding.commentTitleInput.text.toString().trim(),
                "description" to binding.commentDescriptionInput.text.toString().trim(),
                "lastEdited" to com.google.firebase.Timestamp.now() // Add the last edited timestamp
            )

            commentViewModel.updateComment(args.commentId, updatedComment) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Comment updated successfully.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update comment.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Cancel button logic
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
