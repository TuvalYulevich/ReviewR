package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.MainUserFragmentBinding
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.ViewModel.UserViewModel

class MainUserFragment : Fragment() {

    private var _binding: MainUserFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainUserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Display the logged-in user's username
        val currentUser = userViewModel.getCurrentUser()
        if (currentUser != null) {
            val username = currentUser.displayName ?: currentUser.email ?: "User"
            binding.welcomeMessage.text = "Welcome to ReviewR, $username!"
        } else {
            Toast.makeText(requireContext(), "No user logged in.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_mainUserFragment_to_welcomeFragment)
        }

        binding.mapButton.setOnClickListener {
            // Navigate to Map Screen
            findNavController().navigate(R.id.action_mainUserFragment_to_mapFragment)
        }

        binding.searchReviewsButton.setOnClickListener {
            // Navigate to Search Reviews Screen
        }

        binding.viewMyInfoButton.setOnClickListener {
            // Navigate to View My Information Screen
        }

        // Logout button
        binding.logoutButton.setOnClickListener {
            userViewModel.logout()
            Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_mainUserFragment_to_welcomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

