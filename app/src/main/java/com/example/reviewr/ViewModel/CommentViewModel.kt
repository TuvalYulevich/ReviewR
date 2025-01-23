package com.example.reviewr.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

// Comment ViewModel interacts with all of the databases offline and online in the actions regarding to comments
class CommentViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _comments = MutableLiveData<List<Map<String, Any>>>()
    val comments: LiveData<List<Map<String, Any>>> get() = _comments

    // Fetch single comment
    fun fetchComment(commentId: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection("comments").document(commentId).get()
            .addOnSuccessListener { document ->
                callback(document.data)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to fetch comment: $exception")
                callback(null)
            }
    }

    // Update a specific comment
    fun updateComment(commentId: String, updatedComment: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("comments").document(commentId).update(updatedComment)
            .addOnSuccessListener {
                Log.d("CommentViewModel", "Comment updated successfully.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to update comment: $exception")
                callback(false)
            }
    }

    // Fetch all comments from a specific user
    fun fetchUserComments(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        firestore.collection("comments").whereEqualTo("userId", userId).get()
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
        firestore.collection("comments").document(commentId).delete()
            .addOnSuccessListener {
                Log.d("CommentViewModel", "Comment deleted successfully.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to delete comment: $exception")
                callback(false)
            }
    }

    // Delete comments by postId (Delete all comments related to the post that was being deleted as well)
    fun deleteCommentsByPostId(postId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comments").whereEqualTo("postId", postId).get()
            .addOnSuccessListener { commentSnapshots ->
                val batch = firestore.batch()
                for (comment in commentSnapshots) {
                    val commentId = comment.id
                    val userId = comment.getString("userId") ?: continue
                    // Delete the comment
                    batch.delete(comment.reference)
                    // Update the user document
                    val userRef = firestore.collection("users").document(userId)
                    batch.update(
                        userRef,
                        "comments",
                        com.google.firebase.firestore.FieldValue.arrayRemove(commentId)
                    )
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("CommentViewModel", "All comments for review $postId deleted successfully.")
                        callback(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CommentViewModel", "Failed to delete comments for review $postId: $exception")
                        callback(false)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("CommentViewModel", "Failed to fetch comments for review $postId: $exception")
                callback(false)
            }
    }


}