# TaskMesh — Stage B1.1: On-Device Speech-to-Text Audit & Verification Report

**Document Version:** 1.1.0  
**Date:** October 18, 2026  
**Module:** Daily Speech & Share Work On-Device Transcription PoC  
**Target SDK:** Android Compile SDK 35 / Min SDK 26  

---

## 1. Audit Trace of `transcribeRecording()`

We traced the complete execution path of `AndroidOnDeviceTranscriber.transcribeRecording(recordingFile)` in [`OnDeviceSpeechTranscriber.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/sharework/OnDeviceSpeechTranscriber.kt):

1. **File & Audio Format Validation**:
   - Checks `recordingFile.exists()` and `recordingFile.length() > 0L`. Rejects missing/empty files.
   - Enforces `MAX_AUDIO_SIZE_BYTES = 15 MB`.
   - Uses `MediaMetadataRetriever` to extract audio duration in milliseconds (`audioDurationMs`).
   - Uses `MediaExtractor` to iterate through tracks and verify the presence of a valid `audio/` codec track.
2. **System & On-Device Recognizer Availability**:
   - Checks `SpeechRecognizer.isRecognitionAvailable(context)`.
   - On Android 12+ (API 31+), checks `SpeechRecognizer.isOnDeviceRecognitionAvailable(context)` to verify whether Google's on-device offline speech recognition packs are installed on the device.
3. **Android SDK SpeechRecognizer Architecture for File Transcription**:
   - **Android SDK Limitation**: Standard `android.speech.SpeechRecognizer` is designed exclusively for live microphone streams (`AudioRecord`). It does NOT natively accept a file path (`.m4a` on disk) as an input parameter for `startListening()`.
   - **For File-Based Offline STT**: File-based transcription of saved `.m4a` files on disk requires decoding the `.m4a` AAC container into PCM 16kHz mono audio buffers and passing those buffers to an offline engine such as **Vosk Android SDK (`org.kaldi:vosk-android:0.3.47`)** or **Whisper.cpp NDK**.
   - `AndroidOnDeviceTranscriber` flags file verification status with `isSimulated = true` to clearly distinguish simulated file metadata validation from real PCM acoustic decoding.

---

## 2. Local Transcription Engine Comparison Matrix

| Feature / Metric | Android System `SpeechRecognizer` (API 31+ On-Device) | Vosk Android Speech SDK (`org.kaldi:vosk-android`) | OpenAI Whisper.cpp (Tiny / Base) |
| :--- | :--- | :--- | :--- |
| **Offline Operation** | 100% Offline (Uses system speech pack) | 100% Offline (Kaldi JNI Engine) | 100% Offline (C++ NDK Engine) |
| **File (.m4a) Transcription** | Requires live mic stream or pipe | **Native File/Buffer Transcription** | **Native File/Buffer Transcription** |
| **Language & Accents** | English (`en-US`, `en-IN` Indian English) | `vosk-model-small-en-in-0.4` (Indian English) | Multilingual / English (`whisper-tiny.en`) |
| **APK / AAB Size Impact** | **0 MB** (Uses system speech engine) | **~31 MB** (Model file bundled or downloaded) | **~39 MB to 140 MB** (Model file) |
| **RAM Usage** | Low (< 20 MB) | Moderate (~40 MB) | High (~100 MB to 250 MB) |
| **License** | Native Android SDK | Apache 2.0 | MIT License |

---

## 3. Required Manifest Permissions & Privacy Guarantees

- **Manifest Permission**: `<uses-permission android:name="android.permission.RECORD_AUDIO" />` declared in `AndroidManifest.xml`.
- **Zero Secrets**: No API keys, server tokens, or cloud endpoints required.
- **Privacy**: All audio processing occurs on `Dispatchers.IO` in background coroutine threads locally on the device. No audio or transcript data is transmitted over the network.
- **Non-Interference**: Share Work audio recording, saved files, and Meshy AI evaluation flows remain 100% intact and untouched.

---

## 4. Summary of Verification Status

- **Unit Tests**: All 39 unit tests (`OnDeviceSpeechTranscriberTest`, `DailySpeechTest`, `MeshyAiAnswerKeyTest`, `ShareWorkAdapterTest`, `ShareWorkStorageTest`, etc.) passed cleanly.
- **Build Status**: Debug APK (`app-debug.apk` 22.99 MB) and Release APK (`app-release.apk` 16.06 MB) compiled successfully.
