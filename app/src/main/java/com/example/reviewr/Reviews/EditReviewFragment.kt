package com.example.reviewr.Reviews

import android.net.Uri
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
                "imageUrl" to (currentImageUrl ?: "") // Update the image URL
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
        MediaManager.get().upload(uri)
            .option("folder", "review_images/")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    currentImageUrl = resultData["secure_url"] as? String
                    currentImageUrl?.let {
                        binding.reviewImageView.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(it)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.reviewImageView)
                        Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo) {
                    Toast.makeText(requireContext(), "Failed to upload image: ${error.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Optional: Track upload progress
                }
            }).dispatch()
    }

    private fun deleteCurrentImage() {
        val publicId = currentImageUrl?.substringAfterLast("/")?.substringBeforeLast(".")
        if (publicId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No image to delete.", Toast.LENGTH_SHORT).show()
            return
        }



        val cloudName = "dm8sulfig"  // Replace with your Cloudinary cloud name
        val apiKey = "253965312649661"       // Replace with your Cloudinary API key
        val apiSecret = "HR8e9mCNeDklFHZuCLznYxHRGNQ" // Replace with your Cloudinary API secret

        val requestBody = okhttp3.FormBody.Builder()
            .add("public_id", publicId)
            .add("invalidate", "true")
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/destroy")
            .post(requestBody)
            .addHeader(
                "Authorization",
                "Basic ${android.util.Base64.encodeToString(
                    "$apiKey:$apiSecret".toByteArray(),
                    android.util.Base64.NO_WRAP
                )}"
            )
            .build()

        val client = okhttp3.OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to delete image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show()
                        currentImageUrl = null
                        binding.reviewImageView.visibility = View.GONE
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete image: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
