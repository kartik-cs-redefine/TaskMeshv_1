package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.SavedRecording
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareWorkStorageTest {

    @Test
    fun `test filename sanitization logic`() {
        val rawName = "General Vocabulary @ 123! #$"
        val sanitized = rawName.replace(Regex("[^a-zA-Z0-9_-]"), "")
        assertEquals("GeneralVocabulary123", sanitized)
    }

    @Test
    fun `test saved recording metadata model`() {
        val recording = SavedRecording(
            id = "rec_001",
            worksheetId = "general_vocab_30d",
            worksheetTitle = "General Vocabulary",
            selectedGroupsLabel = "Day 8, Day 9",
            recordingDate = 1700000000000L,
            durationSeconds = 125,
            filePath = "/sdcard/test.m4a",
            fileSizeBytes = 2048500L
        )

        assertEquals("rec_001", recording.id)
        assertEquals("General Vocabulary", recording.worksheetTitle)
        assertEquals("Day 8, Day 9", recording.selectedGroupsLabel)
        assertEquals(125, recording.durationSeconds)
        assertEquals(2048500L, recording.fileSizeBytes)
    }
}
