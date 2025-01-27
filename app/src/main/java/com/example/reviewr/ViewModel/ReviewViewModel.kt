package com.example.reviewr.ViewModel

import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.security.MessageDigest

// Review ViewModel interacts with all of the databases offline and online in the actions regarding to reviews
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

    // LiveData to hold the list of comments
    private val _comments = MutableLiveData<List<Map<String, Any>>>()
    val comments: LiveData<List<Map<String, Any>>> get() = _comments

    // LiveData to hold the username
    private val _reviewAuthor = MutableLiveData<String>()
    val reviewAuthor: LiveData<String> get() = _reviewAuthor

    // Filtered Reviews from search button
    private val _filteredReviews = MutableLiveData<List<Map<String, Any>>>()
    val filteredReviews: LiveData<List<Map<String, Any>>> get() = _filteredReviews


    var imageUploadStatus = MutableLiveData<Pair<Boolean, String?>>()

    // Upload image for a review method
    fun uploadReviewImage(uri: Uri) {
        MediaManager.get().upload(uri)
            .option("folder", "review_images/") // Image source in Cloudinary
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    // Optionally indicate the upload started
                }
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    imageUploadStatus.postValue(Pair(true, imageUrl))
                }
                override fun onError(requestId: String?, error: ErrorInfo) {
                    imageUploadStatus.postValue(Pair(false, null))
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            }).dispatch()
    }


    // Delete review image
    fun deleteReviewImage(imageUrl: String, callback: ((Boolean) -> Unit)?=null) {
        // Verifying API credentials
        val CLOUD_NAME = "dm8sulfig"
        val API_KEY = "129181168733979"
        val API_SECRET = "uNaILxRogPyZ_FTQtnOWEQ-Tq5Y"

        // Extract the public ID from the URL
        val publicId = "review_images/" + imageUrl.substringAfterLast("/").substringBeforeLast(".")
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        // Generate the correct signature string (including invalidate=true)
        val signatureString = "invalidate=true&public_id=$publicId&timestamp=$timestamp$API_SECRET"
        val signature = MessageDigest.getInstance("SHA-1")
            .digest(signatureString.toByteArray())
            .joinToString("") { "%02x".format(it) }

        // Prepare the request body (include api_key [SUPER IMPORTANT])
        val requestBody = FormBody.Builder()
            .add("public_id", publicId)
            .add("timestamp", timestamp)
            .add("signature", signature)
            .add("invalidate", "true")
            .add("api_key", API_KEY)
            .build()

        val requestUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy"

        // Send the request
        val request = Request.Builder().url(requestUrl).post(requestBody).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Cloudinary", "Failed to delete image: ${e.message}")
                callback?.invoke(false)
            }
            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string() ?: "No response body"
                if (response.isSuccessful) {
                    Log.d("Cloudinary", "Successfully deleted image: $publicId, Response: $responseBody")
                    callback?.invoke(true)
                } else {
                    Log.e("Cloudinary", "Failed to delete image: ${response.message}, Response: $responseBody")
                    callback?.invoke(false)
                }
            }
        })
    }


    // Fetch reviews
    fun fetchFilteredReviews(filters: Map<String, String>? = null, userLocation: Location? = null) {
        val collectionRef = firestore.collection("posts")

        // Only apply status filter if it's not "ALL"
        var query: Query = collectionRef  // Initialize as Query type

        filters?.get("status")?.let { status ->
            if (status != "All") {
                query = collectionRef.whereEqualTo("status", status)
            }
        }

        filters?.get("category")?.let { category ->
            if (category != "All") {
                query = query.whereEqualTo("category", category)
            }
        }

        query.get().addOnSuccessListener { snapshots ->
            val reviews = snapshots.documents.mapNotNull { it.data }

            val finalReviews = if (userLocation != null && filters?.get("within500Meters")?.toBoolean() == true) {
                reviews.filter { review ->
                    val location = review["location"] as? Map<String, Double> ?: return@filter false
                    val reviewLocation = Location("").apply {
                        latitude = location["latitude"] ?: return@filter false
                        longitude = location["longitude"] ?: return@filter false
                    }
                    reviewLocation.distanceTo(userLocation) <= 500
                }
            } else {
                reviews
            }

            _filteredReviews.postValue(finalReviews)
        }.addOnFailureListener { exception ->
            Log.e("ReviewViewModel", "Error fetching reviews: $exception")
            _filteredReviews.postValue(emptyList())
        }
    }


    // Fetch reviews from Firestore
    fun fetchReviews() {
        firestore.collection("posts")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("ReviewViewModel", "Failed to listen for reviews: $exception")
                    _reviews.value = emptyList()
                    return@addSnapshotListener
                }
                val reviewList = snapshots?.documents?.mapNotNull { it.data } ?: emptyList()
                _reviews.value = reviewList
                Log.d("ReviewViewModel", "Real-time reviews fetched: $reviewList")
            }
    }

    // Fetch specific review details from Firestore
    fun fetchReviewDetails(postId: String) {
        firestore.collection("posts").document(postId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ReviewViewModel", "Error listening to review changes", error)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val reviewData = document.data
                    if (reviewData != null) {
                        _selectedReview.value = reviewData

                        // Set up live username updates
                        val userId = reviewData["userId"] as? String
                        if (userId != null) {
                            firestore.collection("users").document(userId)
                                .addSnapshotListener { userDoc, userError ->
                                    if (userError != null) {
                                        Log.e("ReviewViewModel", "Error fetching user", userError)
                                        _reviewAuthor.value = "Unknown"
                                        return@addSnapshotListener
                                    }

                                    if (userDoc != null && userDoc.exists()) {
                                        val username = userDoc.getString("username") ?: "Anonymous"
                                        _reviewAuthor.value = username
                                    } else {
                                        _reviewAuthor.value = "Unknown"
                                    }
                                }
                        } else {
                            _reviewAuthor.value = "Unknown"
                        }
                    }
                } else {
                    _selectedReview.value = null
                    _reviewAuthor.value = "Unknown"
                }
            }
    }

    // Post a new review to Firestore
    fun postReview(review: Map<String, Any>) {
        firestore.collection("posts").add(review)
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

    // Reseting review status
    fun resetPostReviewStatus() {
        _postReviewStatus.value = null
    }

    // Get the currently logged-in user
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    // Fetch comments for a specific review
    fun fetchComments(postId: String) {
        firestore.collection("comments").whereEqualTo("postId", postId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("ReviewViewModel", "Failed to listen for comments: $exception")
                    _comments.value = emptyList()
                    return@addSnapshotListener
                }
                val fetchedComments = snapshots?.map { it.data } ?: emptyList()
                _comments.value = fetchedComments
                Log.d("ReviewViewModel", "Real-time comments fetched: $fetchedComments")
            }
    }

    // Function to post a new comment
    fun postComment(postId: String, comment: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("comments").add(comment)
            .addOnSuccessListener {
                Log.d("ReviewViewModel", "Comment added with ID: ${it.id}")
                fetchComments(postId) // Fetch comments again after posting
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewViewModel", "Failed to add comment: $exception")
                callback(false)
            }
    }

    // Fetch review author
    fun fetchReviewAuthor(userId: String, callback: (String) -> Unit) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Anonymous"
                callback(username)
            }
            .addOnFailureListener {
                callback("Unknown")
            }
    }

    // Fetch user reviews
    fun fetchUserReviews(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        firestore.collection("posts").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                val reviews = documents.mapNotNull { it.data }
                callback(reviews)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Delete review
    fun deleteReview(postId: String, callback: (Boolean) -> Unit) {
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("imageUrl")
                // Delete the review document
                firestore.collection("posts").document(postId).delete()
                    .addOnSuccessListener {
                        Log.d("ReviewViewModel", "Review deleted successfully.")
                        // If the review has an associated image, delete it from Cloudinary
                        if (!imageUrl.isNullOrEmpty()) {
                            deleteReviewImage(imageUrl) { success ->
                                if (success) {
                                    Log.d("ReviewViewModel", "Image deleted from Cloudinary.")
                                } else {
                                    Log.e("ReviewViewModel", "Failed to delete image from Cloudinary.")
                                }
                            }
                        }
                        // Update local reviews LiveData
                        _reviews.value = _reviews.value?.filterNot { it["postId"] == postId }
                        callback(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ReviewViewModel", "Failed to delete review: $exception")
                        callback(false)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewViewModel", "Failed to fetch review for deletion: $exception")
                callback(false)
            }
    }


    // Fetch a single review
    fun fetchReview(postId: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                callback(document.data)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewViewModel", "Failed to fetch review: $exception")
                callback(null)
            }
    }

    // Update review
    fun updateReview(postId: String, updatedReview: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("posts").document(postId).update(updatedReview)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Apply filters for "Filter" feature in MapFragment
    fun applyFilters(filters: Map<String, String>, callback: (List<Map<String, Any>>) -> Unit) {
        var query: Query = firestore.collection("posts")
        filters["status"]?.let { status ->
            if (status != "All") {
                query = query.whereEqualTo("status", status)
            }
        }
        filters["category"]?.let { category ->
            if (category != "All") {
                query = query.whereEqualTo("category", category)
            }
        }
        query.get().addOnSuccessListener { snapshot ->
            val filteredReviews = snapshot.documents.mapNotNull { it.data }
            callback(filteredReviews)
        }.addOnFailureListener { exception ->
            Log.e("ReviewViewModel", "Error applying filters: $exception")
            callback(emptyList()) // Return an empty list on failure
        }
    }

    // Apply filters for "Search" feature in MainUserScreen



    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }







}