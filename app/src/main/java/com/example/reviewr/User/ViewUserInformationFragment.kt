package com.example.reviewr.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.ViewUserInformationFragmentBinding
import com.example.reviewr.Utils.NetworkUtils


class ViewUserInformationFragment : Fragment() {

    private var _binding: ViewUserInformationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ViewUserInformationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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