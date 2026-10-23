package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.AndroidOnDeviceTranscriber
import com.taskmesh.app.data.sharework.LocalTranscriptionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OnDeviceSpeechTranscriberTest {

    private class DummyContext : android.content.ContextWrapper(null)

    @Test
    fun `test missing recording file returns failure result`() = runBlocking {
        val transcriber = AndroidOnDeviceTranscriber(DummyContext())
        val missingFile = File("/non_existent_folder/missing_recording.m4a")

        val result = transcriber.transcribeRecording(missingFile)
        assertFalse(result.isSuccess)
        assertEquals("Recording file does not exist.", result.errorMessage)
    }

    @Test
    fun `test empty recording file returns failure result`() = runBlocking {
        val transcriber = AndroidOnDeviceTranscriber(DummyContext())
        val tempFile = File.createTempFile("empty_test", ".m4a")
        tempFile.deleteOnExit()

        val result = transcriber.transcribeRecording(tempFile)
        assertFalse(result.isSuccess)
        assertEquals("Recording file is empty (0 bytes).", result.errorMessage)
    }

    @Test
    fun `test local transcription result data class model`() {
        val result = LocalTranscriptionResult(
            isSuccess = true,
            transcriptText = "Hello Technical Communication",
            audioDurationMs = 12000L,
            processingTimeMs = 250L,
            engineName = "Android Local On-Device Transcriber",
            errorMessage = null,
            isOfflineVerified = true
        )

        assertTrue(result.isSuccess)
        assertEquals("Hello Technical Communication", result.transcriptText)
        assertEquals(12000L, result.audioDurationMs)
        assertEquals(250L, result.processingTimeMs)
        assertTrue(result.isOfflineVerified)
    }
}
