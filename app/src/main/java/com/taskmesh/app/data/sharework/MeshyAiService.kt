package com.taskmesh.app.data.sharework

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.taskmesh.app.data.DailySpeechDay
import com.taskmesh.app.data.OwspDatasetRepository
import com.taskmesh.app.data.VocabularyDataset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.TimeUnit

object MeshyAiService {

    // Configurable backend endpoint (NO API keys or secrets in APK!)
    var ENDPOINT_URL: String = "https://script.google.com/macros/s/AKfycbyySsRbreoJ0Vs7Yo4RaiLtgGUPK4jItsLXU-qyU8lzt6l5Pwit_r-14fQysEpal0y1/exec"

    const val MAX_AUDIO_FILE_SIZE_BYTES = 15 * 1024 * 1024L // 15 MB

    private fun logW(tag: String, msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {}
    }

    private fun logE(tag: String, msg: String, t: Throwable? = null) {
        try {
            if (t != null) Log.e(tag, msg, t) else Log.e(tag, msg)
        } catch (_: Throwable) {}
    }

    fun buildAnswerKey(context: Context, recording: SavedRecording): MeshyAnswerKey? {
        val storageManager = RecordingStorageManager(context)
        var groupIds = recording.selectedGroupIds
        if (groupIds.isEmpty()) {
            groupIds = storageManager.deriveGroupIdsFromLabel(recording.worksheetId, recording.selectedGroupsLabel)
        }

        if (groupIds.isEmpty()) return null

        val prompts = mutableListOf<MeshyQuestionPrompt>()

        if (recording.worksheetId == "general_vocab_30d") {
            val days = groupIds.mapNotNull {
                Regex("\\d+").find(it)?.value?.toIntOrNull()
            }.toSet()

            val selectedWords = VocabularyDataset.words
                .filter { days.contains(it.dayNumber) }
                .sortedWith(compareBy({ it.dayNumber }, { it.orderIndex }))

            selectedWords.forEachIndexed { idx, word ->
                prompts.add(
                    MeshyQuestionPrompt(
                        questionNumber = idx + 1,
                        promptText = word.word,
                        correctAnswerText = "${word.word} (${word.hindiMeaning}) — Pronunciation: /${word.pronunciation}/, Example: \"${word.exampleSentence}\", Tip: ${word.usageTip}",
                        hintText = word.hindiMeaning
                    )
                )
            }
        } else if (recording.worksheetId == "owsp_workbook_59d") {
            val dataset = OwspDatasetRepository.loadDataset(context)
            val days = groupIds.mapNotNull {
                Regex("\\d+").find(it)?.value?.toIntOrNull()
            }.toSet()

            val selectedQuestions = dataset.questions
                .filter { days.contains(it.dayNumber) }
                .sortedBy { it.dayNumber }

            selectedQuestions.forEachIndexed { idx, q ->
                val correctOption = q.options.find { it.isCorrect } ?: q.options.firstOrNull()
                val correctText = if (correctOption != null) {
                    "${correctOption.word} — ${correctOption.englishMeaning} (${correctOption.hindiMeaning}) — Example: \"${correctOption.exampleSentence}\""
                } else {
                    "Q${q.questionNumber} Correct Answer"
                }

                prompts.add(
                    MeshyQuestionPrompt(
                        questionNumber = idx + 1,
                        promptText = q.questionText,
                        correctAnswerText = correctText,
                        hintText = correctOption?.word ?: ""
                    )
                )
            }
        }

        if (prompts.isEmpty()) return null

        return MeshyAnswerKey(
            worksheetId = recording.worksheetId,
            worksheetTitle = recording.worksheetTitle,
            selectedGroupsLabel = recording.selectedGroupsLabel,
            questions = prompts
        )
    }

    fun extractErrorMessage(jsonString: String, defaultMsg: String): String {
        return try {
            val root = JSONObject(jsonString)
            when {
                root.has("error") -> root.getString("error")
                root.has("message") -> root.getString("message")
                else -> defaultMsg
            }
        } catch (_: Throwable) {
            defaultMsg
        }
    }

    fun parseAndValidateReviewResult(jsonString: String): MeshyReviewResult? {
        if (jsonString.isBlank()) return null
        return try {
            val root = JSONObject(jsonString)

            // If payload represents an error object, fail validation safely
            if (root.has("error") || root.has("message")) {
                logW("MeshyAiService", "JSON is an error object: ${root.optString("error")}")
                return null
            }

            if (!root.has("totalScore") || !root.has("maxScore")) {
                logE("MeshyAiService", "Missing totalScore or maxScore in JSON")
                return null
            }

            val totalScore = root.getInt("totalScore")
            val maxScore = root.getInt("maxScore")

            if (maxScore <= 0 || totalScore < 0 || totalScore > maxScore) {
                logE("MeshyAiService", "Invalid score range: $totalScore / $maxScore")
                return null
            }

            val calculatedPercentage = if (maxScore > 0) ((totalScore.toFloat() / maxScore) * 100).toInt() else 0
            val percentage = root.optInt("percentage", calculatedPercentage).coerceIn(0, 100)
            val transcript = root.optString("transcript", "Audio transcribed.")
            val overallSummary = root.optString("overallSummary", "Evaluation completed.")

            val feedbackList = mutableListOf<MeshyQuestionFeedback>()
            if (root.has("questionsFeedback")) {
                val feedbackArray = root.getJSONArray("questionsFeedback")
                for (i in 0 until feedbackArray.length()) {
                    val qObj = feedbackArray.getJSONObject(i)
                    feedbackList.add(
                        MeshyQuestionFeedback(
                            questionNumber = qObj.optInt("questionNumber", i + 1),
                            promptText = qObj.optString("promptText", "Question ${i + 1}"),
                            isCorrect = qObj.optBoolean("isCorrect", false),
                            score = qObj.optInt("score", if (qObj.optBoolean("isCorrect", false)) 1 else 0),
                            userSpokenText = qObj.optString("userSpokenText", ""),
                            feedbackText = qObj.optString("feedbackText", ""),
                            missingPoints = qObj.optString("missingPoints", "")
                        )
                    )
                }
            }

            MeshyReviewResult(
                totalScore = totalScore,
                maxScore = maxScore,
                percentage = percentage,
                transcript = transcript,
                overallSummary = overallSummary,
                questionsFeedback = feedbackList
            )
        } catch (e: Throwable) {
            logE("MeshyAiService", "JSON parsing exception", e)
            null
        }
    }

    suspend fun reviewRecording(
        context: Context,
        recording: SavedRecording,
        onResult: (Boolean, MeshyReviewResult?, String?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            // 1. Check cached result first
            if (!recording.reviewResultJson.isNullOrBlank()) {
                val cached = parseAndValidateReviewResult(recording.reviewResultJson)
                if (cached != null) {
                    withContext(Dispatchers.Main) {
                        onResult(true, cached, null)
                    }
                    return@withContext
                }
            }

            // 2. File Validation
            val audioFile = File(recording.filePath)
            if (!audioFile.exists() || audioFile.length() == 0L) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Audio file does not exist or is empty.")
                }
                return@withContext
            }

            if (audioFile.length() > MAX_AUDIO_FILE_SIZE_BYTES) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Audio file exceeds maximum allowed size (15 MB).")
                }
                return@withContext
            }

            // 3. Endpoint Validation
            if (ENDPOINT_URL.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult(
                        false,
                        null,
                        "Meshy AI backend endpoint is not configured. Please set a secure server proxy URL in MeshyAiService.ENDPOINT_URL."
                    )
                }
                return@withContext
            }

            // 4. Build Dynamic Answer Key
            val answerKey = buildAnswerKey(context, recording)
            if (answerKey == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Could not reconstruct answer key for this recording.")
                }
                return@withContext
            }

            // 5. Fetch Firebase ID token if user is signed in
            var idToken: String? = null
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val tokenTask = currentUser.getIdToken(true)
                    val result = Tasks.await(tokenTask, 10, TimeUnit.SECONDS)
                    idToken = result.token
                } catch (e: Exception) {
                    logW("MeshyAiService", "Failed to fetch Firebase ID token: ${e.localizedMessage}")
                }
            }

            // 6. Construct JSON Payload
            val answerKeyJson = JSONObject().apply {
                put("worksheetId", answerKey.worksheetId)
                put("worksheetTitle", answerKey.worksheetTitle)
                put("selectedGroupsLabel", answerKey.selectedGroupsLabel)
                val qArray = JSONArray()
                answerKey.questions.forEach { q ->
                    qArray.put(JSONObject().apply {
                        put("questionNumber", q.questionNumber)
                        put("promptText", q.promptText)
                        put("correctAnswerText", q.correctAnswerText)
                        put("hintText", q.hintText)
                    })
                }
                put("questions", qArray)
            }

            val audioBytes = audioFile.readBytes()
            val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

            val payload = JSONObject().apply {
                put("action", "evaluateShareWork") // Share Work existing flow
                put("recordingId", recording.id)
                put("mimeType", "audio/m4a")
                put("audioBase64", audioBase64)
                put("answerKey", answerKeyJson)
                if (!idToken.isNullOrBlank()) {
                    put("idToken", idToken)
                }
            }

            // 7. Perform HTTP Request with bounded retries and 60s read timeout
            var lastError = "Network request failed."
            val maxRetries = 1

            for (attempt in 0..maxRetries) {
                try {
                    val url = URL(ENDPOINT_URL)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 20000
                        readTimeout = 60000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        if (!idToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $idToken")
                        }
                    }

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val responseCode = conn.responseCode
                    val responseText = if (responseCode in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    }

                    if (responseCode in 200..299) {
                        val reviewResult = parseAndValidateReviewResult(responseText)
                        if (reviewResult != null) {
                            // Cache valid result in metadata
                            val updatedRecording = recording.copy(reviewResultJson = responseText)
                            RecordingStorageManager(context).saveRecordingMetadata(updatedRecording)

                            withContext(Dispatchers.Main) {
                                onResult(true, reviewResult, null)
                            }
                            return@withContext
                        } else {
                            val errMsg = extractErrorMessage(responseText, "Invalid evaluation JSON schema returned by server.")
                            lastError = errMsg
                        }
                    } else {
                        val errMsg = extractErrorMessage(responseText, "Server returned HTTP $responseCode")
                        lastError = errMsg
                    }
                } catch (e: SocketTimeoutException) {
                    lastError = "Request timed out while evaluating audio with AI. Please tap Retry."
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: "Connection failed."
                }
            }

            withContext(Dispatchers.Main) {
                onResult(false, null, lastError)
            }
        }
    }

    suspend fun transcribeSpeechAudio(
        context: Context,
        recording: SavedRecording,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val audioFile = File(recording.filePath)
            if (!audioFile.exists() || audioFile.length() == 0L) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Audio file does not exist or is empty.")
                }
                return@withContext
            }

            if (audioFile.length() > MAX_AUDIO_FILE_SIZE_BYTES) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Audio file exceeds maximum allowed size (15 MB).")
                }
                return@withContext
            }

            if (ENDPOINT_URL.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult(
                        false,
                        null,
                        "Meshy AI backend endpoint is not configured. Please set a valid URL in MeshyAiService.ENDPOINT_URL."
                    )
                }
                return@withContext
            }

            var idToken: String? = null
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val tokenTask = currentUser.getIdToken(true)
                    val result = Tasks.await(tokenTask, 10, TimeUnit.SECONDS)
                    idToken = result.token
                } catch (e: Exception) {
                    logW("MeshyAiService", "Failed to fetch Firebase ID token: ${e.localizedMessage}")
                }
            }

            val audioBytes = audioFile.readBytes()
            val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

            val payload = JSONObject().apply {
                put("action", "transcribe")
                put("recordingId", recording.id)
                put("mimeType", "audio/m4a")
                put("audioBase64", audioBase64)
                if (!idToken.isNullOrBlank()) {
                    put("idToken", idToken)
                }
            }

            var lastError = "Network request failed."
            val maxRetries = 1

            for (attempt in 0..maxRetries) {
                try {
                    val url = URL(ENDPOINT_URL)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 20000
                        readTimeout = 60000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        if (!idToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $idToken")
                        }
                    }

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val responseCode = conn.responseCode
                    val responseText = if (responseCode in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    }

                    if (responseCode in 200..299) {
                        val root = JSONObject(responseText)
                        if (root.has("transcript")) {
                            val transcriptText = root.getString("transcript")
                            withContext(Dispatchers.Main) {
                                onResult(true, transcriptText, null)
                            }
                            return@withContext
                        } else {
                            val errMsg = extractErrorMessage(responseText, "Invalid transcription response schema from server.")
                            lastError = errMsg
                        }
                    } else {
                        val errMsg = extractErrorMessage(responseText, "Server returned HTTP $responseCode")
                        lastError = errMsg
                    }
                } catch (e: SocketTimeoutException) {
                    lastError = "Request timed out while transcribing audio. Please tap Retry."
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: "Connection failed."
                }
            }

            withContext(Dispatchers.Main) {
                onResult(false, null, lastError)
            }
        }
    }

    suspend fun evaluateDailySpeechTranscript(
        context: Context,
        recording: SavedRecording,
        confirmedTranscript: String,
        dayData: DailySpeechDay,
        onResult: (Boolean, MeshyReviewResult?, String?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            if (confirmedTranscript.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult(false, null, "Transcript cannot be empty for evaluation.")
                }
                return@withContext
            }

            if (ENDPOINT_URL.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult(
                        false,
                        null,
                        "Meshy AI backend endpoint is not configured. Please set a valid URL in MeshyAiService.ENDPOINT_URL."
                    )
                }
                return@withContext
            }

            var idToken: String? = null
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val tokenTask = currentUser.getIdToken(true)
                    val result = Tasks.await(tokenTask, 10, TimeUnit.SECONDS)
                    idToken = result.token
                } catch (e: Exception) {
                    logW("MeshyAiService", "Failed to fetch Firebase ID token: ${e.localizedMessage}")
                }
            }

            val studyContentObj = JSONObject().apply {
                put("dayNumber", dayData.dayNumber)
                put("title", dayData.title)
                put("unit", dayData.unit)
                put("summary", dayData.summary)
                put("content", dayData.content)
            }

            val payload = JSONObject().apply {
                put("action", "evaluateDailySpeech")
                put("recordingId", recording.id)
                put("dayNumber", dayData.dayNumber)
                put("topicTitle", dayData.title)
                put("confirmedTranscript", confirmedTranscript)
                put("authoritativeStudyContent", studyContentObj)
                if (!idToken.isNullOrBlank()) {
                    put("idToken", idToken)
                }
            }

            var lastError = "Network request failed."
            val maxRetries = 1

            for (attempt in 0..maxRetries) {
                try {
                    val url = URL(ENDPOINT_URL)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 20000
                        readTimeout = 60000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        if (!idToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $idToken")
                        }
                    }

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val responseCode = conn.responseCode
                    val responseText = if (responseCode in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    }

                    if (responseCode in 200..299) {
                        val reviewResult = parseAndValidateReviewResult(responseText)
                        if (reviewResult != null) {
                            val updatedRecording = recording.copy(reviewResultJson = responseText)
                            RecordingStorageManager(context).saveRecordingMetadata(updatedRecording)

                            withContext(Dispatchers.Main) {
                                onResult(true, reviewResult, null)
                            }
                            return@withContext
                        } else {
                            val errMsg = extractErrorMessage(responseText, "Invalid evaluation JSON schema returned by server.")
                            lastError = errMsg
                        }
                    } else {
                        val errMsg = extractErrorMessage(responseText, "Server returned HTTP $responseCode")
                        lastError = errMsg
                    }
                } catch (e: SocketTimeoutException) {
                    lastError = "Request timed out while evaluating audio with AI. Please tap Retry."
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: "Connection failed."
                }
            }

            withContext(Dispatchers.Main) {
                onResult(false, null, lastError)
            }
        }
    }
}
