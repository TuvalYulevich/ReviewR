package com.example.reviewr.ui

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.EditPersonalDetailsFragmentBinding
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class EditPersonalDetailsFragment : Fragment() {

    private var _binding: EditPersonalDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    private val imagePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadNewProfilePicture(it) }
    }

    private val CLOUD_NAME = "dm8sulfig"
    private val API_KEY = "253965312649661"
    private val API_SECRET = "HR8e9mCNeDklFHZuCLznYxHRGNQ"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditPersonalDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val userId = userViewModel.getCurrentUser()?.uid ?: return

        // Fetch and populate user details
        userViewModel.fetchUserDetails(userId) { userDetails ->
            binding.usernameInput.setText(userDetails["username"] as? String ?: "")
            binding.firstNameInput.setText(userDetails["firstName"] as? String ?: "")
            binding.lastNameInput.setText(userDetails["lastName"] as? String ?: "")
            binding.ageInput.setText(userDetails["age"] as? String ?: "")
            binding.emailInput.setText(userDetails["email"] as? String ?: "")
        }

        // Save personal details
        binding.saveUserDetailsButton.setOnClickListener {
            val updatedData = mapOf(
                "username" to binding.usernameInput.text.toString().trim(),
                "firstName" to binding.firstNameInput.text.toString().trim(),
                "lastName" to binding.lastNameInput.text.toString().trim(),
                "age" to binding.ageInput.text.toString().trim()
            )

            userViewModel.updateUserDetails(userId, updatedData) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Personal details updated successfully.", Toast.LENGTH_SHORT).show()
                    userViewModel.updateReviewsAndComments(userId, updatedData)
                } else {
                    Toast.makeText(requireContext(), "Failed to update personal details.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Save email
        binding.saveEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and current password are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userViewModel.updateEmail(email, password) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), message ?: "Verification email sent. Please verify the new email.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update email: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Save password
        binding.savePasswordButton.setOnClickListener {
            val currentPassword = binding.currentPasswordInput.text.toString().trim()
            val newPassword = binding.newPasswordInput.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Both current and new passwords are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.updatePassword(currentPassword, newPassword) { success, errorMessage ->
                if (success) {
                    Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update password: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Upload a new profile picture
        binding.uploadProfilePictureButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Delete current profile picture
        binding.deleteProfilePictureButton.setOnClickListener {
            deleteCurrentProfilePicture()
        }

        // Go back button
        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun uploadNewProfilePicture(uri: Uri) {
        userViewModel.fetchUserDetails(userViewModel.getCurrentUser()?.uid ?: return) { userDetails ->
            val oldProfilePictureUrl = userDetails["profilePictureUrl"] as? String

            MediaManager.get().upload(uri)
                .option("folder", "profile_pictures/")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Toast.makeText(requireContext(), "Uploading new profile picture...", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val newProfilePictureUrl = resultData["secure_url"] as String
                        val userId = userViewModel.getCurrentUser()?.uid ?: return

                        userViewModel.updateUserDetails(userId, mapOf("profilePictureUrl" to newProfilePictureUrl)) { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Profile picture updated successfully.", Toast.LENGTH_SHORT).show()
                                oldProfilePictureUrl?.let { deleteImageFromCloudinary(it) }
                            } else {
                                Toast.makeText(requireContext(), "Failed to update profile picture in Firebase.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo) {
                        Toast.makeText(requireContext(), "Failed to upload new profile picture: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toDouble() / totalBytes.toDouble() * 100).toInt()
                        Log.d("UploadProgress", "Progress: $progress%")
                    }
                }).dispatch()
        }
    }

    private fun deleteCurrentProfilePicture() {
        userViewModel.fetchUserDetails(userViewModel.getCurrentUser()?.uid ?: return) { userDetails ->
            val profilePictureUrl = userDetails["profilePictureUrl"] as? String ?: return@fetchUserDetails
            val userId = userViewModel.getCurrentUser()?.uid ?: return@fetchUserDetails

            userViewModel.updateUserDetails(userId, mapOf("profilePictureUrl" to "")) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Profile picture deleted successfully.", Toast.LENGTH_SHORT).show()
                    deleteImageFromCloudinary(profilePictureUrl)
                } else {
                    Toast.makeText(requireContext(), "Failed to delete profile picture.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteImageFromCloudinary(imageUrl: String) {
        val publicId = imageUrl.substringAfterLast("/").substringBeforeLast(".") // Extract the public ID

        val requestBody = FormBody.Builder()
            .add("public_id", publicId)
            .add("invalidate", "true")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy")
            .post(requestBody)
            .addHeader(
                "Authorization",
                "Basic ${Base64.encodeToString("$API_KEY:$API_SECRET".toByteArray(), Base64.NO_WRAP)}"
            )
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Cloudinary", "Failed to delete image: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Cloudinary", "Successfully deleted image: $publicId")
                } else {
                    Log.e("Cloudinary", "Failed to delete image: ${response.message}")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
