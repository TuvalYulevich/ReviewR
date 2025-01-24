package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

        // Load background image with Glide
        //val backgroundImageUrl = "https://res.cloudinary.com/dm8sulfig/image/upload/v1737723782/RegisterImage_h6uyi2.png" // Replace with your image URL
        //Glide.with(this)
            //.//load(backgroundImageUrl)
            //.into(binding.backgroundImageView)


        // Navigate to Register Screen
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
        }

        // Navigate to Login Screen
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
