package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.RegisterFragmentBinding

class RegisterFragment : Fragment() {

    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate back to Welcome Screen
        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
        }

        // Handle Register Button (Logic will be added later)
        binding.registerButton.setOnClickListener {
            // Placeholder for registration logic
        }

        // Handle Upload Profile Picture Button (Logic will be added later)
        binding.uploadPictureButton.setOnClickListener {
            // Placeholder for uploading profile picture
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
