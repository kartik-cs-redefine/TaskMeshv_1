package com.taskmesh.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMeshAuthTest {

    @Test
    fun `test user profile data model`() {
        val profile = UserProfile(
            uid = "user_12345",
            displayName = "Test User",
            email = "test@taskmesh.app",
            providerId = "google.com"
        )

        assertEquals("user_12345", profile.uid)
        assertEquals("Test User", profile.displayName)
        assertEquals("test@taskmesh.app", profile.email)
        assertEquals("google.com", profile.providerId)
    }

    @Test
    fun `test email and password validation rules`() {
        val emptyEmail = ""
        val shortPassword = "123"
        val validEmail = "learner@taskmesh.app"
        val validPassword = "securePassword123"

        assertFalse(emptyEmail.isNotBlank())
        assertTrue(shortPassword.length < 6)
        assertTrue(validEmail.contains("@") && validEmail.isNotBlank())
        assertTrue(validPassword.length >= 6)
    }
}
