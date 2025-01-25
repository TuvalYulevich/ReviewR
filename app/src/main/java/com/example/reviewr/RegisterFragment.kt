package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reviewr.R
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.RegisterFragmentBinding

class RegisterFragment : Fragment() {

    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private var profilePictureUrl: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Delete the previously uploaded image if it exists
            profilePictureUrl?.let { previousUrl ->
                userViewModel.deleteProfileImage(previousUrl) { success ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Previous image deleted successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete the previous image.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            // Upload the new image
            userViewModel.uploadProfileImage(it).observe(viewLifecycleOwner) { status ->
                val (success, imageUrl) = status
                requireActivity().runOnUiThread {
                    if (success) {
                        profilePictureUrl = imageUrl
                        // Show image preview using Glide
                        binding.profilePicturePreview.visibility = View.VISIBLE
                        Glide.with(this)
                            .load(imageUrl)
                            .into(binding.profilePicturePreview)
                        Toast.makeText(requireContext(), "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Profile picture upload failed: $imageUrl", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.registerButton.setOnClickListener { registerUser() }
        binding.uploadPictureButton.setOnClickListener { imagePicker.launch("image/*") }
        binding.goBackButton.setOnClickListener {
            handleGoBack()
        }
    }

    private fun registerUser() {
        val username = binding.usernameInput.text.toString().trim()
        val firstName = binding.firstNameInput.text.toString().trim()
        val lastName = binding.lastNameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val age = binding.ageInput.text.toString().trim()

        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || age.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        userViewModel.register(email, password, username, firstName, lastName, age).observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserViewModel.RegistrationResult.Success -> {
                    val userId = userViewModel.getCurrentUser()?.uid
                    userId?.let {
                        val updatedData = HashMap<String, Any>()
                        updatedData["profilePictureUrl"] = profilePictureUrl ?: ""
                        userViewModel.updateUserDetails(it, updatedData) { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to upload profile picture.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
                }
                is UserViewModel.RegistrationResult.Failure -> {
                    Toast.makeText(requireContext(), "Registration failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleGoBack() {
        // If a profile picture was uploaded but not used for registration, delete it
        profilePictureUrl?.let { imageUrl ->
            userViewModel.deleteProfileImage(imageUrl) { success ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "Unused profile picture deleted.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete unused profile picture.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Navigate back to the Welcome Fragment
        findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
