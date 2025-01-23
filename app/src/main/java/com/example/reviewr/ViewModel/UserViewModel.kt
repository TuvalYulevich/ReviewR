package com.example.reviewr.ViewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.reviewr.Data.AppDatabase
import com.example.reviewr.Data.UserEntity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import okhttp3.Callback
import okhttp3.Response
import java.security.MessageDigest

// User ViewModel interacts with all of the databases offline and online in the actions regarding to users
class UserViewModel (application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val database = AppDatabase.getInstance(application.applicationContext)
    private val userDao = database.userDao()


    init { Log.d("DatabaseInit", "Database: $database, UserDao: $userDao") } // Debugging statement to verify Database launch

    sealed class RegistrationResult { object Success : RegistrationResult() data class Failure(val message: String?) : RegistrationResult() }

    // Updating user information in Room
    fun updateUserInRoom(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.insertUser(user)
                Log.d("UserViewModel", "User updated in Room successfully: $user")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to update user in Room: ${e.message}")
            }
        }
    }

    // Upload profile picture
    fun uploadProfileImage(uri: Uri): LiveData<Pair<Boolean, String?>> {
        val uploadStatus = MutableLiveData<Pair<Boolean, String?>>()
        try {
            MediaManager.get().upload(uri)
                .option("folder", "profile_pictures/") // Image source in Cloudinary
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("UserViewModel", "Uploading image started...")
                    }
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        if (!imageUrl.isNullOrEmpty()) {
                            Log.d("UserViewModel", "Image uploaded successfully: $imageUrl")
                            uploadStatus.postValue(Pair(true, imageUrl))
                        } else {
                            Log.e("UserViewModel", "Image upload failed: No secure_url found")
                            uploadStatus.postValue(Pair(false, null))
                        }
                    }
                    override fun onError(requestId: String?, error: ErrorInfo) {
                        Log.e("UserViewModel", "Image upload failed: ${error.description}")
                        uploadStatus.postValue(Pair(false, error.description))
                    }
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo) {}
                }).dispatch()
        } catch (e: IllegalStateException) {
            Log.e("UserViewModel", "MediaManager not initialized: ${e.message}")
            uploadStatus.postValue(Pair(false, "MediaManager not initialized. Restart the app."))
        }
        return uploadStatus
    }

    // Delete profile picture
    fun deleteProfileImage(imageUrl: String, callback: ((Boolean) -> Unit)? = null) {
        // Verifying API credentials
        val CLOUD_NAME = "dm8sulfig"
        val API_KEY = "129181168733979"
        val API_SECRET = "uNaILxRogPyZ_FTQtnOWEQ-Tq5Y"

        // Extract the public ID from the URL
        val publicId = "profile_pictures/" + imageUrl.substringAfterLast("/").substringBeforeLast(".")
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

        val request = Request.Builder().url(requestUrl).post(requestBody).build()

        // Send the request
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

    // Register user
    fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        age: String,
        ): LiveData<RegistrationResult> {
        val result = MutableLiveData<RegistrationResult>()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDetailsToFirestore(userId, username, firstName, lastName, email, age, password, result) // Saving user data to Firestore in Firebase
                } else {
                    result.value = RegistrationResult.Failure(task.exception?.message)
                }
            }
        return result
    }

    // Save user details to fire store
    private fun saveUserDetailsToFirestore(
        userId: String,
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        age: String,
        password: String,
        result: MutableLiveData<RegistrationResult>) {
        val user = hashMapOf(
            "userId" to userId,
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "age" to age,
            "password" to password)
        firestore.collection("users").document(userId).set(user).addOnSuccessListener {
            result.value = RegistrationResult.Success
            }.addOnFailureListener{ e ->
            result.value = RegistrationResult.Failure(e.message)
        }
    }

    // Getting the current user that is logged in
    fun getCurrentUser() = auth.currentUser

    sealed class LoginResult { object Success : LoginResult()data class Failure(val message: String?) : LoginResult() }

    // Login user
    fun login(email: String, password: String): LiveData<LoginResult> {
        val loginResult = MutableLiveData<LoginResult>()
        auth.signInWithEmailAndPassword(email, password) // Firebase auth using E-Mail and passwrd
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserViewModel", "Login successful for email: $email")
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        Log.d("UserViewModel", "Fetching user data from Firestore for userId: ${firebaseUser.uid}")
                        firestore.collection("users").document(firebaseUser.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                // If login is successfull, save user data to Room.
                                if (document.exists()) {
                                    Log.d("UserViewModel", "User data fetched from Firestore: ${document.data}")
                                    val userEntity = UserEntity(
                                        userId = firebaseUser.uid,
                                        username = document.getString("username") ?: "Unknown",
                                        firstName = document.getString("firstName") ?: "Unknown",
                                        lastName = document.getString("lastName") ?: "Unknown",
                                        email = document.getString("email") ?: email,
                                        age = document.getString("age") ?: "Unknown",
                                        profileImageUrl = document.getString("profilePictureUrl") ?: "Unknown",
                                        password = document.getString("password") ?: password
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            Log.d("UserViewModel", "Inserting user into Room: $userEntity")
                                            userDao.insertUser(userEntity)
                                            Log.d("UserViewModel", "User inserted into Room successfully")
                                        } catch (e: Exception) {
                                            Log.e("UserViewModel", "Failed to insert user into Room: ${e.message}")
                                        }
                                    }
                                } else {
                                    Log.e("UserViewModel", "User document does not exist in Firestore")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserViewModel", "Failed to fetch user data from Firestore: ${e.message}")
                            }
                    }
                    loginResult.value = LoginResult.Success
                } else {
                    Log.e("UserViewModel", "Login failed: ${task.exception?.message}")
                    loginResult.value = LoginResult.Failure(task.exception?.message)
                }
            }
        return loginResult
    }

    // Logout user
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.deleteAllUsers() // Clear the Room database
                Log.d("UserViewModel", "All users deleted from Room database.")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to delete all users from Room: ${e.message}")
            }
        }
        auth.signOut() // Sign out the user from Firebase Authentication
        Log.d("UserViewModel", "User signed out.")
    }

    // Fetching user details
    fun fetchUserDetails(userId: String, callback: (Map<String, Any>) -> Unit) {
        Log.d("UserViewModel", "Fetching user details for userId: $userId")
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.data ?: emptyMap()
                    Log.d("UserViewModel", "User data fetched from Firestore: $userData")
                    callback(userData)
                    val userEntity = UserEntity(
                        userId = userData["userId"] as String,
                        username = userData["username"] as? String ?: "Unknown",
                        firstName = userData["firstName"] as? String ?: "Unknown",
                        lastName = userData["lastName"] as? String ?: "Unknown",
                        email = userData["email"] as? String ?: "Unknown",
                        age = userData["age"] as? String ?: "Unknown",
                        profileImageUrl = document.getString("profilePictureUrl") ?: "Unknown",
                        password = userData["password"] as? String ?: "" // Fetch password
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            Log.d("UserViewModel", "Caching user data in Room: $userEntity")
                            userDao.insertUser(userEntity)
                            Log.d("UserViewModel", "User cached in Room successfully")
                        } catch (e: Exception) {
                            Log.e("UserViewModel", "Failed to cache user in Room: ${e.message}")
                        }
                    }
                } else {
                    Log.e("UserViewModel", "User document does not exist in Firestore")
                    callback(emptyMap())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Failed to fetch user data from Firestore: ${exception.message}")
                // Fallback to Room
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val cachedUser = userDao.getUser(userId)
                        if (cachedUser != null) {
                            Log.d("UserViewModel", "Returning cached user from Room: $cachedUser")
                            val cachedData = mapOf(
                                "userId" to cachedUser.userId,
                                "username" to cachedUser.username,
                                "firstName" to cachedUser.firstName,
                                "lastName" to cachedUser.lastName,
                                "email" to cachedUser.email,
                                "age" to cachedUser.age,
                                "profileImageUrl" to (cachedUser.profileImageUrl ?: "Unknown"),
                                "password" to cachedUser.password
                            )
                            callback(cachedData)
                        } else {
                            Log.d("UserViewModel", "No cached user found in Room for userId: $userId")
                            callback(emptyMap())
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error fetching user from Room: ${e.message}")
                        callback(emptyMap())
                    }
                }
            }
    }

    // Fetching Review Count
    fun fetchReviewCount(userId: String, callback: (Int) -> Unit) {
        firestore.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    // Fetching Review Count
    fun fetchCommentCount(userId: String, callback: (Int) -> Unit) {
        firestore.collection("comments").whereEqualTo("userId", userId).get().addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    // Updating Email address
    fun updateEmail(newEmail: String, password: String, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", password)
        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d("EditPersonalDetails", "Reauthentication successful")
                    // Send verification email
                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Log.d("EditPersonalDetails", "Verification email sent to $newEmail")
                                callback(true, "Verification email sent. Please check your inbox.")
                            } else {
                                Log.e("EditPersonalDetails", "Failed to send verification email: ${verificationTask.exception?.message}")
                                callback(false, verificationTask.exception?.message)
                            }
                        }
                } else {
                    Log.e("EditPersonalDetails", "Reauthentication failed: ${authTask.exception?.message}")
                    callback(false, "Reauthentication failed: ${authTask.exception?.message}")
                }
            }
    }

    // Updating password
    fun updatePassword(currentPassword: String, newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
        // Reauthenticate the user
        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Proceed with password update in Firebase Authentication
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                val userId = user.uid
                                // Save the new password to Firestore
                                firestore.collection("users").document(userId).update("password", newPassword)
                                    .addOnSuccessListener {
                                        Log.d("UserViewModel", "Password updated in Firestore successfully.")
                                        // Save the new password to Room
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("UserViewModel", "Failed to update password in Firestore: ${e.message}")
                                    }

                                callback(true, null)
                            } else {
                                callback(false, updateTask.exception?.message)
                            }
                        }
                } else {
                    callback(false, authTask.exception?.message)
                }
            }
    }


    // Updating user data in live-data
    fun updateUserDetails(userId: String, updatedData: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("users").document(userId).update(updatedData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Updating comments and review amount in live-data
    fun updateReviewsAndComments(userId: String, updatedData: Map<String, Any>) {
        val updatedUsername = updatedData["username"] as String
        // Update reviews
        firestore.collection("posts").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("username", updatedUsername)
                }
            }
        // Update comments
        firestore.collection("comments").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("username", updatedUsername)
                }
            }
    }

}