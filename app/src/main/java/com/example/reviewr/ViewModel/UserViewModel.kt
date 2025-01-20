package com.example.reviewr.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reviewr.Data.AppDatabase
import com.example.reviewr.Data.UserEntity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel (application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val database = AppDatabase.getInstance(application.applicationContext)
    private val userDao = database.userDao()

    init {
        Log.d("DatabaseInit", "Database: $database, UserDao: $userDao")
    }

    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Failure(val message: String?) : RegistrationResult()
    }

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
                    saveUserDetailsToFirestore(userId, username, firstName, lastName, email, age, password, result)
                } else {
                    result.value = RegistrationResult.Failure(task.exception?.message)
                }
            }

        return result
    }

    private fun saveUserDetailsToFirestore(
        userId: String,
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        age: String,
        password: String,
        result: MutableLiveData<RegistrationResult>
    ) {
        val user = hashMapOf(
            "userId" to userId,
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "age" to age,
            "password" to password
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                result.value = RegistrationResult.Success
            }
            .addOnFailureListener { e ->
                result.value = RegistrationResult.Failure(e.message)
            }
    }

    fun getCurrentUser() = auth.currentUser

    sealed class LoginResult {
        object Success : LoginResult()
        data class Failure(val message: String?) : LoginResult()
    }

    fun login(email: String, password: String): LiveData<LoginResult> {
        val loginResult = MutableLiveData<LoginResult>()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Fetch user data from Firestore
                        firestore.collection("users").document(firebaseUser.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userEntity = UserEntity(
                                        userId = firebaseUser.uid,
                                        username = document.getString("username") ?: "Unknown",
                                        firstName = document.getString("firstName") ?: "Unknown",
                                        lastName = document.getString("lastName") ?: "Unknown",
                                        email = document.getString("email") ?: email,
                                        age = document.getString("age") ?: "Unknown"
                                    )

                                    // Save user data to Room on a background thread
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            userDao.insertUser(userEntity)
                                        } catch (e: Exception) {
                                            Log.e("UserViewModel", "Database insert failed: ${e.message}")
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserViewModel", "Failed to fetch user data: ${e.message}")
                            }
                    }
                    loginResult.value = LoginResult.Success
                } else {
                    loginResult.value = LoginResult.Failure(task.exception?.message)
                }
            }
        return loginResult
    }



    fun logout() {
        auth.signOut()
    }

    fun fetchUserDetails(userId: String, callback: (Map<String, Any>) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.data ?: emptyMap()
                    // Return Firebase data
                    callback(userData)
                    // Optionally update cached data in Room
                    val userEntity = UserEntity(
                        userId = userData["userId"] as String,
                        username = userData["username"] as? String ?: "Unknown",
                        firstName = userData["firstName"] as? String ?: "Unknown",
                        lastName = userData["lastName"] as? String ?: "Unknown",
                        email = userData["email"] as? String ?: "Unknown",
                        age = userData["age"] as? String ?: "Unknown"
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            userDao.insertUser(userEntity) // Save to Room
                        } catch (e: Exception) {
                            Log.e("UserViewModel", "Error inserting user: ${e.message}")
                        }
                    }
                } else {
                    callback(emptyMap()) // No data in Firebase
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Failed to fetch user data from Firestore: ${exception.message}")
                // Fallback to Room on Firebase failure
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val cachedUser = userDao.getUser(userId)
                        if (cachedUser != null) {
                            val cachedData = mapOf(
                                "userId" to cachedUser.userId,
                                "username" to cachedUser.username,
                                "firstName" to cachedUser.firstName,
                                "lastName" to cachedUser.lastName,
                                "email" to cachedUser.email,
                                "age" to cachedUser.age
                            )
                            callback(cachedData) // Return cached data
                        } else {
                            callback(emptyMap()) // No cached data available
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error fetching cached user: ${e.message}")
                        callback(emptyMap())
                    }
                }
            }
    }




    fun fetchReviewCount(userId: String, callback: (Int) -> Unit) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    fun fetchCommentCount(userId: String, callback: (Int) -> Unit) {
        firestore.collection("comments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    fun updateEmail(newEmail: String, password: String, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", password)

        user?.reauthenticate(credential)
            ?.addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d("EditPersonalDetails", "Reauthentication successful")

                    // Send verification email
                    user.verifyBeforeUpdateEmail(newEmail)
                        .addOnCompleteListener { verificationTask ->
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




    fun updatePassword(currentPassword: String, newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)

        // Reauthenticate the user
        user?.reauthenticate(credential)
            ?.addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Proceed with password update
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
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



    fun updateUserDetails(userId: String, updatedData: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("users").document(userId)
            .update(updatedData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateReviewsAndComments(userId: String, updatedData: Map<String, Any>) {
        val updatedUsername = updatedData["username"] as String
        // Update reviews
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("username", updatedUsername)
                }
            }
        // Update comments
        firestore.collection("comments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("username", updatedUsername)
                }
            }
    }


}