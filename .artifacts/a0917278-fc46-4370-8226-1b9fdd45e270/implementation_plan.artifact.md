# TaskMesh — Daily Speech UX Improvements & Content Formatting Implementation Plan

This plan details the technical architecture and screen-by-screen changes required to implement the three Daily Speech UX and content formatting improvements.

---

## 1. Feature Specifications & Requirements

### Improvement 1: Conditional Meshy AI Button Visibility
- **Current Behavior**: "Check with Meshy AI" button appears at the bottom of the content screen even when no audio recording exists.
- **Proposed Behavior**:
  - In `DailySpeechContentScreen.kt`, query `RecordingStorageManager(context).getAllSavedRecordings()`.
  - Filter recordings matching `worksheetId == "daily_speech_25d"` AND (`selectedGroupIds.contains("daily_speech_day_$currentDayNumber")` OR `selectedGroupsLabel.contains("Day $currentDayNumber")`).
  - Show "Check with Meshy AI" button **only** when `eligibleRecordings.isNotEmpty()`.
  - If multiple recordings exist for that day, present a recording selector sheet before opening the UI placeholder dialog (*"Daily Speech AI Evaluation Coming Soon"*).

### Improvement 2: Explicit "Set Today's Day" Manual Day Selection
- **Current Behavior**: Suggested day based on start timestamp is displayed, but user lacks an explicit "Set Today's Day" action button on the Journey screen.
- **Proposed Behavior**:
  - In `DailySpeechJourneyScreen.kt`, add a "Set Active Day" button in the TopBar / Hero Banner.
  - Opens `SetTodayDayDialog` allowing selection from Day 1 to Day 25.
  - Displays badges: `[SUGGESTED TODAY]` vs `[ACTIVE TODAY]` vs `[COMPLETED]`.
  - Persists selection via `prefs.setDailySpeechSelectedDay(day)` without resetting start date timestamp or completion progress.

### Improvement 3: Structured Daily Speech Content Formatting
- **Current Behavior**: Content is rendered as a single long block of plain text.
- **Proposed Behavior**:
  - Implement `FormattedSpeechContent(content: String)` in `DailySpeechContentScreen.kt`.
  - Parses Q&A blocks, headers, answers, simple explanations, bullet points, and video tasks:
    - **Questions (`Q1.`, `Q2.`, `VIDEO TASK:`)**: Rendered as high-emphasis bold text (`titleLarge.copy(fontWeight = FontWeight.Bold)`).
    - **Answers ("Answer (from study material):")**: Formatted in a soft surface card with a primary accent `"ANSWER"` label and comfortable line height (`22.sp`).
    - **Simple Explanations ("Simple Explanation (for understanding):")**: Formatted in a primary container card with a `"SIMPLE EXPLANATION"` label and readable line height (`24.sp`).

---

## 2. File-by-File Modification Plan

#### [MODIFY] [DailySpeechContentScreen.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/dailyspeech/DailySpeechContentScreen.kt)
- Add recording query for active day using `RecordingStorageManager`.
- Conditionally render "Check with Meshy AI" button only when `dayRecordings.isNotEmpty()`.
- Add `FormattedSpeechContent(content)` parser rendering questions, answers, and explanations in styled cards.

#### [MODIFY] [DailySpeechJourneyScreen.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/dailyspeech/DailySpeechJourneyScreen.kt)
- Add "Set Today's Day" action button in TopBar / Hero banner.
- Implement `SetTodayDayDialog` allowing manual day selection (1..25) with clear `[SUGGESTED]` vs `[ACTIVE TODAY]` vs `[COMPLETED]` badges.

#### [MODIFY] [DailySpeechTest.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/test/java/com/taskmesh/app/data/DailySpeechTest.kt)
- Add test cases verifying:
  - Day-specific recording filter for Meshy AI button eligibility.
  - Manual day selection persistence vs suggested day calculation.
  - Content parsing logic.

---

## 3. Verification Plan

### Automated Tests
- Run `.\gradlew.bat testDebugUnitTest` to execute all 35+ unit tests.

### Build Verification
- Run `.\gradlew.bat assembleDebug` and `.\gradlew.bat assembleRelease`.
