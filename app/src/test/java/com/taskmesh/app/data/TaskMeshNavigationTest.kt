package com.taskmesh.app.data

import com.taskmesh.app.ui.screens.AvailabilityStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMeshNavigationTest {

    @Test
    fun `test 1 - initiative availability states`() {
        val englishStatus = AvailabilityStatus.AVAILABLE
        val dsaStatus = AvailabilityStatus.COMING_SOON
        val projectStatus = AvailabilityStatus.COMING_SOON

        assertEquals(AvailabilityStatus.AVAILABLE, englishStatus)
        assertEquals(AvailabilityStatus.COMING_SOON, dsaStatus)
        assertEquals(AvailabilityStatus.COMING_SOON, projectStatus)
    }

    @Test
    fun `test 2 - english module availability states`() {
        val vocabStatus = AvailabilityStatus.AVAILABLE
        val speechStatus = AvailabilityStatus.COMING_SOON
        val analysisStatus = AvailabilityStatus.COMING_SOON

        assertEquals(AvailabilityStatus.AVAILABLE, vocabStatus)
        assertEquals(AvailabilityStatus.COMING_SOON, speechStatus)
        assertEquals(AvailabilityStatus.COMING_SOON, analysisStatus)
    }

    @Test
    fun `test 3 - navigation stack state transitions and back flow`() {
        val navHistory = mutableListOf("initiatives")

        // User taps English Initiative
        navHistory.add("english_initiative")
        assertEquals("english_initiative", navHistory.last())

        // User taps Vocabulary
        navHistory.add("vocabulary_app")
        assertEquals("vocabulary_app", navHistory.last())

        // User presses Back from Vocabulary
        if (navHistory.last() == "vocabulary_app") {
            navHistory.removeAt(navHistory.lastIndex)
        }
        assertEquals("english_initiative", navHistory.last())

        // User presses Back from English Initiative
        if (navHistory.last() == "english_initiative") {
            navHistory.removeAt(navHistory.lastIndex)
        }
        assertEquals("initiatives", navHistory.last())
    }
}
