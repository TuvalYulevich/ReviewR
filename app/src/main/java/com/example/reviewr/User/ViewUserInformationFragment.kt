package com.example.reviewr.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reviewr.Data.AppDatabase
import com.example.reviewr.R
import com.example.reviewr.databinding.ViewUserInformationFragmentBinding
import com.example.reviewr.Utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewUserInformationFragment : Fragment() {

    private var _binding: ViewUserInformationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ViewUserInformationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appImageDao = AppDatabase.getInstance(requireContext()).appImageDao()

        // Use a coroutine to fetch the image on a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val imageEntity = appImageDao.getImageByKey("DataImage") // Fetch using the stable key
            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                if (imageEntity != null) {
                    Glide.with(this@ViewUserInformationFragment)
                        .load(imageEntity.url)
                        .into(binding.DataImage)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Image not found for the given key.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Navigation to view user data
        binding.viewUserDataButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewUserInformationFragment_to_viewUserDataFragment)
        }

        // Navigation to edit user personal data (Possible only if the app is connected to the internet)
        binding.editPersonalDetailsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editPersonalDetailsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation to edit user reviews (Possible only if the app is connected to the internet)
        binding.editMyReviewsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyReviewsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation to edit user comments (Possible only if the app is connected to the internet)
        binding.editMyCommentsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyCommentsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate back main user screen
        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}