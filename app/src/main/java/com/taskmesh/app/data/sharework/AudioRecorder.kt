package com.taskmesh.app.data.sharework

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var startTimeMillis: Long = 0L
    private var isRecordingActive = false

    fun isRecording(): Boolean = isRecordingActive

    fun startRecording(outputFile: File): Boolean {
        if (isRecordingActive) return false

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(44100)
            recorder.setAudioEncodingBitRate(96000)
            recorder.setOutputFile(outputFile.absolutePath)

            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            startTimeMillis = System.currentTimeMillis()
            isRecordingActive = true
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            release()
            return false
        }
    }

    fun stopRecording(): Int {
        if (!isRecordingActive) return 0
        val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(1)

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recorder", e)
        } finally {
            release()
        }
        return durationSeconds
    }

    fun release() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error releasing recorder", e)
        } finally {
            mediaRecorder = null
            isRecordingActive = false
        }
    }
}
