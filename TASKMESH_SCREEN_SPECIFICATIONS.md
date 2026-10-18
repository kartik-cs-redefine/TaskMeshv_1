# TASKMESH — SCREEN SPECIFICATIONS CATALOG

**Date:** October 18, 2026  
**Catalog Scope:** All 15 User-Facing Screens & Shared UI Components  

---

## 1. Primary Screens

### 1. TaskMesh Home / Initiative Selection ([`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt))
- **Purpose**: Main platform hub displaying available initiatives and personal streak/progress banner.
- **Entry & Exit**: Opened from Splash. Navigation: English Initiative -> `EnglishInitiativeScreen`, Profile -> `ProfileScreen` or `AuthScreen`.
- **Layout & Spacing**: 16dp horizontal screen margins, 20dp card padding, 16dp vertical section gaps.
- **Responsive & Accessibility**: 48dp min touch targets, adaptive card widths.

### 2. English Initiative ([`EnglishInitiativeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/EnglishInitiativeScreen.kt))
- **Purpose**: Access point for Vocabulary (30D & 59D), Daily Speech, Self Analysis, and Share Work.
- **Entry & Exit**: Opened from Home. Navigation: Vocabulary -> `MainActivity` Vocabulary Scaffold, Share Work -> `WorksheetSelectionScreen`.

### 3. General Vocabulary Today ([`HomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/HomeScreen.kt))
- **Purpose**: Daily 7-word vocabulary learning interface.
- **Entry & Exit**: Opened when active course is "general". Bottom bar navigates to Journey, Favourites, Settings.
- **Layout & Components**: Uses [`WordCardItem.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/components/WordCardItem.kt) with 20dp card padding and 48dp min action buttons.

### 4. OWSP Workbook MCQ Quiz Today ([`OwspHomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspHomeScreen.kt))
- **Purpose**: Daily One Word Substitution MCQ quiz question and explanation for OWSP Workbook.
- **Entry & Exit**: Opened when active course is "owsp". Button opens "See All Meanings & Index".

### 5. Course Dropdown Selector Bar ([`MainActivity.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/MainActivity.kt))
- **Purpose**: Full-width dropdown selector card directly below TopAppBar for switching active courses.
- **States**: "General Vocabulary — 30 Days, 210 Words" vs "OWSP Workbook — 59 Days, One Word Substitution".

### 6. General & OWSP Journey Screens ([`JourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/JourneyScreen.kt) & [`OwspJourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspJourneyScreen.kt))
- **Purpose**: 30-day and 59-day curriculum progress grids with status badges (Current, Completed, Locked).

### 7. See All Meanings & Index ([`OwspSeeAllMeaningsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspSeeAllMeaningsScreen.kt))
- **Purpose**: Full 236-option dictionary search, A-Z letter filters, and 225 PDF A-Z index.

### 8. Favourites / Bookmarks ([`FavouritesScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/FavouritesScreen.kt))
- **Purpose**: Saved bookmarked words across both courses with 1-tap navigation to source day.

### 9. Settings Screen ([`SettingsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/SettingsScreen.kt))
- **Purpose**: Curriculum switcher, manual study day pickers, TaskMesh Nudge WorkManager reminders, and Theme mode.

### 10. Profile & Dashboard ([`ProfileScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/ProfileScreen.kt))
- **Purpose**: User profile card (84dp avatar, Google & Calendar badges), display name editing, learning metrics, and sign-out.

### 11. Authentication Screen ([`AuthScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/AuthScreen.kt))
- **Purpose**: Email/Password login, registration, password reset, and Credential Manager Google Sign-In.

### 12. Share Work Recording Sheet ([`ShareWorkRecordingScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/ShareWorkRecordingScreen.kt))
- **Purpose**: Single vertically scrollable question sheet containing ALL selected questions across selected days. Continuous AAC recording while scrolling, live timer (`00:42`), mic pulse, and in-app navigation lock guard.

### 13. Saved Recordings Library ([`SavedRecordingsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/SavedRecordingsScreen.kt))
- **Purpose**: Local recordings library with play/pause audio playback, native WhatsApp / Android Sharesheet sharing via `FileProvider`, and "Check with Meshy AI".

### 14. Meshy AI Review Sheet ([`MeshyAiReviewDialog.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/MeshyAiReviewDialog.kt))
- **Purpose**: Material 3 Modal BottomSheet displaying score badge (`8 / 10 · 80%`), full transcript, per-question spoken feedback, and missing points.
