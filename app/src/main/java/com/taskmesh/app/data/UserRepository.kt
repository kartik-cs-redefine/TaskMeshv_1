package com.taskmesh.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

open class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    open fun getUserProfile(uid: String, onResult: (UserProfile?) -> Unit) {
        if (uid.isBlank()) {
            onResult(null)
            return
        }
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = doc.toObject(UserProfile::class.java)
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    open fun saveUserProfile(profile: UserProfile, onResult: (Boolean, String?) -> Unit) {
        if (profile.uid.isBlank()) {
            onResult(false, "Invalid user ID")
            return
        }
        db.collection("users").document(profile.uid)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Failed to save user profile")
            }
    }

    open fun updateDisplayName(uid: String, newName: String, onResult: (Boolean, String?) -> Unit) {
        if (uid.isBlank() || newName.isBlank()) {
            onResult(false, "Name cannot be empty")
            return
        }

        val updates = mapOf(
            "displayName" to newName.trim(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user != null && user.uid == uid) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newName.trim())
                        .build()
                    user.updateProfile(profileUpdates)
                }
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Failed to update profile name")
            }
    }

    open fun syncLearningProgress(
        uid: String,
        generalLearned: Int,
        owspLearned: Int,
        bookmarksCount: Int,
        day1: Int,
        day2: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (uid.isBlank()) {
            onResult(false, "Invalid UID")
            return
        }

        val updates = mapOf(
            "generalLearnedCount" to generalLearned,
            "owspLearnedCount" to owspLearned,
            "bookmarksCount" to bookmarksCount,
            "generalDay" to day1,
            "owspDay" to day2,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Failed to sync learning progress")
            }
    }
}
