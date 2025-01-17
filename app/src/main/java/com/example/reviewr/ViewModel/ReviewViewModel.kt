package com.example.reviewr.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // LiveData for review posting status
    private val _postReviewStatus = MutableLiveData<Boolean?>()
    val postReviewStatus: LiveData<Boolean?> get() = _postReviewStatus

    // LiveData to hold the list of reviews
    private val _reviews = MutableLiveData<List<Map<String, Any>>>()
    val reviews: LiveData<List<Map<String, Any>>> get() = _reviews

    // LiveData to hold the selected review details
    private val _selectedReview = MutableLiveData<Map<String, Any>?>()
    val selectedReview: LiveData<Map<String, Any>?> get() = _selectedReview


    // Fetch reviews from Firestore
    fun fetchReviews() {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reviewList = querySnapshot.documents.mapNotNull { it.data }
                _reviews.value = reviewList
            }
            .addOnFailureListener {
                _reviews.value = emptyList() // Set an empty list on failure
            }
    }

    // Fetch specific review details from Firestore
    fun fetchReviewDetails(postId: String) {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _selectedReview.value = document.data
                } else {
                    _selectedReview.value = null
                }
            }
            .addOnFailureListener {
                _selectedReview.value = null
            }
    }

    // Post a new review to Firestore
    fun postReview(review: Map<String, Any>) {
        firestore.collection("posts")
            .add(review)
            .addOnSuccessListener { documentReference ->
                val postId = documentReference.id
                firestore.collection("posts").document(postId).update("postId", postId)
                    .addOnCompleteListener {
                        _postReviewStatus.value = it.isSuccessful
                    }
            }
            .addOnFailureListener {
                _postReviewStatus.value = false
            }
    }

    fun resetPostReviewStatus() {
        _postReviewStatus.value = null
    }

    // Get the currently logged-in user
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
}
