package com.example.reviewr.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.EditPersonalDetailsFragmentBinding

class EditPersonalDetailsFragment : Fragment() {

    private var _binding: EditPersonalDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

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
                    // Update reviews and comments
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


