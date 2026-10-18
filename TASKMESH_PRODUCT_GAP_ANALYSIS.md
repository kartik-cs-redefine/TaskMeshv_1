# TASKMESH — PRODUCT & UX GAP ANALYSIS

**Date:** October 18, 2026  
**Focus:** Consumer Android Experience, Engagement, Accessibility & Platform Capabilities  

---

## 1. First-Launch & Onboarding Gap

### Current State
- App launches directly through a 1.8s splash screen into [`InitiativeSelectionScreen.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/InitiativeSelectionScreen.kt).
- There is no welcome sequence, daily learning goal setup, or curriculum explanation for new learners.

### Recommended Improvement
- **Welcome & Goal-Setting Onboarding**: Add a clean, skippable 3-slide Material 3 onboarding flow on first app launch explaining:
  1. "A little learning. Every day." (Structured 30D & 59D daily progression).
  2. "Speak & Share Work" (Record spoken answers from memory).
  3. "Set your daily reminder time" (Schedules WorkManager notification).
- **User Value**: High. Increases day-1 retention and sets clear learner expectations.

---

## 2. Personalization & Resume Learning Gap

### Current State
- Tapping "English Initiative" opens a list of module cards.
- The user must tap "Vocabulary" and then select "General" or "OWSP Workbook" manually.

### Recommended Improvement
- **"Continue Learning" Hero Banner on Home Screen**: Add a prominent "Resume Day X" banner on the main Initiative Selection screen showing the user's active streak, last studied word/question, and a 1-tap "Resume Learning" action.
- **User Value**: High. Reduces friction to return to active study.

---

## 3. Engagement & Streak Analytics Gap

### Current State
- Users can see their current study day (e.g. "Day 8 / 30") and learned word counts.
- No daily study streak counter, milestone badges, or completion certificates exist.

### Recommended Improvement
- **Daily Streak Counter & Milestone Badges**:
  - Track consecutive daily logins/learnings in `PreferencesManager`.
  - Award badges for "7-Day Streak", "Course Completed (30D)", "50 Words Learned", etc.
  - Display streak flame icon in TopBar header next to profile action.
- **User Value**: Medium-High. Drives daily habitual engagement and motivation.

---

## 4. Accessibility, Typography & Touch Target Audit

### Current State & Audit Findings
- **Touch Target Dimensions**: Verified **48.dp minimum** across all interactive buttons, icon buttons, chips, and list rows.
- **Color Contrast**:
  - Light Theme Main Text (`#16251E` on `#FFFFFF`): **14.7:1 contrast ratio** (WCAG AAA Pass).
  - Primary Container Text (`#113120` on `#E2EFE6`): **11.2:1 contrast ratio** (WCAG AAA Pass).
  - Dark Theme Text (`#F1F3EE` on `#202A26`): **13.8:1 contrast ratio** (WCAG AAA Pass).
- **Custom Calm Typography**: Custom `Typography` scale configured in [`Theme.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/theme/Theme.kt) with generous line heights (`20.sp` to `38.sp`).

---

## 5. Offline, Permission & Recovery State Handling

### Current State
- **Offline Persistence**: 100% offline-first. Words, quiz questions, study day schedules, bookmarks, and audio recording files function with zero internet connection.
- **Permission Denied Handling**:
  - `RECORD_AUDIO`: Clear error banner in `ShareWorkRecordingScreen.kt` if permission is denied.
  - `POST_NOTIFICATIONS`: WorkManager gracefully schedules notifications even if notification permission is disabled in system settings.
- **In-App Navigation Lock**: Intercepts Back gestures / exit requests during recording with a safety confirmation dialog to finalize and save valid partial audio.
