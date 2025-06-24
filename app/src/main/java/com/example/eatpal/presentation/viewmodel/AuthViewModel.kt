package com.example.eatpal.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            Log.d(TAG, "Auth state changed: User ${if (_currentUser.value != null) "logged in" else "logged out"}")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Attempting login for $email")

                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
                Log.d(TAG, "Login successful for $email")
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, height: Float, weight: Float) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Attempting registration for $email")

                // Create user
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Update profile with name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                result.user?.updateProfile(profileUpdates)?.await()

                // Store additional user info in Firestore
                result.user?.uid?.let { userId ->
                    saveUserData(userId, name, email, height, weight)
                }

                _authState.value = AuthState.Success
                Log.d(TAG, "Registration successful for $email")
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private fun saveUserData(userId: String, name: String, email: String, height: Float, weight: Float) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "height" to height,
            "weight" to weight,
            "dailyCalorieGoal" to 2039, // Default value
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully for $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving user data", e)
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
        Log.d(TAG, "User logged out")
    }

    fun validateEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun validatePassword(password: String): Boolean {
        // At least 6 characters
        return password.length >= 6
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}