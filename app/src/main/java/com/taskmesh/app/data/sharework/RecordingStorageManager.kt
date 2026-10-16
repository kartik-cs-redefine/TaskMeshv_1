package com.taskmesh.app.data.sharework

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingStorageManager(private val context: Context) {

    private fun getRecordingsDir(): File {
        val dir = context.getExternalFilesDir("share_work_recordings")
            ?: File(context.filesDir, "share_work_recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getMetadataFile(): File {
        return File(getRecordingsDir(), "recordings_metadata.json")
    }

    fun sanitizeFilenamePart(text: String): String {
        return text.replace(Regex("[^a-zA-Z0-9_-]"), "").ifBlank { "Worksheet" }
    }

    fun createRecordingFile(worksheetTitle: String, groupLabels: List<String>, isTemp: Boolean = false): File {
        val dir = getRecordingsDir()
        val cleanTitle = sanitizeFilenamePart(worksheetTitle)
        val cleanGroups = groupLabels.take(3).joinToString("_") { sanitizeFilenamePart(it) }
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val prefix = if (isTemp) "temp_" else ""
        var filename = "TaskMesh_${prefix}${cleanTitle}_${cleanGroups}_${dateStr}.m4a"
        var targetFile = File(dir, filename)

        var suffix = 1
        while (targetFile.exists()) {
            filename = "TaskMesh_${prefix}${cleanTitle}_${cleanGroups}_${dateStr}_$suffix.m4a"
            targetFile = File(dir, filename)
            suffix++
        }

        return targetFile
    }

    fun saveRecordingMetadata(recording: SavedRecording) {
        val allRecordings = getAllSavedRecordings().toMutableList()
        allRecordings.removeAll { it.id == recording.id }
        allRecordings.add(0, recording)

        val jsonArray = JSONArray()
        for (item in allRecordings) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("worksheetId", item.worksheetId)
                put("worksheetTitle", item.worksheetTitle)
                put("selectedGroupsLabel", item.selectedGroupsLabel)
                put("recordingDate", item.recordingDate)
                put("durationSeconds", item.durationSeconds)
                put("filePath", item.filePath)
                put("fileSizeBytes", item.fileSizeBytes)
                put("isTemporary", item.isTemporary)
            }
            jsonArray.put(obj)
        }

        try {
            getMetadataFile().writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e("RecordingStorage", "Failed to write recordings metadata", e)
        }
    }

    fun getAllSavedRecordings(): List<SavedRecording> {
        val metaFile = getMetadataFile()
        if (!metaFile.exists()) return emptyList()

        return try {
            val jsonText = metaFile.readText()
            val jsonArray = JSONArray(jsonText)
            val list = mutableListOf<SavedRecording>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val path = obj.getString("filePath")
                val audioFile = File(path)

                // Only include if actual audio file exists and is not empty
                if (audioFile.exists() && audioFile.length() > 0L) {
                    list.add(
                        SavedRecording(
                            id = obj.getString("id"),
                            worksheetId = obj.getString("worksheetId"),
                            worksheetTitle = obj.getString("worksheetTitle"),
                            selectedGroupsLabel = obj.getString("selectedGroupsLabel"),
                            recordingDate = obj.getLong("recordingDate"),
                            durationSeconds = obj.getInt("durationSeconds"),
                            filePath = path,
                            fileSizeBytes = audioFile.length(),
                            isTemporary = obj.optBoolean("isTemporary", false)
                        )
                    )
                }
            }
            list.sortedByDescending { it.recordingDate }
        } catch (e: Exception) {
            Log.e("RecordingStorage", "Error reading metadata file", e)
            emptyList()
        }
    }

    fun deleteRecording(recordingId: String): Boolean {
        val allRecordings = getAllSavedRecordings()
        val target = allRecordings.find { it.id == recordingId } ?: return false

        try {
            val file = File(target.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("RecordingStorage", "Error deleting file", e)
        }

        val updatedList = allRecordings.filter { it.id != recordingId }
        val jsonArray = JSONArray()
        for (item in updatedList) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("worksheetId", item.worksheetId)
                put("worksheetTitle", item.worksheetTitle)
                put("selectedGroupsLabel", item.selectedGroupsLabel)
                put("recordingDate", item.recordingDate)
                put("durationSeconds", item.durationSeconds)
                put("filePath", item.filePath)
                put("fileSizeBytes", item.fileSizeBytes)
                put("isTemporary", item.isTemporary)
            }
            jsonArray.put(obj)
        }

        return try {
            getMetadataFile().writeText(jsonArray.toString(2))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getContentUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
