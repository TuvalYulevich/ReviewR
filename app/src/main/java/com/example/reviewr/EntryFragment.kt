package com.example.reviewr.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.ViewModel.UserViewModel

class EntryFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.entry_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Check if user is remembered
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            // Attempt auto-login
            userViewModel.login(savedEmail, savedPassword).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is UserViewModel.LoginResult.Success -> {
                        findNavController().navigate(R.id.action_entryFragment_to_mainUserFragment)
                    }
                    is UserViewModel.LoginResult.Failure -> {
                        // Navigate to WelcomeFragment if login fails
                        findNavController().navigate(R.id.action_entryFragment_to_welcomeFragment)
                    }
                }
            }
        } else {
            // No saved credentials, navigate to WelcomeFragment
            findNavController().navigate(R.id.action_entryFragment_to_welcomeFragment)
        }
    }
}
