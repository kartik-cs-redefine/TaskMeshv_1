package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.MeshyAiService
import com.taskmesh.app.data.sharework.SavedRecording
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshyAiAnswerKeyTest {

    @Test
    fun `test dynamic answer key generation for general vocabulary`() {
        val recording = SavedRecording(
            id = "rec_test_1",
            worksheetId = "general_vocab_30d",
            worksheetTitle = "General Vocabulary",
            selectedGroupsLabel = "Day 1",
            selectedGroupIds = listOf("day_1"),
            recordingDate = System.currentTimeMillis(),
            durationSeconds = 60,
            filePath = "/test/path.m4a"
        )

        val answerKey = MeshyAiService.buildAnswerKey(TestMockContext(), recording)
        assertNotNull(answerKey)
        assertEquals("general_vocab_30d", answerKey?.worksheetId)
        assertEquals(7, answerKey?.questions?.size)
        assertTrue(answerKey?.questions?.first()?.correctAnswerText?.contains("Abundant") == true)
    }

    @Test
    fun `test valid review result JSON parsing and validation`() {
        val jsonPayload = """
            {
                "totalScore": 8,
                "maxScore": 10,
                "percentage": 80,
                "transcript": "Abundant means plentiful. Resilient means strong.",
                "overallSummary": "Great spoken attempt!",
                "questionsFeedback": [
                    {
                        "questionNumber": 1,
                        "promptText": "Abundant",
                        "isCorrect": true,
                        "score": 1,
                        "userSpokenText": "Abundant means plentiful",
                        "feedbackText": "Accurate definition",
                        "missingPoints": ""
                    }
                ]
            }
        """.trimIndent()

        val parsed = MeshyAiService.parseAndValidateReviewResult(jsonPayload)
        assertNotNull(parsed)
        assertEquals(8, parsed?.totalScore)
        assertEquals(10, parsed?.maxScore)
        assertEquals(80, parsed?.percentage)
        assertEquals(1, parsed?.questionsFeedback?.size)
        assertEquals(true, parsed?.questionsFeedback?.first()?.isCorrect)
    }

    @Test
    fun `test backend error json returns null and extracts message`() {
        val errorJson = """{"error": "Daily AI review limit reached (10 reviews/day). Try again tomorrow!"}"""
        
        val parsed = MeshyAiService.parseAndValidateReviewResult(errorJson)
        assertNull(parsed)

        val extracted = MeshyAiService.extractErrorMessage(errorJson, "Default Error")
        assertEquals("Daily AI review limit reached (10 reviews/day). Try again tomorrow!", extracted)
    }

    @Test
    fun `test missing totalScore returns null`() {
        val malformedJson = """{"maxScore": 10, "transcript": "Testing"}"""
        
        val parsed = MeshyAiService.parseAndValidateReviewResult(malformedJson)
        assertNull(parsed)
    }

    @Test
    fun `test invalid score bounds returns null`() {
        val invalidScoreJson = """{"totalScore": 15, "maxScore": 10, "questionsFeedback": []}"""
        
        val parsed = MeshyAiService.parseAndValidateReviewResult(invalidScoreJson)
        assertNull(parsed)
    }
}

private class TestMockContext : android.content.ContextWrapper(null)
