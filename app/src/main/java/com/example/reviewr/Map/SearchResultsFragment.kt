package com.example.reviewr.Map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewr.R
import com.example.reviewr.ViewModel.ReviewViewModel
import com.example.reviewr.adapters.ReviewAdapter

class SearchResultsFragment : Fragment() {
    private lateinit var reviewViewModel: ReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_results_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]

        // Initialize RecyclerView and Adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviewsRecyclerView)
        val goBackButton = view.findViewById<Button>(R.id.goBackButton)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe filteredReviews to update RecyclerView
        reviewViewModel.filteredReviews.observe(viewLifecycleOwner) { reviews ->
            val adapter = ReviewAdapter(
                reviews = reviews.toMutableList(),
                showEditDeleteButtons = false, // Hide edit/delete buttons
                onItemClicked = { postId ->
                    val action = SearchResultsFragmentDirections.actionSearchResultsFragmentToViewReviewFragment(postId)
                    findNavController().navigate(action)
                }
            )
            recyclerView.adapter = adapter
        }

        // Go back functionality
        goBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}




