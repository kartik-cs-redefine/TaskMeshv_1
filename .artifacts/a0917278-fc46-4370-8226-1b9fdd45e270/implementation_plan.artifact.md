# TaskMesh — Phase 3: Full Application UI/UX Audit & Polish Plan

This plan details the screen-by-screen audit, design system component polish, and non-regression verification for Phase 3.

---

## 1. Complete Screen Inventory & Audit Matrix

| # | Screen / Area | Visual & Usability Enhancements | Severity | Implementation Details | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Splash & Onboarding** ([`SplashScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/SplashScreen.kt) & [`OnboardingScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OnboardingScreen.kt)) | Refine onboarding card illustrations, step indicator dot animations, and "Get Started" 52dp button. | Medium | Apply standard 24dp screen padding, 48dp+ touch targets, and clear typography scale. | Pending |
| **2** | **Home Dashboard** ([`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt)) | Elevate "Continue Learning" hero card with 20dp padding, active streak badge, and 1-tap "Resume Study" button. | High | Standardize card shapes (`20.dp`), M3 surface container colors, and 16dp outer screen margins. | Pending |
| **3** | **English Initiative** ([`EnglishInitiativeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/EnglishInitiativeScreen.kt)) | Module cards and `ShareWorkEntryCard` layout spacing. | Medium | Standardize vertical card spacing to 16dp and inner card padding to 20dp. | Pending |
| **4** | **General Vocabulary** ([`HomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/HomeScreen.kt)) | Daily progress header card and [`WordCardItem.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/components/WordCardItem.kt) action button touch targets. | High | Enforce 20dp card padding, 48dp action button targets, and generous line heights for reading. | Pending |
| **5** | **OWSP Workbook MCQ** ([`OwspHomeScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspHomeScreen.kt)) | MCQ option cards and post-submission explanation cards. | High | Refine MCQ option card states (Default, Selected, Correct, Incorrect), 48dp touch targets. | Pending |
| **6** | **Course Dropdown Selector** ([`MainActivity.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/MainActivity.kt)) | Full-width selector card directly below TopAppBar. | Medium | Standardize card padding (`12.dp` / `16.dp`) and dropdown menu item vertical padding (`12.dp`). | Pending |
| **7** | **General & OWSP Journey** ([`JourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/JourneyScreen.kt) & [`OwspJourneyScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspJourneyScreen.kt)) | 30-day and 59-day curriculum progress grids. | Medium | Standardize day progress badges (Current, Completed, Locked) and 12dp grid spacing. | Pending |
| **8** | **Dictionary & Index** ([`OwspSeeAllMeaningsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/OwspSeeAllMeaningsScreen.kt)) | Search field, letter chips, and dictionary cards. | High | 52dp search bar, `Icons.AutoMirrored.Filled.ArrowBack`, expanded A-Z letter chip tap areas. | Pending |
| **9** | **Favourites / Bookmarks** ([`FavouritesScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/FavouritesScreen.kt)) | Bookmark cards and empty state card. | Medium | Standardize card padding to 20dp, 48dp min action buttons, and quick un-favourite toggle. | Pending |
| **10** | **Settings Screen** ([`SettingsScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/SettingsScreen.kt)) | Curriculum, Day Pickers, Nudge, and Theme selection cards. | High | Group settings into M3 cards, 16dp vertical spacing, Android 13+ notification settings card. | Pending |
| **11** | **Profile & Dashboard** ([`ProfileScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/ProfileScreen.kt)) | Reference 2 layout match (84dp avatar, Google & Calendar badges, chevrons, 54dp sign out button). | Low | Maintain 100% horizontal center alignment and spacious card padding. | Pending |
| **12** | **Auth Screen** ([`AuthScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/AuthScreen.kt)) | Login, Registration, Reset, and Google Sign-In. | Medium | Standardize text field heights, password visibility toggles, and Google Sign-In button. | Pending |
| **13** | **Share Work Screens** (`sharework/` screens) | Worksheet Selection, Day Selection, Recording Sheet, Saved Recordings, and Meshy AI Review Sheet. | High | Standardize card shapes (`18.dp` / `20.dp`), system inset padding (`navigationBarsPadding()`), and 48dp action buttons. | Pending |

---

## 2. Proposed Changes & Implementation Steps

### Phase 3.1: Design Tokens & Component Audit
- Verify custom `Typography` scale and 4dp-grid spacing tokens in [`Theme.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/theme/Theme.kt).
- Verify 48dp minimum touch targets on [`WordCardItem.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/components/WordCardItem.kt) and [`TopBarAccountAction.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/components/TopBarAccountAction.kt).

### Phase 3.2: Screen-by-Screen Visual Polish
- Standardize horizontal margins (`16.dp` / `20.dp`), card shapes (`18.dp` / `20.dp`), and system inset padding (`.navigationBarsPadding()`, `.statusBarsPadding()`).
- Polish `OnboardingScreen.kt`, `InitiativeSelectionScreen.kt`, `EnglishInitiativeScreen.kt`, `HomeScreen.kt`, `OwspHomeScreen.kt`, `OwspSeeAllMeaningsScreen.kt`, `FavouritesScreen.kt`, `SettingsScreen.kt`, `ProfileScreen.kt`, `AuthScreen.kt`, and `sharework/` screens.

---

## 3. Non-Regression Safeguards
- 100% preserve all business logic, Firebase Auth, Firestore profile sync, General Vocabulary (30D), OWSP Workbook (59D), MCQ quiz scoring, Share Work audio recording engine, bookmarks, Nudge notifications, SharedPreferences, and release signing setup.

---

## 4. Verification Plan

### Automated Tests
- Execute `.\gradlew.bat testDebugUnitTest` to verify all 33 unit tests pass.

### Build Verification
- Execute `.\gradlew.bat assembleDebug` and `.\gradlew.bat assembleRelease`.
