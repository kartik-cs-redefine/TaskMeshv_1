package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.AndroidLocalAudioDecoder
import com.taskmesh.app.data.sharework.AndroidOnDeviceTranscriber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class AndroidLocalAudioDecoderTest {

    private class MockTestContext : android.content.ContextWrapper(null) {
        private val tempFolder = File(System.getProperty("java.io.tmpdir"), "taskmesh_unit_tests").apply { mkdirs() }
        override fun getCacheDir(): File = tempFolder
        override fun getExternalFilesDir(type: String?): File = File(tempFolder, type ?: "files").apply { mkdirs() }
    }

    @Test
    fun `test missing input file decoding returns false`() = runBlocking {
        val missingFile = File("/invalid_path/missing_test.m4a")
        val outputFile = File.createTempFile("output_test", ".wav")
        outputFile.deleteOnExit()

        val success = AndroidLocalAudioDecoder.decodeM4aToPcmWav(missingFile, outputFile)
        assertFalse(success)
    }

    @Test
    fun `test empty input file decoding returns false`() = runBlocking {
        val emptyFile = File.createTempFile("empty_test", ".m4a")
        emptyFile.deleteOnExit()
        val outputFile = File.createTempFile("output_test", ".wav")
        outputFile.deleteOnExit()

        val success = AndroidLocalAudioDecoder.decodeM4aToPcmWav(emptyFile, outputFile)
        assertFalse(success)
    }

    @Test
    fun `test 44 byte RIFF WAV header creation`() {
        val baos = ByteArrayOutputStream()
        val sampleRate = 16000L
        val channels = 1
        val pcmBytesLen = 32000L // 1 sec 16kHz 16bit
        val totalDataLen = pcmBytesLen + 36
        val byteRate = sampleRate * channels * 2

        AndroidLocalAudioDecoder.writeWavHeader(
            out = baos,
            totalAudioLen = pcmBytesLen,
            totalDataLen = totalDataLen,
            longSampleRate = sampleRate,
            channels = channels,
            byteRate = byteRate
        )

        val headerBytes = baos.toByteArray()
        assertEquals(44, headerBytes.size)

        // Check RIFF & WAVE magic bytes
        assertEquals('R'.code.toByte(), headerBytes[0])
        assertEquals('I'.code.toByte(), headerBytes[1])
        assertEquals('F'.code.toByte(), headerBytes[2])
        assertEquals('F'.code.toByte(), headerBytes[3])

        assertEquals('W'.code.toByte(), headerBytes[8])
        assertEquals('A'.code.toByte(), headerBytes[9])
        assertEquals('V'.code.toByte(), headerBytes[10])
        assertEquals('E'.code.toByte(), headerBytes[11])
    }

    @Test
    fun `test missing vosk model returns model installation error with isSimulated false`() = runBlocking {
        val transcriber = AndroidOnDeviceTranscriber(MockTestContext())
        val dummyAudio = File.createTempFile("dummy_audio", ".m4a")
        dummyAudio.writeBytes(ByteArray(1024))
        dummyAudio.deleteOnExit()

        val result = transcriber.transcribeRecording(dummyAudio)
        assertFalse(result.isSuccess)
        assertFalse(result.isSimulated) // Must be false for real engine path!
        assertTrue(result.errorMessage?.contains("Vosk model not installed") == true || result.errorMessage?.contains("Failed to decode") == true)
    }
}
