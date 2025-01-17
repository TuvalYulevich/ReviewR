package com.example.reviewr.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
        age: String
    ): LiveData<RegistrationResult> {
        val result = MutableLiveData<RegistrationResult>()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDetailsToFirestore(userId, username, firstName, lastName, email, age, result)
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
        result: MutableLiveData<RegistrationResult>
    ) {
        val user = hashMapOf(
            "userId" to userId,
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "age" to age
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
}
