package com.example.reviewr.User

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.reviewr.Data.AppDatabase
import com.example.reviewr.Utils.NetworkUtils
import com.example.reviewr.Map.SearchDialogFragment
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.ViewModel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainUserFragment : Fragment() {

    private var _binding: MainUserFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel
    private lateinit var reviewViewModel: ReviewViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MainUserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appImageDao = AppDatabase.getInstance(requireContext()).appImageDao()

        // Use a coroutine to fetch the image on a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val imageEntity = appImageDao.getImageByKey("backgroundImage") // Fetch using the stable key
            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                if (imageEntity != null) {
                    Glide.with(this@MainUserFragment)
                        .load(imageEntity.url)
                        .into(binding.backgroundImageView)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Image not found for the given key.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Display the logged-in user's username (From the Firebase authenticator)
        val currentUser = userViewModel.getCurrentUser()
        if (currentUser != null) {
            val username = currentUser.displayName ?: currentUser.email ?: "User"
            binding.welcomeMessage.text = "Welcome to ReviewR!"
        } else {
            Toast.makeText(requireContext(), "No user logged in.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_mainUserFragment_to_welcomeFragment)
        }

        binding.mapButton.setOnClickListener {
            // Navigate to Map Screen
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_mainUserFragment_to_mapFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot enter map when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize ViewModel
        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]

        // Set up the search button click listener (Search is allowed only if the app is connected to the internet)
        binding.searchReviewsButton.setOnClickListener {
            if (NetworkUtils.isOnline(requireContext())){
            val dialog = SearchDialogFragment { filters ->
                when (filters["action"]) {
                    "showAll" -> {
                        // Fetch all reviews and navigate to SearchResultsFragment
                        reviewViewModel.fetchAllReviews() // Use a method specifically for fetching all reviews
                        findNavController().navigate(
                            MainUserFragmentDirections.actionMainUserFragmentToSearchResultsFragment()
                        )
                    }
                    "applyFilters" -> {
                        // Apply filters and navigate to SearchResultsFragment
                        reviewViewModel.applyFilters2(filters)
                        findNavController().navigate(
                            MainUserFragmentDirections.actionMainUserFragmentToSearchResultsFragment()
                        )
                    }
                }
            }
            dialog.show(parentFragmentManager, "SearchDialog")
        }
            else{
                Toast.makeText(requireContext(), "You cannot search when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to view user information
        binding.viewMyInfoButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainUserFragment_to_viewUserInformationFragment)
        }

        // Logout interface
        // Logout interface
        binding.logoutButton.setOnClickListener {
            val currentUser = userViewModel.getCurrentUser()
            if (currentUser != null) {
                val userId = currentUser.uid
                userViewModel.logout(userId) // Pass the userId to the logout function
                clearUserCredentials()
                Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_mainUserFragment_to_welcomeFragment)
            } else {
                Toast.makeText(requireContext(), "No user is logged in.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Clearing user credentials after logout
    private fun clearUserCredentials() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}