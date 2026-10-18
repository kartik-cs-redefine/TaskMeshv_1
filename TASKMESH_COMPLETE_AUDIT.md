# TASKMESH — COMPLETE PRODUCT & CODEBASE AUDIT

**Date:** October 18, 2026  
**Project:** TaskMesh Native Android Application  
**Package ID:** `com.taskmesh.app`  
**Architecture:** Single-Activity Jetpack Compose + Material 3 + Firebase + Coroutines  

---

## 1. Executive Summary

TaskMesh is an offline-first native Android learning application built using Kotlin, Jetpack Compose, and Material 3. Its core product value revolves around daily structured curriculum progression, interactive vocabulary cards with Devanagari Hindi translations, One Word Substitution (OWSP) workbook quizzes, worksheet audio recordings ("Share Work"), and Meshy AI spoken answer evaluation.

### Overall Product Capabilities
1. **Multi-Curriculum Learning System**:
   - **General Vocabulary**: 30-Day curriculum with 210 unique vocabulary entries (7 entries/day).
   - **OWSP Workbook**: 59-Day curriculum with 59 sheets, 236 options, and interactive MCQ quizzes.
2. **Worksheet-Based Audio Recording ("Share Work")**:
   - Single vertically scrollable question list containing ALL questions across selected days.
   - Continuous AAC audio capture (`.m4a`) during scrolling with live timer and in-app navigation lock guard.
   - Saved recordings library with Android Sharesheet / WhatsApp sharing (`FileProvider`) and playback.
3. **Meshy AI Spoken Answer Review**:
   - Explicit user-triggered AI evaluation comparing user spoken audio against dynamic answer keys generated directly from source datasets.
   - Local JSON caching of evaluation results and Google-verified Firebase Auth ID token security headers.
4. **Cloud Profile & Persistence**:
   - Firebase Authentication (Email/Password & Credential Manager Google Sign-In).
   - Firestore profile storage (`users/{uid}`) and background learning progress backup.
   - SharedPreferences offline-first storage for study days, learned words, bookmarks, theme, and WorkManager notification schedules.

### Capability & Maturity Matrix

| Area / Feature | Status | Quality & Integrity Assessment | Major Risks |
| :--- | :--- | :--- | :--- |
| **General Vocabulary (30D)** | **Implemented & Verified** | High quality dataset, offline persistence, daily 5:30 AM progression schedule. | Low. |
| **OWSP Workbook (59D)** | **Implemented & Verified** | 59 questions, 236 options, interactive MCQ scoring, A-Z index, and dictionary search. | Low. |
| **Share Work Recording System** | **Implemented & Verified** | Native `MediaRecorder` AAC engine, in-app navigation lock, `FileProvider` sharing. | Low. |
| **Meshy AI Audio Review** | **Implemented & Verified (Client)** | Dynamic answer keys, JSON score validation, response caching, Firebase ID token header. | Server backend deployment required for live endpoint. |
| **Firebase Auth & Firestore Profile** | **Implemented & Verified** | Email/Password, Google Sign-In, profile name editing, Firestore security rules (`firestore.rules`). | Requires project Web Client ID in Firebase Console. |
| **TaskMesh Nudge Notifications** | **Implemented & Verified** | WorkManager daily 8:00 AM notification scheduler (`TaskMeshNudgeWorker`) & BootReceiver. | Android 13+ `POST_NOTIFICATIONS` runtime permission dialog. |
| **DSA & Project Initiatives** | **UI Placeholder Only** | Represented as "Coming Soon" cards in [`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt). | Users cannot access or interact with these modules. |
| **Daily Speech & Self Analysis** | **UI Placeholder Only** | Represented as "Coming Soon" cards in [`EnglishInitiativeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/EnglishInitiativeScreen.kt). | Module cards are disabled. |

---

## 2. Repository and Architecture Inventory

### Project Structure & Packages
```
app/src/main/java/com/taskmesh/app/
├── MainActivity.kt                      # Main Single-Activity Controller & Route Dispatcher
├── data/                                # Data Layer, Repositories, Datasets & Managers
│   ├── AuthRepository.kt               # Firebase Auth (Email/Pass & Google Credential Manager)
│   ├── UserRepository.kt               # Cloud Firestore User Profile (`users/{uid}`) Persistence
│   ├── UserProfile.kt                  # User Profile & Progress Data Model
│   ├── VocabularyModel.kt              # General Vocabulary Word Data Class
│   ├── VocabularyDataset.kt            # 30-Day Curriculum Dataset (210 entries)
│   ├── OwspVocabularyModel.kt          # OWSP Workbook Models & JSON Repository
│   ├── PreferencesManager.kt           # SharedPreferences Offline Storage
│   ├── StudyDayManager.kt              # General Vocabulary 5:30 AM Progression Schedule
│   ├── OwspStudyDayManager.kt          # OWSP Workbook 5:30 AM Progression Schedule
│   ├── NudgeMessages.kt                # Motivational Notification Message Database
│   ├── NudgeScheduler.kt               # WorkManager Notification Scheduler
│   ├── TaskMeshNudgeWorker.kt          # WorkManager Coroutine Worker
│   ├── TaskMeshBootReceiver.kt         # Android BOOT_COMPLETED Broadcast Receiver
│   └── sharework/                      # Share Work Audio Engine & Meshy AI
│       ├── WorksheetModel.kt           # Common Worksheet & Recording Models
│       ├── WorksheetAdapters.kt        # General Vocab & OWSP Dataset Adapters
│       ├── AudioRecorder.kt            # Native MediaRecorder AAC/m4a Recorder
│       ├── RecordingStorageManager.kt  # File & Metadata Manager (recordings_metadata.json)
│       ├── ShareWorkSession.kt         # In-App Navigation Lock & Session State
│       ├── MeshyAiModels.kt            # Answer Key & AI Evaluation Data Models
│       └── MeshyAiService.kt           # Dynamic Answer Key Engine & HTTP Client
├── ui/                                 # UI Layer (Jetpack Compose + Material 3)
│   ├── theme/
│   │   ├── Color.kt                    # Light & Dark High-Contrast Palette Tokens
│   │   └── Theme.kt                    # Material 3 Theme, Typography Scale & Spacing Tokens
│   ├── components/
│   │   ├── WordCardItem.kt             # General Vocabulary Interactive Card (48dp targets)
│   │   └── TopBarAccountAction.kt      # Shared Reusable TopBar Account Icon Action
│   └── screens/
│       ├── SplashScreen.kt             # App Entry & Brand Animation
│       ├── InitiativeSelectionScreen.kt# TaskMesh Home / Initiative Hub
│       ├── EnglishInitiativeScreen.kt  # Module Selection (Vocab, Speech, Analysis, Share Work)
│       ├── HomeScreen.kt               # General Vocabulary Today Screen
│       ├── OwspHomeScreen.kt           # OWSP Workbook MCQ Quiz & Explanation Screen
│       ├── JourneyScreen.kt            # General Vocabulary 30-Day Grid
│       ├── OwspJourneyScreen.kt        # OWSP Workbook 59-Day Grid
│       ├── OwspSeeAllMeaningsScreen.kt # Dictionary Search, A-Z Filter & Detail Dialogs
│       ├── FavouritesScreen.kt         # Saved Bookmarks List & Empty State
│       ├── SettingsScreen.kt           # Settings Cards (Curriculum, Day Pickers, Nudge, Theme)
│       ├── ProfileScreen.kt            # Profile & Learning Dashboard (Reference 2 Layout)
│       ├── AuthScreen.kt               # Login, Registration, Reset & Google Sign-In
│       └── sharework/                  # Share Work Screens
│           ├── ShareWorkEntryCard.kt   # Share Work Card Entry Point
│           ├── WorksheetSelectionScreen.kt # Worksheet Selection
│           ├── DaySelectionScreen.kt   # Multi-Select Days / Sections Grid
│           ├── ShareWorkRecordingScreen.kt # Scrollable Recording Sheet & Control Bar
│           ├── SavedRecordingsScreen.kt# Saved Recordings Library & Sharesheet
│           └── MeshyAiReviewDialog.kt  # Meshy AI Evaluation Sheet & Feedback Breakdown
```

---

## 3. Feature-by-Feature Truth Table

| Feature | Implementation Status | Verified Evidence |
| :--- | :--- | :--- |
| **General Vocabulary 30-Day Curriculum** | Implemented & Verified | 210 entries in [`VocabularyDataset.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/VocabularyDataset.kt). Tested in `StudyDayManagerTest.kt`. |
| **OWSP Workbook 59-Day Curriculum** | Implemented & Verified | 59 questions, 236 options parsed from `owsp_workbook_dataset.json`. Tested in `OwspDatasetTest.kt`. |
| **OWSP MCQ Quiz & Answer Scoring** | Implemented & Verified | Tested in `OwspQuizTest.kt`. Calculates correct/incorrect options dynamically. |
| **5:30 AM Daily Progression** | Implemented & Verified | Tested in `StudyDayManagerTest.kt`. Auto-advances day at 05:30 AM local time. |
| **Offline SharedPreferences Persistence** | Implemented & Verified | [`PreferencesManager.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/PreferencesManager.kt) manages learned words, bookmarks, days, themes, and notification settings. |
| **Firebase Auth (Email/Pass & Google)** | Implemented & Verified | Tested in `TaskMeshAuthTest.kt`. Supports registration, login, password reset, and Credential Manager Google Sign-In. |
| **Cloud Firestore Profile & Security** | Implemented & Verified | [`UserRepository.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/UserRepository.kt) & [`firestore.rules`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/firestore.rules). |
| **WorkManager Nudge Notifications** | Implemented & Verified | Tested in `TaskMeshNudgeTest.kt`. Schedules daily 8:00 AM notifications and handles boot completion. |
| **Share Work Audio Recording Engine** | Implemented & Verified | [`AudioRecorder.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/sharework/AudioRecorder.kt) captures AAC `.m4a` audio with in-app navigation lock guard. |
| **Android Sharesheet / WhatsApp Sharing** | Implemented & Verified | [`RecordingStorageManager.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/data/sharework/RecordingStorageManager.kt) generates `content://` URIs via `FileProvider`. |
| **Meshy AI Dynamic Answer Keys** | Implemented & Verified | Tested in `MeshyAiAnswerKeyTest.kt`. Reconstructs exact answers dynamically without hardcoding. |
| **Meshy AI JSON Score Validation** | Implemented & Verified | Tested in `MeshyAiAnswerKeyTest.kt`. Validates `0 <= totalScore <= maxScore` and handles error JSON payloads. |
| **DSA & Project Initiatives** | UI Placeholder Only | "Coming Soon" cards in [`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt). |
| **Daily Speech & Self Analysis** | UI Placeholder Only | "Coming Soon" cards in [`EnglishInitiativeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/EnglishInitiativeScreen.kt). |

---

## 4. Business Logic, Data Integrity & Security Audit

1. **Zero Client Secrets in Android Code/APK**:
   - `MeshyAiService.kt` contains **zero API keys or secret tokens**. `GEMINI_API_KEY` is kept exclusively on the server side in Google Apps Script Script Properties.
2. **Firebase Auth Token Verification**:
   - Android client attaches signed-in user's Firebase Auth ID token in `Authorization: Bearer <idToken>` and JSON payload. Server verifies token via official Firebase Identity Toolkit REST API (`accounts:lookup`).
3. **Data Loss Prevention**:
   - Local `SharedPreferences` remains the primary offline-first source of truth. Offline progression, learned words, bookmarks, and recording files are stored locally and never erased by auth or network state changes.
4. **Keystore & Signing Security**:
   - Release signing setup uses `keystore.properties` pointing to `taskmesh-release.keystore`. Keystore and passwords are kept local and excluded from Git tracking.
