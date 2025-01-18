package com.example.reviewr.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Check if user is already remembered
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            loginUser(savedEmail, savedPassword, rememberMe = true, skipToast = true)
        }

        // Login Button Click
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val rememberMe = binding.rememberMeCheckbox.isChecked

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, rememberMe)
            } else {
                Toast.makeText(requireContext(), "Email and password are required.", Toast.LENGTH_SHORT).show()
            }
        }

        // Go Back Button Click
        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_welcomeFragment)
        }
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean = false, skipToast: Boolean = false) {
        userViewModel.login(email, password).observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserViewModel.LoginResult.Success -> {
                    if (rememberMe) {
                        saveUserCredentials(email, password)
                    }
                    navigateToMainScreen()
                    if (!skipToast) {
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    }
                }
                is UserViewModel.LoginResult.Failure -> {
                    Toast.makeText(requireContext(), "Login failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserCredentials(email: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    private fun navigateToMainScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_mainUserFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
