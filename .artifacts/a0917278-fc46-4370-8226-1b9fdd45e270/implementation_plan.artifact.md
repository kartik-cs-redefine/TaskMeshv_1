# TaskMesh Verification & Final UI Alignment Plan

This plan details the technical verification steps for the full screen-by-screen audit, design system component validation, and build verification.

## 1. Audit Verification Summary

| Screen / Area | Visual Alignment Status | Touch Targets | Theme Contrast |
| :--- | :--- | :--- | :--- |
| **TaskMesh Home** (`InitiativeSelectionScreen.kt`) | Aligned with standard 16dp horizontal margins & Material 3 card borders | 48dp+ | WCAG AAA Pass |
| **English Initiative** (`EnglishInitiativeScreen.kt`) | Standardized card container tokens & `TopBarAccountAction` | 48dp+ | WCAG AAA Pass |
| **General Vocabulary Today** (`HomeScreen.kt`) | Refined word cards with 48dp min action buttons & 18dp padding | 48dp+ | WCAG AAA Pass |
| **OWSP Workbook MCQ Quiz** (`OwspHomeScreen.kt`) | Clean option cards, feedback banners, and post-submission details | 48dp+ | WCAG AAA Pass |
| **Course Dropdown Selector** (`MainActivity.kt`) | Full-width selector card with dropdown menu & checkmark indicators | 48dp+ | WCAG AAA Pass |
| **Journey Screens** (`JourneyScreen.kt`, `OwspJourneyScreen.kt`) | Standardized day cards, progress badges, and 12dp grid spacing | 48dp+ | WCAG AAA Pass |
| **Dictionary & Index** (`OwspSeeAllMeaningsScreen.kt`) | Replaced deprecated `Icons.Filled.ArrowBack` with `Icons.AutoMirrored.Filled.ArrowBack` | 48dp+ | WCAG AAA Pass |
| **Favourites** (`FavouritesScreen.kt`) | Standardized bookmark list rows and empty state | 48dp+ | WCAG AAA Pass |
| **Settings** (`SettingsScreen.kt`) | Grouped M3 cards, enlarged day picker touch targets | 48dp+ | WCAG AAA Pass |
| **Profile & Dashboard** (`ProfileScreen.kt`) | Spacious Reference 2 layout match (84dp avatar, Google & Calendar badges, 54dp sign out button) | 48dp+ | WCAG AAA Pass |
| **Auth Screen** (`AuthScreen.kt`) | Material 3 text fields, password toggle, Credential Manager Google Sign-In | 48dp+ | WCAG AAA Pass |

---

## 2. Automated Testing & Build Plan

1. Execute `.\gradlew.bat testDebugUnitTest` to verify all 24 unit tests pass without regressions.
2. Execute `.\gradlew.bat assembleDebug` to build the debug APK.
3. Execute `.\gradlew.bat assembleRelease` to confirm release build compilation.
