package com.example.reviewr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reviewr.R
import com.example.reviewr.databinding.ViewUserInformationFragmentBinding

class ViewUserInformationFragment : Fragment() {

    private var _binding: ViewUserInformationFragmentBinding? = null
    private val binding get() = _binding!!

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
            findNavController().navigate(R.id.action_viewUserInformationFragment_to_editPersonalDetailsFragment)
        }

        binding.editMyReviewsButton.setOnClickListener {
             findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyReviewsFragment)
        }

        //binding.editMyCommentsButton.setOnClickListener {
            //findNavController().navigate(R.id.action_viewUserInformationFragment_to_editMyCommentsFragment)
        //}

        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}