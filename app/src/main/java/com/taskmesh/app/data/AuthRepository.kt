package com.taskmesh.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

open class AuthRepository(
    private val userRepository: UserRepository = UserRepository()
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    open fun getCurrentUser(): FirebaseUser? = auth.currentUser

    open fun isLoggedIn(): Boolean = auth.currentUser != null

    open fun signInWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email and password cannot be empty.")
            return
        }

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        userRepository.getUserProfile(user.uid) { existingProfile ->
                            if (existingProfile == null) {
                                val newProfile = UserProfile(
                                    uid = user.uid,
                                    displayName = user.displayName ?: email.substringBefore("@"),
                                    email = user.email ?: email,
                                    providerId = "email"
                                )
                                userRepository.saveUserProfile(newProfile) { _, _ -> }
                            }
                        }
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Sign-in failed.")
                }
            }
    }

    open fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            onResult(false, "All fields are required.")
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.trim())
                            .build()
                        user.updateProfile(profileUpdates)

                        val newProfile = UserProfile(
                            uid = user.uid,
                            displayName = displayName.trim(),
                            email = user.email ?: email.trim(),
                            providerId = "email"
                        )
                        userRepository.saveUserProfile(newProfile) { success, msg ->
                            onResult(success, msg)
                        }
                    } else {
                        onResult(true, null)
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Registration failed.")
                }
            }
    }

    open fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email address.")
            return
        }

        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent. Please check your inbox.")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                }
            }
    }

    open fun signInWithGoogleIdToken(
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        userRepository.getUserProfile(user.uid) { existingProfile ->
                            if (existingProfile == null) {
                                val newProfile = UserProfile(
                                    uid = user.uid,
                                    displayName = user.displayName ?: "User",
                                    email = user.email ?: "",
                                    providerId = "google.com"
                                )
                                userRepository.saveUserProfile(newProfile) { _, _ -> }
                            }
                        }
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Google sign-in failed.")
                }
            }
    }

    open fun signOut() {
        auth.signOut()
    }
}
