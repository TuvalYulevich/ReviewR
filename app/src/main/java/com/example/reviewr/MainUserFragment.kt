package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.MainUserFragmentBinding

class MainUserFragment : Fragment() {

    private var _binding: MainUserFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainUserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Placeholder navigation logic for future screens
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

        binding.logoutButton.setOnClickListener {
            // Navigate back to Welcome Screen
            findNavController().navigate(R.id.action_mainUserFragment_to_welcomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
