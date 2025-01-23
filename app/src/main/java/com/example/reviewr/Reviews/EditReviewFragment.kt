package com.example.reviewr.Reviews

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.databinding.EditReviewFragmentBinding
import java.io.IOException

class EditReviewFragment : Fragment() {

    private var _binding: EditReviewFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewViewModel: ReviewViewModel
    private val args: EditReviewFragmentArgs by navArgs()

    private var currentImageUrl: String? = null // URL of the current review image

    private val imagePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadNewImage(it) }
    }

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
                populateReviewDetails(review)
            }
        }

        // Save Button Logic
        binding.saveButton.setOnClickListener {
            val updatedReview = mapOf(
                "title" to binding.reviewTitleInput.text.toString().trim(),
                "description" to binding.reviewDescriptionInput.text.toString().trim(),
                "status" to view?.findViewById<RadioButton>(binding.statusRadioGroup.checkedRadioButtonId)?.text.toString(),
                "category" to binding.categorySpinner.selectedItem.toString(),
                "imageUrl" to (currentImageUrl ?: ""),
                "lastEdited" to com.google.firebase.Timestamp.now() // Add the last edited timestamp
            )
            reviewViewModel.updateReview(args.postId, updatedReview) { success ->
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

        // Upload Image Button Logic
        binding.uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Delete Image Button Logic
        binding.deleteImageButton.setOnClickListener {
            deleteCurrentImage()
        }
    }

    private fun populateReviewDetails(review: Map<String, Any>) {
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

        // Load the image
        currentImageUrl = review["imageUrl"] as? String
        if (!currentImageUrl.isNullOrEmpty()) {
            binding.reviewImageView.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(currentImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(binding.reviewImageView)
        } else {
            binding.reviewImageView.visibility = View.GONE
        }
    }

    private fun uploadNewImage(uri: Uri) {
        reviewViewModel.uploadReviewImage(uri)
        reviewViewModel.imageUploadStatus.observe(viewLifecycleOwner) { (success, imageUrl) ->
            if (success && imageUrl != null) {
                currentImageUrl = imageUrl
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
    }

    private fun deleteCurrentImage() {
        currentImageUrl?.let { imageUrl ->
            val postId = args.postId // Ensure you have the postId from arguments
            // First, update the review in Firebase to remove the image URL
            val updatedReviewData = mapOf("imageUrl" to "")
            reviewViewModel.updateReview(postId, updatedReviewData) { updateSuccess ->
                requireActivity().runOnUiThread {
                    if (updateSuccess) {
                        // Update UI immediately after Firebase update
                        Toast.makeText(requireContext(), "Image removed from review.", Toast.LENGTH_SHORT).show()
                        currentImageUrl = null
                        binding.reviewImageView.visibility = View.GONE

                        // Proceed to delete the image from Cloudinary
                        reviewViewModel.deleteReviewImage(imageUrl) { deleteSuccess ->
                            if (!deleteSuccess) {
                                Log.e("EditReviewFragment", "Failed to delete image from Cloudinary.")
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to remove image from review.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "No image to delete.", Toast.LENGTH_SHORT).show()
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
