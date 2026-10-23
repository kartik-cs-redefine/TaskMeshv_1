# TaskMesh — Daily Speech Meshy AI Evaluation Contract (Specification v1.0)

**Document Version:** 1.0.0  
**Date:** October 18, 2026  
**Status:** Formal Specification & API Contract (Stage A)  
**Target Module:** Daily Speech 25-Day Technical Communication Challenge (`daily_speech_25d`)  

---

## 1. End-to-End Architectural Flow

```
┌────────────────────────────────┐
│  DailySpeechContentScreen     │ User reads Days 1–25 Q&A study content from PDF dataset
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  ShareWork Audio Engine        │ Captures AAC audio (.m4a) via AudioRecorder.kt
│  (ShareWorkSession)            │ Associated with worksheetId = "daily_speech_25d" & dayNumber
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  RecordingStorageManager       │ Persists audio file & metadata locally in recordings_metadata.json
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  Speech Transcription          │ Converts spoken audio Base64/file to verbatim transcript text
│  (Gemini Audio / Whisper)      │ (Extracts user's spoken words)
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  Meshy AI Evaluation Request   │ Constructs DailySpeechEvaluationRequest JSON:
│  (Bearer <firebaseIdToken>)    │ dayNumber, topicTitle, authoritativeStudyContent, transcript
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  Backend Proxy Server          │ Verifies Firebase Auth ID Token, validates payload & rate limits,
│  (Google Apps Script / Server) │ evaluates transcript against 100-point rubric via Gemini REST API
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  Meshy AI Evaluation Response  │ Returns DailySpeechEvaluationResponse JSON:
│  (DailySpeechEvaluationResult) │ Category scores (100pt max), feedback, strengths, missing points
└───────────────┬────────────────┘
                │
                ▼
┌────────────────────────────────┐
│  MeshyAiReviewDialog           │ Validates score sum equality & score bounds, caches result JSON,
│  (Android UI Render)           │ and displays score badge (87%), transcript & question breakdown
└────────────────────────────────┘
```

---

## 2. Request Schema Specification (`DailySpeechEvaluationRequest`)

```json
{
  "evaluationId": "eval_ds_day4_1700000000000",
  "recordingId": "rec_ds_1700000000000",
  "dayNumber": 4,
  "topicTitle": "Flow of Communication – Horizontal, Diagonal, & Grapevine",
  "authoritativeStudyContent": {
    "unit": "Unit 1: Fundamentals of Technical Communication",
    "summary": "Daily speech topic for Day 4 on Technical Communication flow.",
    "questions": [
      {
        "questionNumber": 1,
        "promptText": "What is Horizontal Communication?",
        "correctAnswerText": "Horizontal communication is communication between colleagues at the same organizational hierarchy..."
      },
      {
        "questionNumber": 2,
        "promptText": "What is Diagonal Communication?",
        "correctAnswerText": "Diagonal communication cuts across different hierarchies and departments..."
      }
    ]
  },
  "transcript": "Horizontal communication happens between peers at the same level in an office. Diagonal communication cuts across departments when people need fast cross-team coordination.",
  "language": "en-IN",
  "rubricVersion": "v1.0"
}
```

---

## 3. Fixed 100-Point Evaluation Rubric Breakdown

The Daily Speech evaluation employs a deterministic **100-Point Fixed Rubric** evaluating spoken expression against the authoritative day content:

| Rubric Category | Max Points | Evaluation Focus & Criteria |
| :--- | :---: | :--- |
| **1. Content Accuracy** | **30** | Accuracy of technical concepts, definitions, and core explanations compared to the authoritative study content. |
| **2. Vocabulary & Word Choice** | **25** | Use of relevant technical communication terminology, precision of vocabulary, and absence of inappropriate slang. |
| **3. Language Quality & Grammar** | **25** | Sentence structure, grammatical correctness, clarity of thought expression, and natural flow of English. |
| **4. Completeness & Relevance** | **20** | Coverage of all daily prompt questions, addressing key points, and remaining on-topic. |
| **TOTAL MAXIMUM SCORE** | **100** | **Sum of all 4 category scores (`30 + 25 + 25 + 20 = 100`)** |

---

## 4. Response Schema Specification (`DailySpeechEvaluationResponse`)

```json
{
  "evaluationId": "eval_ds_day4_1700000000000",
  "recordingId": "rec_ds_1700000000000",
  "rubricVersion": "v1.0",
  "scores": {
    "contentAccuracyScore": 26,
    "contentAccuracyMax": 30,
    "vocabularyScore": 22,
    "vocabularyMax": 25,
    "languageQualityScore": 21,
    "languageQualityMax": 25,
    "completenessScore": 18,
    "completenessMax": 20,
    "totalScore": 87,
    "maxScore": 100,
    "percentage": 87
  },
  "overallSummary": "Solid spoken attempt! You explained horizontal and diagonal communication clearly with appropriate technical vocabulary.",
  "strengths": [
    "Accurate definition of horizontal peer-to-peer communication.",
    "Clear distinction of cross-departmental diagonal flow."
  ],
  "improvements": [
    "Include an example for Grapevine/Informal communication to achieve full completeness."
  ],
  "questionsFeedback": [
    {
      "questionNumber": 1,
      "promptText": "What is Horizontal Communication?",
      "isCovered": true,
      "score": 9,
      "maxScore": 10,
      "spokenEvidence": "Horizontal communication happens between peers at the same level...",
      "feedbackText": "Accurate explanation of lateral peer communication.",
      "missingPoints": "Could mention that it helps build team coordination."
    },
    {
      "questionNumber": 2,
      "promptText": "What is Diagonal Communication?",
      "isCovered": true,
      "score": 9,
      "maxScore": 10,
      "spokenEvidence": "Diagonal communication cuts across departments...",
      "feedbackText": "Clear explanation of diagonal cross-functional communication.",
      "missingPoints": ""
    }
  ]
}
```

---

## 5. Validation Rules & Total-Score Consistency

1. **Category Score Bounds**:
   - `0 <= contentAccuracyScore <= 30`
   - `0 <= vocabularyScore <= 25`
   - `0 <= languageQualityScore <= 25`
   - `0 <= completenessScore <= 20`
2. **Total Score Consistency Enforcement**:
   - `totalScore == contentAccuracyScore + vocabularyScore + languageQualityScore + completenessScore`
   - `maxScore == 100`
   - `percentage == Math.round((totalScore / 100) * 100)`
3. **Validation Failure Action**:
   - If `totalScore != sum(categoryScores)` or any category score exceeds its defined maximum, the evaluation response is classified as **Malformed** (`MALFORMED_AI_RESPONSE`) and rejected without updating user progress.

---

## 6. Error Contract Specification

| Error Code | HTTP Status | User-Facing Message | Root Cause & Resolution |
| :--- | :---: | :--- | :--- |
| `MISSING_RECORDING` | 400 | "Audio recording file not found on device." | Local audio file does not exist or has 0 bytes. |
| `EMPTY_TRANSCRIPT` | 400 | "No spoken speech detected in recording." | Audio file contains silence or ambient noise only. |
| `INVALID_DAY_CONTENT` | 400 | "Could not reconstruct study content for Day X." | Day dataset content or question array is missing. |
| `UNAUTHORIZED` | 401 | "Authentication required. Please sign in to continue." | Firebase Auth ID Token is missing, expired, or invalid. |
| `RATE_LIMIT_EXCEEDED` | 429 | "Daily evaluation limit reached (10 reviews/day). Try again tomorrow!" | User exceeded daily AI review quota. |
| `AI_SERVICE_TIMEOUT` | 504 | "AI review server timed out. Tap to retry." | Server connection timed out (>30s). Bounded retries applied. |
| `MALFORMED_AI_RESPONSE` | 502 | "Invalid evaluation schema returned by server." | Server returned JSON missing required score fields. |

---

## 7. Idempotency & Association Rules

1. **Recording Association**:
   - Every evaluation is tied to `recordingId` (`"rec_ds_1700000000000"`) and `worksheetId` (`"daily_speech_25d"`).
2. **Local Caching & Idempotency**:
   - Valid evaluation JSON is cached in `SavedRecording.reviewResultJson` in `recordings_metadata.json`.
   - Re-opening a reviewed day's recording reuses the cached evaluation immediately without re-submitting audio or incrementing rate limits.

---

## 8. Privacy, Security & Retention

1. **Authentication**: Requests attach `Authorization: Bearer <firebaseIdToken>` header. Verified server-side via Firebase Auth Admin / Identity Toolkit.
2. **Zero Secrets in Android APK**: No Gemini API keys or server tokens are stored in Android client resources or source code.
3. **Ephemeral Audio Processing**: Audio file Base64 is processed in memory during the evaluation request and discarded immediately after transcription. No audio files are retained on external servers.

---

## 9. Semantic Text Evaluation vs. Future Acoustic Audio Evaluation

> [!IMPORTANT]
> **Semantic Transcript Evaluation (Stage A Scope)**:
> Evaluates concept accuracy, technical word choice, grammar, sentence structure, and topic completeness based on the **transcribed text of the user's spoken words**.

> [!NOTE]
> **Future Acoustic Audio Evaluation (Future Extension)**:
> Pitch variation, acoustic fluency, speech rate (words/minute), pause duration, and stress patterns require raw audio waveform analysis (DSP / Wav2Vec2) and are **explicitly excluded from text-transcript scoring**.

---

## 10. Stage B Acceptance Criteria & Test Matrix

1. **Test 1**: Verify `DailySpeechEvaluationRequest` JSON serialization contains all required fields (`dayNumber`, `topicTitle`, `authoritativeStudyContent`, `transcript`).
2. **Test 2**: Verify `DailySpeechEvaluationResponse` parser validates that `totalScore == sum(categoryScores)`.
3. **Test 3**: Verify invalid category scores (e.g. `contentAccuracyScore = 35` > 30) fail validation safely.
4. **Test 4**: Verify error contract responses (`RATE_LIMIT_EXCEEDED`, `UNAUTHORIZED`) extract user-friendly messages cleanly.
