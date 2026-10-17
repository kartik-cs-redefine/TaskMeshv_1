# TaskMesh — MeshyAiService JSON Parsing & Error Handling Fix Plan

This plan details the technical root cause fix for `org.json.JSONException: No value for totalScore` and the complete restoration and enhancement of `MeshyAiService.kt`.

---

## 1. Root Cause & Response Flow Fix

### Problem Analysis
- When the backend or Apps Script endpoint returns an error payload (e.g. `{"error": "Daily AI review limit reached"}` or `{"error": "Authentication failed"}`), `MeshyAiService` passed the JSON directly to `root.getInt("totalScore")`, throwing `org.json.JSONException: No value for totalScore`.
- In addition, `MeshyAiService.kt` was truncated externally at line 96.

### Fix Strategy
1. **Safely Extract Backend Errors**:
   Add `extractErrorMessage(jsonString: String)` to check `root.has("error")` or `root.has("message")` first before attempting to parse review fields.
2. **Robust Validation (`parseAndValidateReviewResult`)**:
   Verify `root.has("totalScore")` and `root.has("questionsFeedback")` exist before reading. Validate score bounds (`0 <= totalScore <= maxScore`). Return `null` safely on invalid JSON without throwing unhandled exceptions.
3. **HTTP Response Handling (`reviewRecording`)**:
   If the HTTP status code is non-200 or response body contains `{"error": "..."}`, extract and display the clear error message to the user instead of treating it as a successful review.

---

## 2. Proposed Changes

### Component 1: Android Service Restoration & Enhancement

#### [MODIFY] [MeshyAiService.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/sharework/MeshyAiService.kt)
- Restore and complete `parseAndValidateReviewResult` and `reviewRecording`.
- Add error payload detection (`root.has("error")`).
- Validate score bounds and response field types.
- Format user-friendly error messages for rate limits (HTTP 429), auth failures (HTTP 401), and server errors.

---

### Component 2: Unit Testing

#### [MODIFY] [MeshyAiAnswerKeyTest.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/test/java/com/taskmesh/app/data/MeshyAiAnswerKeyTest.kt)
- Add tests for:
  - Valid review JSON parsing.
  - Error JSON payload parsing (e.g., `{"error": "Daily limit reached"}`).
  - Missing `totalScore` JSON.
  - Invalid field types / malformed `questionsFeedback`.

---

## 3. Verification Plan

### Automated Tests
- Run `.\gradlew.bat testDebugUnitTest` to verify all 30+ unit tests pass.

### Build Verification
- Run `.\gradlew.bat assembleDebug` and `.\gradlew.bat assembleRelease`.
