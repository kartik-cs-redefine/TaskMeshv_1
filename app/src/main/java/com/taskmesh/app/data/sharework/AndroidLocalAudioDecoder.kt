package com.taskmesh.app.data.sharework

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AndroidLocalAudioDecoder {

    private const val TAG = "AudioDecoder"
    private const val TIMEOUT_US = 10000L

    private fun logE(tag: String, msg: String, e: Throwable? = null) {
        try {
            if (e != null) Log.e(tag, msg, e) else Log.e(tag, msg)
        } catch (_: Throwable) {}
    }

    suspend fun decodeM4aToPcmWav(
        inputFile: File,
        outputFile: File,
        targetSampleRate: Int = 16000
    ): Boolean = withContext(Dispatchers.IO) {
        if (!inputFile.exists() || inputFile.length() == 0L) {
            logE(TAG, "Input file does not exist or is empty: ${inputFile.absolutePath}")
            return@withContext false
        }

        var extractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        var outputStream: FileOutputStream? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)

            var trackIndex = -1
            var format: MediaFormat? = null
            var mime: String? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mimeType = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mimeType.startsWith("audio/")) {
                    trackIndex = i
                    format = trackFormat
                    mime = mimeType
                    break
                }
            }

            if (trackIndex < 0 || format == null || mime == null) {
                logE(TAG, "No audio track found in file: ${inputFile.absolutePath}")
                return@withContext false
            }

            extractor.selectTrack(trackIndex)
            decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val pcmFile = File(outputFile.absolutePath + ".raw")
            outputStream = FileOutputStream(pcmFile)

            val info = MediaCodec.BufferInfo()
            var isInputEOS = false
            var isOutputEOS = false
            var totalPcmBytes = 0L

            while (!isOutputEOS) {
                if (!isInputEOS) {
                    val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isInputEOS = true
                            } else {
                                val presentationTimeUs = extractor.sampleTime
                                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                val outputBufferIndex = decoder.dequeueOutputBuffer(info, TIMEOUT_US)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                    if (outputBuffer != null && info.size > 0) {
                        outputBuffer.position(info.offset)
                        outputBuffer.limit(info.offset + info.size)
                        val chunk = ByteArray(info.size)
                        outputBuffer.get(chunk)
                        outputStream.write(chunk)
                        totalPcmBytes += chunk.size
                    }
                    decoder.releaseOutputBuffer(outputBufferIndex, false)

                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isOutputEOS = true
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null

            // Construct 44-byte WAV header and raw PCM content into target WAV file
            writeCanonicalWavFile(pcmFile, outputFile, targetSampleRate, totalPcmBytes)

            if (pcmFile.exists()) {
                pcmFile.delete()
            }

            return@withContext outputFile.exists() && outputFile.length() > 44L

        } catch (e: Exception) {
            logE(TAG, "Audio decoding failed for ${inputFile.name}", e)
            return@withContext false
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { decoder?.stop(); decoder?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
        }
    }

    private fun writeCanonicalWavFile(
        pcmFile: File,
        wavFile: File,
        sampleRate: Int,
        totalPcmBytes: Long
    ) {
        val totalDataLen = totalPcmBytes + 36
        val channels = 1
        val bitDepth = 16
        val byteRate = (sampleRate * channels * bitDepth / 8).toLong()

        FileOutputStream(wavFile).use { out ->
            writeWavHeader(out, totalPcmBytes, totalDataLen, sampleRate.toLong(), channels, byteRate)
            if (pcmFile.exists()) {
                pcmFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
        }
    }

    fun writeWavHeader(
        out: OutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalDataLen.toInt())
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size (16 for PCM)
        buffer.putShort(1.toShort()) // AudioFormat (1 for PCM)
        buffer.putShort(channels.toShort())
        buffer.putInt(longSampleRate.toInt())
        buffer.putInt(byteRate.toInt())
        buffer.putShort((channels * 16 / 8).toShort()) // BlockAlign
        buffer.putShort(16.toShort()) // BitsPerSample
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(totalAudioLen.toInt())

        out.write(header)
    }
}
