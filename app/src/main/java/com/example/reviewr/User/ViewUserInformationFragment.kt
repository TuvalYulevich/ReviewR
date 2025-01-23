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
import com.example.reviewr.ViewModel.UserViewModel


class ViewUserInformationFragment : Fragment() {

    private var _binding: ViewUserInformationFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewUserInformationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigation actions for buttons
        binding.viewUserDataButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewUserInformationFragment_to_viewUserDataFragment)
        }

        binding.editPersonalDetailsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editPersonalDetailsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
            }

        binding.editMyReviewsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyReviewsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editMyCommentsButton.setOnClickListener {
            if(NetworkUtils.isOnline(requireContext())) {
                findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyCommentsFragment)
            }
            else {
                Toast.makeText(requireContext(), "You cannot edit when offline.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}