package com.example.reviewr.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
            uploadImageToCloudinary(it) { url ->
                profilePictureUrl = url
                Toast.makeText(requireContext(), "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.registerButton.setOnClickListener { registerUser() }
        binding.uploadPictureButton.setOnClickListener { imagePicker.launch("image/*") }
        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
        }
    }

    private fun uploadImageToCloudinary(uri: Uri, callback: (String) -> Unit) {
        try {
            MediaManager.get().upload(uri)
                .option("folder", "profile_pictures/")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()
                    }
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as String
                        callback(imageUrl)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo) {
                        Toast.makeText(requireContext(), "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo) {}
                }).dispatch()
        } catch (e: IllegalStateException) {
            Toast.makeText(requireContext(), "MediaManager not initialized. Please restart the app.", Toast.LENGTH_SHORT).show()
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
        userViewModel.register(email, password, username, firstName, lastName, age)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is UserViewModel.RegistrationResult.Success -> {
                        val userId = userViewModel.getCurrentUser()?.uid
                        userId?.let {
                            val updatedData = HashMap<String, Any>()
                            updatedData["profilePictureUrl"] = profilePictureUrl ?: ""
                            userViewModel.updateUserDetails(it, updatedData) { success ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update profile picture.", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
