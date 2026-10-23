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
    fun `test day 2 daily speech evaluation response parsing and rubric validation`() {
        val day2EvaluationJson = """
            {
                "totalScore": 88,
                "maxScore": 100,
                "percentage": 88,
                "transcript": "General communication is informal, whereas technical communication is formal and audience-focused.",
                "overallSummary": "Excellent spoken explanation for Day 2: General vs Technical Communication.",
                "scores": {
                    "contentAccuracyScore": 27,
                    "contentAccuracyMax": 30,
                    "vocabularyScore": 22,
                    "vocabularyMax": 25,
                    "languageQualityScore": 22,
                    "languageQualityMax": 25,
                    "completenessScore": 17,
                    "completenessMax": 20,
                    "totalScore": 88,
                    "maxScore": 100,
                    "percentage": 88
                },
                "strengths": ["Clear comparison of general vs technical communication"],
                "improvements": ["Elaborate on workplace benefits"],
                "questionsFeedback": [
                    {
                        "questionNumber": 1,
                        "promptText": "How is General Communication different from Technical Communication?",
                        "isCorrect": true,
                        "score": 1,
                        "userSpokenText": "General communication is informal, whereas technical communication is formal...",
                        "feedbackText": "Accurate comparison",
                        "missingPoints": ""
                    }
                ]
            }
        """.trimIndent()

        val parsed = MeshyAiService.parseAndValidateReviewResult(day2EvaluationJson)
        assertNotNull(parsed)
        assertEquals(88, parsed?.totalScore)
        assertEquals(100, parsed?.maxScore)
        assertEquals(88, parsed?.percentage)
        assertEquals(1, parsed?.questionsFeedback?.size)
    }

    @Test
    fun `test off topic transcript sample audio receives low score and 0 content accuracy`() {
        val offTopicResultJson = """
            {
                "totalScore": 8,
                "maxScore": 100,
                "percentage": 8,
                "transcript": "This is sample audio",
                "overallSummary": "The spoken response is off-topic or contains test audio. Content Accuracy and Completeness score 0/50.",
                "scores": {
                    "contentAccuracyScore": 0,
                    "contentAccuracyMax": 30,
                    "vocabularyScore": 3,
                    "vocabularyMax": 25,
                    "languageQualityScore": 5,
                    "languageQualityMax": 25,
                    "completenessScore": 0,
                    "completenessMax": 20,
                    "totalScore": 8,
                    "maxScore": 100,
                    "percentage": 8
                },
                "strengths": ["Spoken audio recorded"],
                "improvements": ["Please record a response addressing the topic"],
                "questionsFeedback": []
            }
        """.trimIndent()

        val parsed = MeshyAiService.parseAndValidateReviewResult(offTopicResultJson)
        assertNotNull(parsed)
        assertEquals(8, parsed?.totalScore)
        assertEquals(100, parsed?.maxScore)
        assertEquals("This is sample audio", parsed?.transcript)
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
