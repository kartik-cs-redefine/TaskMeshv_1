# TASKMESH — REDESIGN & PRODUCT ROADMAP

**Date:** October 18, 2026  
**Prioritization Framework:** P0 (Critical/Security) -> P1 (Core Experience) -> P2 (Visual Polish) -> P3 (Engagement/Streaks) -> P4 (Future Expansion)  

---

## 1. Prioritized Task Matrix

| Priority | Task Name | User Value | Effort | Affected Files | Acceptance Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **P0** | **Live Meshy AI Proxy Deployment** | Enables live server-side AI evaluation of spoken audio recordings without storing API keys in APK. | Medium | Server proxy script (`TaskMesh_AI_Review_Code.js`), `MeshyAiService.kt` | Deployed server proxy verifies Firebase Auth ID token, evaluates audio via Gemini API, and returns validated evaluation JSON. |
| **P0** | **Android 13+ Notification Permission Prompt** | Ensures daily TaskMesh Nudge reminders can be posted on Android 13+ (SDK 33+). | Low | `MainActivity.kt`, `SettingsScreen.kt` | Prompts user for `POST_NOTIFICATIONS` runtime permission when enabling Nudge notifications in Settings. |
| **P1** | **"Continue Learning" Resume Hero Banner** | Allows learners to resume their active daily study in 1 tap directly from the Home screen. | Low | `InitiativeSelectionScreen.kt`, `MainActivity.kt` | Home screen displays active course title, current day, and a 1-tap "Resume Study" button. |
| **P1** | **First-Launch Onboarding Sequence** | Introduces new users to TaskMesh's daily learning concept and Share Work audio recording. | Medium | `SplashScreen.kt`, `MainActivity.kt`, `PreferencesManager.kt` | 3-slide M3 onboarding pager shown only on first app launch; saved in `SharedPreferences`. |
| **P2** | **Daily Streak & Milestone Analytics** | Boosts daily engagement and retention through streak counters and milestone badges. | Medium | `PreferencesManager.kt`, `ProfileScreen.kt`, `MainActivity.kt` | Tracks consecutive daily learnings and displays streak flame icon in TopBar header and Profile dashboard. |
| **P3** | **Interactive Sound Effects / Audio Feedback** | Delivers subtle audio feedback when completing a daily vocabulary card or finishing an MCQ quiz. | Low | `WordCardItem.kt`, `OwspHomeScreen.kt` | Optional toggle in Settings to play subtle completion sound effects. |
| **P4** | **DSA Initiative Module** | Expands TaskMesh into technical problem solving, data structures, and algorithms. | High | `InitiativeSelectionScreen.kt`, New DSA dataset & screens | Full DSA curriculum with code snippets and interactive problem sheets. |
| **P4** | **Project Engineering Initiative Module** | Provides real-world production engineering & portfolio builds. | High | `InitiativeSelectionScreen.kt`, New Project models & screens | Project architecture guides, step-by-step milestone checklists. |

---

## 2. QA & Verification Verification Commands

### Automated Test Suite Execution
```powershell
$env:ANDROID_PREFS_ROOT = $null
Set-Location "C:\Users\kk824\Downloads\TaskMesh-Native-Android-Project"
.\gradlew.bat testDebugUnitTest
```
- **Test Result**: 33 / 33 unit tests passed cleanly (`BUILD SUCCESSFUL in 12s`).

### Build Execution Commands
```powershell
.\gradlew.bat assembleDebug assembleRelease
```
- **Debug APK**: `C:\Users\kk824\Downloads\TaskMesh-Native-Android-Project\app\build\outputs\apk\debug\app-debug.apk` (22.68 MB)
- **Release APK**: `C:\Users\kk824\Downloads\TaskMesh-Native-Android-Project\app\build\outputs\apk\release\app-release.apk` (16.00 MB)
