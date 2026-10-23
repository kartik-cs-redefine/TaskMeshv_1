# TaskMesh — Dedicated Cloudflare Workers AI Backend Plan (Daily Speech Isolated)

This corrected implementation plan details the dedicated **Cloudflare Workers AI** backend for Daily Speech transcription (**Cloudflare Workers AI `@cf/openai/whisper`**) and evaluation (**Gemini 2.0 Flash / `@cf/meta/llama-3.1-8b-instruct`**), while guaranteeing **100% isolation of Vocabulary, OWSP, and Share Work AI features**.

---

## 1. Provider Architecture & Module Isolation

```
 ┌────────────────────────────────────────┐
 │   Existing Vocabulary & OWSP Modules   │ ──► 100% UNTOUCHED (Offline Datasets + Local SharedPreferences)
 └────────────────────────────────────────┘

 ┌────────────────────────────────────────┐
 │   Existing Share Work AI Module        │ ──► 100% UNTOUCHED (MeshyAiService.kt & Google Apps Script)
 └────────────────────────────────────────┘

 ┌────────────────────────────────────────┐
 │   NEW Daily Speech AI Flow             │ ──► DEDICATED & ISOLATED
 │   (DailySpeechCloudflareService.kt)    │     1. Audio -> Cloudflare Worker (action = "transcribe")
 └──────────────────┬─────────────────────┘        -> Workers AI Whisper (@cf/openai/whisper) -> Transcript
                    │                           2. User Reviews & Edits Transcript in Compose Sheet
                    │                           3. Confirmed Transcript -> Cloudflare Worker (action = "evaluate")
                    ▼                              -> AI Evaluator -> 100pt Rubric Result
 ┌────────────────────────────────────────┐
 │   Dedicated Cloudflare Worker          │
 │   (backend/worker.js - taskmesh-ai-rev)│ ──► Verified Firebase Auth ID Token + Secret GEMINI_API_KEY
 └────────────────────────────────────────┘
```

---

## 2. Server Provider Selections & Secret Management

| Operation / Action | Selected AI Provider & Model | Input Format | Output Format | Secret Storage |
| :--- | :--- | :--- | :--- | :--- |
| **Action A: `transcribe`** | **Cloudflare Workers AI `@cf/openai/whisper`** | Base64 Audio ArrayBuffer (`audio/m4a` / WAV) | JSON: `{ "transcript": "Verbatim user speech..." }` | Cloudflare Workers AI Binding (`env.AI`) |
| **Action B: `evaluate`** | **Gemini 2.0 Flash REST API** (or `@cf/meta/llama-3.1-8b-instruct`) | Confirmed Transcript + Day Study Content JSON | JSON: 100pt Rubric Breakdown (`totalScore`, `scores`, `feedback`) | Cloudflare Worker Secret (`wrangler secret put GEMINI_API_KEY`) |

> [!IMPORTANT]
> **Strict Isolation & Security Guarantees**:
> - **Vocabulary & OWSP**: 100% UNTOUCHED.
> - **Share Work AI Review**: `MeshyAiService.kt` and `MeshyAiReviewDialog.kt` remain 100% UNTOUCHED.
> - **Zero Secrets in APK**: All API keys and tokens are stored in Cloudflare Worker secrets.

---

## 3. File-by-File Modification Plan

### Phase 1 — Cloudflare Worker Backend (`backend/`)

#### [NEW] [backend/worker.js](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/backend/worker.js)
Production Cloudflare Worker JavaScript handler (`export default { async fetch(request, env, ctx) }`):
1. **CORS & Method Validation**: Handles preflight OPTIONS and POST requests.
2. **Firebase Auth ID Token Security Gate**: Verifies `idToken` via Google OAuth2 TokenInfo API (`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=<idToken>`). Validates `aud == "taskmesh-6242d"`, `iss == "https://securetoken.google.com/taskmesh-6242d"`, and expiration.
3. **Daily Speech Actions**:
   - **`action == "transcribe"`**:
     - Converts audio Base64 to binary buffer.
     - Invokes **Cloudflare Workers AI Whisper** model: `env.AI.run("@cf/openai/whisper", { audio: Array.from(audioBuffer) })`.
     - Returns `{ "transcript": "Verbatim user speech..." }`.
   - **`action == "evaluate"`**:
     - Accepts `confirmedTranscript`, `dayNumber`, `topicTitle`, and `authoritativeContent`.
     - Evaluates confirmed transcript against the **100-Point Fixed Rubric**:
       - Content Accuracy: 30 pts
       - Vocabulary & Word Choice: 25 pts
       - Language Quality & Grammar: 25 pts
       - Completeness & Relevance: 20 pts
     - Enforces score sum equality (`totalScore == sum(categoryScores)`).
     - Returns validated JSON: `totalScore`, `maxScore`, `percentage`, `overallSummary`, `strengths`, `improvements`, `questionsFeedback`.

#### [NEW] [backend/wrangler.toml](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/backend/wrangler.toml)
```toml
name = "taskmesh-ai-review"
main = "worker.js"
compatibility_date = "2024-09-23"

[ai]
binding = "AI"

[vars]
FIREBASE_PROJECT_ID = "taskmesh-6242d"
MAX_DAILY_REVIEWS_PER_USER = "10"
```

#### [NEW] [backend/README.md](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/backend/README.md)
Wrangler deployment instructions:
```bash
cd backend
npm install
npx wrangler login
npx wrangler secret put GEMINI_API_KEY
npx wrangler deploy
```

---

### Phase 2 — Android Client Service & UI Integration

#### [NEW] [DailySpeechCloudflareService.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/sharework/DailySpeechCloudflareService.kt)
Dedicated HTTP service for Daily Speech:
- Configurable base URL `var CLOUDFLARE_WORKER_URL: String = ""` (e.g. `https://taskmesh-ai-review.<subdomain>.workers.dev`).
- `suspend fun transcribeSpeechAudio(context, recordingFile, onResult: (Boolean, String?, String?) -> Unit)`: Sends `action = "transcribe"`.
- `suspend fun evaluateSpeechTranscript(context, confirmedTranscript, dayData, onResult: (Boolean, MeshyReviewResult?, String?) -> Unit)`: Sends `action = "evaluate"`.

#### [NEW] [DailySpeechAiReviewSheet.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/dailyspeech/DailySpeechAiReviewSheet.kt)
3-Step Material 3 Modal BottomSheet:
- **Step 1: AI Transcription**: Calls `transcribeSpeechAudio()` showing progress indicator *"Workers AI Whisper is transcribing your speech..."*.
- **Step 2: Transcript Review & Edit**: Displays editable OutlinedTextField with generated transcript so user can review and edit before submitting for evaluation.
- **Step 3: AI Evaluation**: User taps "Confirm & Evaluate" -> Calls `evaluateSpeechTranscript()` -> Displays score badge (`87%`), 100-point rubric breakdown, strengths, and missing points!

#### [MODIFY] [DailySpeechContentScreen.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/dailyspeech/DailySpeechContentScreen.kt)
- Opens `DailySpeechAiReviewSheet` when user taps "Check with Meshy AI".

---

### Phase 3 — Unit Testing

#### [NEW] [DailySpeechCloudflareTest.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/test/java/com/taskmesh/app/data/DailySpeechCloudflareTest.kt)
- Test cases covering:
  - Workers AI Whisper transcription response parsing (`{"transcript": "..."}`).
  - Daily Speech 100-point rubric evaluation response validation (`sum(categoryScores) == totalScore`).
  - Score bounds validation (`0 <= totalScore <= 100`).

---

## 4. Verification Plan

### Automated Tests
- Run `.\gradlew.bat testDebugUnitTest` to execute all unit tests.

### Build Verification
- Run `.\gradlew.bat assembleDebug` and `.\gradlew.bat assembleRelease`.
