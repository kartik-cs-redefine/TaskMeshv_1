# TASKMESH — COMPLETE SCREEN & COMPONENT INVENTORY

**Date:** October 18, 2026  
**Total Screens Cataloged:** 15 User-Facing Screens + 2 Reusable Core Components  

---

## 1. Complete Screen Catalog

### 1. Splash Screen ([`SplashScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/SplashScreen.kt))
- **Purpose**: Brand app entry & launch sequence.
- **Entry & Navigation**: Launch point -> Auto-navigates to `InitiativeSelectionScreen` after 1.8s delay.
- **States**: Brand logo display -> Smooth transition.
- **UI & Accessibility**: Warm cream background (`#F6F5EE`), centered typography.

### 2. TaskMesh Home / Initiative Selection ([`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt))
- **Purpose**: Central initiative selection hub (English Initiative, DSA, Project).
- **Entry & Navigation**: Opened from Splash. Tapping English Initiative opens `EnglishInitiativeScreen`. Tapping Profile icon opens Profile or Auth screen.
- **States**: English Initiative Available; DSA & Project Initiative "Coming Soon".
- **UI & Accessibility**: 16dp horizontal margins, 18dp card corner radii, 48dp minimum touch targets.

### 3. English Initiative Screen ([`EnglishInitiativeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/EnglishInitiativeScreen.kt))
- **Purpose**: Module selection for English learning (Vocabulary, Daily Speech, Self Analysis, Share Work).
- **Entry & Navigation**: Opened from Initiative Selection. Tapping Vocabulary opens Vocabulary App. Tapping Share Work opens Share Work Worksheets.
- **States**: Vocabulary & Share Work Available; Daily Speech & Self Analysis "Coming Soon".
- **UI & Accessibility**: Features `TopBarAccountAction` and `ShareWorkEntryCard`.

### 4. General Vocabulary Today ([`HomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/HomeScreen.kt))
- **Purpose**: Daily 7-word vocabulary learning interface for General Vocabulary (30D).
- **Entry & Navigation**: Opened when active course is "general". Bottom bar navigates to Journey, Favourites, Settings.
- **States**: Content list, daily progress bar, Learned & Bookmark action toggles.
- **UI & Accessibility**: Uses [`WordCardItem.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/components/WordCardItem.kt) with 20dp card padding and 48dp min action buttons.

### 5. OWSP Workbook MCQ Quiz Today ([`OwspHomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspHomeScreen.kt))
- **Purpose**: Daily One Word Substitution MCQ quiz question and explanation for OWSP Workbook (59D).
- **Entry & Navigation**: Opened when active course is "owsp". Button opens "See All Meanings & Index".
- **States**: Unsubmitted MCQ question, submitted question with correct green & wrong red badges, explanation card.
- **UI & Accessibility**: 48dp MCQ option buttons, clear feedback card.

### 6. Course Dropdown Selector Bar ([`MainActivity.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/MainActivity.kt))
- **Purpose**: Full-width dropdown selector card directly below TopAppBar for switching active courses.
- **Entry & Navigation**: Appears across Vocabulary experience. Tapping opens Material 3 `DropdownMenu`.
- **States**: "General Vocabulary — 30 Days, 210 Words" vs "OWSP Workbook — 59 Days, One Word Substitution".
- **UI & Accessibility**: Checkmark indicators for active option; persists choice across restarts.

### 7. General Vocabulary 30-Day Journey ([`JourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/JourneyScreen.kt))
- **Purpose**: 30-day curriculum progress grid for General Vocabulary.
- **Entry & Navigation**: Opened via bottom navigation "Journey" tab. Tapping any day opens that day's vocabulary.
- **States**: Current day, Completed days, Locked future days.
- **UI & Accessibility**: 3-column grid with 12dp spacing and 48dp day card touch targets.

### 8. OWSP Workbook 59-Day Journey ([`OwspJourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspJourneyScreen.kt))
- **Purpose**: 59-day curriculum progress grid for OWSP Workbook.
- **Entry & Navigation**: Opened via bottom navigation "Journey" tab when course is "owsp".
- **States**: Current, Completed, and Locked days.
- **UI & Accessibility**: 3-column grid with 12dp spacing and 48dp card touch targets.

### 9. See All Meanings & Index ([`OwspSeeAllMeaningsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspSeeAllMeaningsScreen.kt))
- **Purpose**: Full 236-option dictionary search, A-Z letter filters, and 225 PDF A-Z index.
- **Entry & Navigation**: Opened from OWSP Home screen.
- **States**: Tab 0 (236 Vocab Options) vs Tab 1 (225 PDF Index), Live search query, A-Z letter filter, entry detail dialog.
- **UI & Accessibility**: 52dp search bar, `Icons.AutoMirrored.Filled.ArrowBack`, `TopBarAccountAction`.

### 10. Saved Favourites / Bookmarks ([`FavouritesScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/FavouritesScreen.kt))
- **Purpose**: View all bookmarked words across both courses.
- **Entry & Navigation**: Opened via bottom navigation "Favourites" tab. Button opens selected word's day.
- **States**: Empty saved guidance card vs List of saved favourite cards.
- **UI & Accessibility**: 20dp card padding, 48dp un-bookmark icon buttons.

### 11. Settings Screen ([`SettingsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/SettingsScreen.kt))
- **Purpose**: Manage active course, manual study day overrides, TaskMesh Nudge WorkManager notifications, and Theme mode.
- **Entry & Navigation**: Opened via bottom navigation "Settings" tab or top bar settings icon.
- **States**: Light, Dark, System themes; TaskMesh Nudge toggle; Day pickers for both courses.
- **UI & Accessibility**: Grouped Material 3 cards with 16dp spacing and 48dp switch targets.

### 12. User Profile & Dashboard ([`ProfileScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/ProfileScreen.kt))
- **Purpose**: User profile card, account provider badges, display name editing, learning metrics, and sign-out.
- **Entry & Navigation**: Opened via TopBar account icon button.
- **States**: Signed in (Google/Email) vs Guest mode, Profile editing dialog.
- **UI & Accessibility**: Matches Reference 2 target (84dp initials avatar, Google & Calendar badges, chevrons, 54dp sign out button, 100% horizontal center alignment).

### 13. Authentication Screen ([`AuthScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/AuthScreen.kt))
- **Purpose**: Email/Password login, registration, password reset, and Credential Manager Google Sign-In.
- **Entry & Navigation**: Opened when tapping account icon while signed out.
- **States**: Sign In mode vs Register mode, password visibility toggle, error/success status banners.
- **UI & Accessibility**: Material 3 text fields, 50dp primary buttons, Google Sign-In button.

### 14. Share Work Worksheet Selection ([`WorksheetSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/WorksheetSelectionScreen.kt))
- **Purpose**: Choose worksheet (General Vocabulary or OWSP Workbook) or open Saved Recordings Library.
- **Entry & Navigation**: Opened from English Initiative screen.

### 15. Share Work Day Selection ([`DaySelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/DaySelectionScreen.kt))
- **Purpose**: Multi-select days/sections for recording (Single, consecutive, non-consecutive, Select All, Clear).

### 16. Share Work Recording Screen ([`ShareWorkRecordingScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/ShareWorkRecordingScreen.kt))
- **Purpose**: **Single vertically scrollable question sheet containing ALL selected questions across selected days.** Continuous AAC audio recording while scrolling, live timer (`00:42`), mic pulse animation, Stop button, and in-app navigation lock guard.

### 17. Saved Recordings Library ([`SavedRecordingsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/sharework/SavedRecordingsScreen.kt))
- **Purpose**: List saved audio recordings, play/pause audio via `MediaPlayer`, share via WhatsApp / Android Sharesheet (`FileProvider`), delete recording, and "Check with Meshy AI".
