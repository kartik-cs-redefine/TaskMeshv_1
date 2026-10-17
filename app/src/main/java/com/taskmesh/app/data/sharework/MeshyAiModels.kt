package com.taskmesh.app.data.sharework

data class MeshyQuestionPrompt(
    val questionNumber: Int,
    val promptText: String,
    val correctAnswerText: String,
    val hintText: String = ""
)

data class MeshyAnswerKey(
    val worksheetId: String,
    val worksheetTitle: String,
    val selectedGroupsLabel: String,
    val questions: List<MeshyQuestionPrompt>
)

data class MeshyQuestionFeedback(
    val questionNumber: Int,
    val promptText: String,
    val isCorrect: Boolean,
    val score: Int,
    val userSpokenText: String,
    val feedbackText: String,
    val missingPoints: String
)

data class MeshyReviewResult(
    val totalScore: Int,
    val maxScore: Int,
    val percentage: Int,
    val transcript: String,
    val overallSummary: String,
    val questionsFeedback: List<MeshyQuestionFeedback>
)
