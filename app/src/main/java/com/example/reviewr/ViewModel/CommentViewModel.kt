package com.example.reviewr.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class CommentViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // Fetch a specific comment
    fun fetchComment(commentId: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection("comments").document(commentId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.data)
            }
            .addOnFailureListener {
                Log.e("CommentViewModel", "Failed to fetch comment: $it")
                callback(null)
            }
    }

    // Update a specific comment
    fun updateComment(commentId: String, updatedComment: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("comments").document(commentId)
            .update(updatedComment)
            .addOnSuccessListener {
                Log.d("CommentViewModel", "Comment updated successfully.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to update comment: $exception")
                callback(false)
            }
    }

    fun fetchUserComments(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        firestore.collection("comments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val comments = documents.mapNotNull { document ->
                    document.data.apply { this["commentId"] = document.id }
                }
                Log.d("CommentViewModel", "Fetched comments: $comments") // Add this line
                callback(comments)
            }
            .addOnFailureListener {
                Log.e("CommentViewModel", "Failed to fetch user comments: $it")
                callback(emptyList())
            }
    }


    // Delete a specific comment
    fun deleteComment(commentId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comments").document(commentId)
            .delete()
            .addOnSuccessListener {
                Log.d("CommentViewModel", "Comment deleted successfully.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to delete comment: $exception")
                callback(false)
            }
    }
}
