package com.taskmesh.app.data.sharework

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object ShareWorkSession {
    var selectedWorksheet by mutableStateOf<WorksheetItem?>(null)
    var selectedGroups by mutableStateOf<Set<WorksheetGroup>>(emptySet())
    var orderedQuestions by mutableStateOf<List<WorksheetQuestion>>(emptyList())

    var isRecording by mutableStateOf(false)
    var elapsedSeconds by mutableIntStateOf(0)
    var activeFile by mutableStateOf<File?>(null)

    private var audioRecorder: AudioRecorder? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun startSession(
        context: Context,
        worksheet: WorksheetItem,
        groups: Set<WorksheetGroup>,
        questions: List<WorksheetQuestion>,
        onStarted: (Boolean, String?) -> Unit
    ) {
        if (isRecording) {
            onStarted(false, "Recording is already active")
            return
        }

        selectedWorksheet = worksheet
        selectedGroups = groups
        orderedQuestions = questions

        val storageManager = RecordingStorageManager(context)
        val groupLabels = groups.sortedBy { it.groupNumber }.map { it.groupLabel }
        val file = storageManager.createRecordingFile(worksheet.title, groupLabels, isTemp = false)
        activeFile = file

        val recorder = AudioRecorder(context)
        val success = recorder.startRecording(file)
        if (success) {
            audioRecorder = recorder
            isRecording = true
            elapsedSeconds = 0

            timerJob?.cancel()
            timerJob = scope.launch {
                while (isActive && isRecording) {
                    delay(1000L)
                    elapsedSeconds++
                }
            }
            onStarted(true, null)
        } else {
            onStarted(false, "Could not start audio recorder")
        }
    }

    fun stopSession(context: Context): SavedRecording? {
        if (!isRecording) return null

        val recorder = audioRecorder
        val duration = recorder?.stopRecording() ?: elapsedSeconds.coerceAtLeast(1)
        timerJob?.cancel()
        timerJob = null

        val file = activeFile
        val worksheet = selectedWorksheet
        val groups = selectedGroups

        isRecording = false
        audioRecorder = null

        if (file != null && file.exists() && file.length() > 0L && worksheet != null) {
            val storageManager = RecordingStorageManager(context)
            val groupsLabel = groups.sortedBy { it.groupNumber }.joinToString(", ") { it.groupLabel }
            val recording = SavedRecording(
                id = "rec_${System.currentTimeMillis()}",
                worksheetId = worksheet.id,
                worksheetTitle = worksheet.title,
                selectedGroupsLabel = groupsLabel,
                selectedGroupIds = groups.sortedBy { it.groupNumber }.map { it.id },
                recordingDate = System.currentTimeMillis(),
                durationSeconds = duration,
                filePath = file.absolutePath,
                fileSizeBytes = file.length(),
                isTemporary = false
            )
            storageManager.saveRecordingMetadata(recording)
            return recording
        }
        return null
    }

    fun cancelAndDiscardSession() {
        timerJob?.cancel()
        timerJob = null

        audioRecorder?.release()
        audioRecorder = null

        activeFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }

        activeFile = null
        isRecording = false
        elapsedSeconds = 0
    }
}
