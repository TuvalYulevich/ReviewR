package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reviewr.ViewModel.UserViewModel
import com.example.reviewr.databinding.ViewUserDataFragmentBinding

class ViewUserDataFragment : Fragment() {

    private var _binding: ViewUserDataFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewUserDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UserViewModel
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val userId = userViewModel.getCurrentUser()?.uid ?: return

        // Fetch personal details
        userViewModel.fetchUserDetails(userId) { userDetails ->
            binding.usernameText.text = "Username: ${userDetails["username"] ?: "N/A"}"
            binding.firstNameText.text = "First Name: ${userDetails["firstName"] ?: "N/A"}"
            binding.lastNameText.text = "Last Name: ${userDetails["lastName"] ?: "N/A"}"
            binding.emailText.text = "Email: ${userDetails["email"] ?: "N/A"}"
            binding.ageText.text = "Age: ${userDetails["age"] ?: "N/A"}"
        }

        // Fetch review and comment counts
        userViewModel.fetchReviewCount(userId) { count ->
            binding.reviewCountText.text = "Reviews: $count"
        }
        userViewModel.fetchCommentCount(userId) { count ->
            binding.commentCountText.text = "Comments: $count"
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
