package com.example.reviewr.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.WelcomeFragmentBinding

class WelcomeFragment : Fragment() {

    private var _binding: WelcomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WelcomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Navigate to Register Screen
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
        }

        // Navigate to Login Screen
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }

        // Show About Dialog
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            Welcome to ReviewR!
            
            This app allows you to share and discover reviews for various locations around you, in the simplest way possbile. 
            
            Features include:
            
            - Writing reviews for places.
            
            - Viewing reviews and ratings by others on the map according to your location.
            
            - Commenting on the reviews.
            
            - Real time chat on reviews using the comments!
            
            - Filtering and searching for specific reviews.
            
            - Editing reviews and comments on reviews to make them up to date.

            Enjoy exploring and sharing!
            
            ***Developed by Guy Halfon and Tuval Yulevich with the guidance of Yehuda Rozalio***
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("About ReviewR")
            .setMessage(aboutMessage)
            .setPositiveButton("Got it✅") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
