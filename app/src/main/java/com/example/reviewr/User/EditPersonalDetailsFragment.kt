package com.example.reviewr.User

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reviewr.Data.UserEntity
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.EditPersonalDetailsFragmentBinding


class EditPersonalDetailsFragment : Fragment() {

    private var _binding: EditPersonalDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    private val imagePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadNewProfilePicture(it) }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            binding.passwordInput.setText(userDetails["password"] as? String ?: "")
        }

        // Save personal details button
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
                    // Save to Room database
                    userViewModel.fetchUserDetails(userId) { userDetails ->
                        val updatedUser = UserEntity(
                            userId = userId,
                            username = updatedData["username"] as String,
                            firstName = updatedData["firstName"] as String,
                            lastName = updatedData["lastName"] as String,
                            email = userDetails["email"] as String,
                            age = updatedData["age"] as String,
                            profileImageUrl = userDetails["profilePictureUrl"] as? String ?: "",
                            password = userDetails["password"] as String // Include password
                        )
                        userViewModel.updateUserInRoom(updatedUser)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update personal details.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Save email button
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
                    // Save to Room database
                            userViewModel.fetchUserDetails(userId) { userDetails ->
                                val updatedUser = UserEntity(
                                    userId = userId,
                                    username = userDetails["username"] as String,
                                    firstName = userDetails["firstName"] as String,
                                    lastName = userDetails["lastName"] as String,
                                    email = email, // Updated email
                                    age = userDetails["age"] as String,
                                    profileImageUrl = userDetails["profilePictureUrl"] as? String ?: "",
                                    password = userDetails["password"] as String // Include password
                                )
                                userViewModel.updateUserInRoom(updatedUser)
                            }
                } else {
                    Toast.makeText(requireContext(), "Failed to update email: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Save password button
        binding.savePasswordButton.setOnClickListener {
            val currentPassword = binding.currentPasswordInput.text.toString().trim()
            val newPassword = binding.newPasswordInput.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Both current and new passwords are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userViewModel.updatePassword(currentPassword, newPassword) { success, errorMessage ->
                if (success) {
                    // Save to Room database
                    userViewModel.fetchUserDetails(userId) { userDetails ->
                        val updatedUser = UserEntity(
                            userId = userId,
                            username = userDetails["username"] as String,
                            firstName = userDetails["firstName"] as String,
                            lastName = userDetails["lastName"] as String,
                            email = userDetails["email"] as String,
                            age = userDetails["age"] as String,
                            profileImageUrl = userDetails["profilePictureUrl"] as? String ?: "",
                            password = newPassword // Update password
                        )
                        userViewModel.updateUserInRoom(updatedUser)
                    }
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
            userViewModel.fetchUserDetails(userId) { userDetails ->
                val profilePictureUrl = userDetails["profilePictureUrl"] as? String ?: return@fetchUserDetails
                userViewModel.updateUserDetails(userId, mapOf("profilePictureUrl" to "")) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Profile picture removed successfully!", Toast.LENGTH_SHORT).show()
                        val updatedUser = UserEntity(
                            userId = userId,
                            username = userDetails["username"] as String,
                            firstName = userDetails["firstName"] as String,
                            lastName = userDetails["lastName"] as String,
                            email = userDetails["email"] as String,
                            age = userDetails["age"] as String,
                            password = userDetails["password"] as String,
                            profileImageUrl = "" // Updated profile picture URL
                        )
                        userViewModel.updateUserInRoom(updatedUser)
                        userViewModel.deleteProfileImage(profilePictureUrl) { deleteSuccess ->
                            if (!deleteSuccess) {
                                Log.e("EditPersonalDetails", "Failed to delete image from Cloudinary.")
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to remove profile picture.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Go back button
        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // Upload a new profile picture (Update profile picture) interface
    private fun uploadNewProfilePicture(uri: Uri) {
        val userId = userViewModel.getCurrentUser()?.uid ?: return
        userViewModel.fetchUserDetails(userId) { userDetails ->
            val oldProfilePictureUrl = userDetails["profilePictureUrl"] as? String
            userViewModel.uploadProfileImage(uri).observe(viewLifecycleOwner) { (success, imageUrl) ->
                if (success && imageUrl != null) {
                    userViewModel.updateUserDetails(userId, mapOf("profilePictureUrl" to imageUrl)) { updateSuccess ->
                        if (updateSuccess) {
                            Toast.makeText(requireContext(), "Profile picture updated successfully.", Toast.LENGTH_SHORT).show()
                            // Save to Room database
                            val updatedUser = UserEntity(
                                userId = userId,
                                username = userDetails["username"] as String,
                                firstName = userDetails["firstName"] as String,
                                lastName = userDetails["lastName"] as String,
                                email = userDetails["email"] as String,
                                age = userDetails["age"] as String,
                                password = userDetails["password"] as String,
                                profileImageUrl = imageUrl // Updated profile picture URL
                            )
                            userViewModel.updateUserInRoom(updatedUser)
                            // Delete the old image if it exists
                            oldProfilePictureUrl?.let { oldUrl ->
                                if (oldUrl.isNotEmpty()) {
                                    userViewModel.deleteProfileImage(oldUrl)
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile picture in Firebase.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to upload new profile picture.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
