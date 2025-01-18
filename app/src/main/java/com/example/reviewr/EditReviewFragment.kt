package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.EditReviewFragmentBinding

class EditReviewFragment : Fragment() {

    private var _binding: EditReviewFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel
    private val args: EditReviewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditReviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]
        val postId = args.postId

        // Fetch the review details
        reviewViewModel.fetchReview(postId) { review ->
            if (review != null) {
                binding.reviewTitleInput.setText(review["title"] as? String)
                binding.reviewDescriptionInput.setText(review["description"] as? String)

                val status = review["status"] as? String
                if (status == "Good") {
                    binding.statusGood.isChecked = true
                } else if (status == "Bad") {
                    binding.statusBad.isChecked = true
                }

                val categoryAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.review_categories,
                    android.R.layout.simple_spinner_item
                )
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.categorySpinner.adapter = categoryAdapter

                val category = review["category"] as? String
                val categoryPosition = categoryAdapter.getPosition(category)
                binding.categorySpinner.setSelection(categoryPosition)
            }
        }

        // Save Button Logic
        binding.saveButton.setOnClickListener {
            val updatedReview = mapOf(
                "title" to binding.reviewTitleInput.text.toString().trim(),
                "description" to binding.reviewDescriptionInput.text.toString().trim(),
                "status" to view?.findViewById<RadioButton>(binding.statusRadioGroup.checkedRadioButtonId)?.text.toString(),
                "category" to binding.categorySpinner.selectedItem.toString()
            )

            reviewViewModel.updateReview(postId, updatedReview) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Review updated successfully.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update review.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Cancel Button Logic
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
