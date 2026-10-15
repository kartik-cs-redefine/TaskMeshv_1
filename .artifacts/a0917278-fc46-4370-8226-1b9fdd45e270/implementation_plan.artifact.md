# TaskMesh Profile Dashboard Visual Match Implementation Plan

This plan details the technical refinements to match the target reference screenshot (`Reference 2`) for the Profile & Dashboard screen.

## Proposed Changes

### Component 1: ProfileScreen.kt Redesign

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/screens/ProfileScreen.kt)
- **Profile Header Card**:
  - Increase horizontal margin to `20.dp`.
  - Increase internal card padding to `24.dp`.
  - Increase avatar size to `84.dp` with bold headline typography.
  - Display Name with Edit icon (`Icons.Filled.Edit`).
  - Account provider badge with Google icon (`Icons.Filled.GMobiledata` or custom vector) and "Joined [Month Year]" badge with Calendar icon (`Icons.Filled.CalendarMonth`).
- **Learning Dashboard Heading**:
  - Prominent heading with generous top/bottom spacing.
- **Progress Cards (General Vocabulary & OWSP Workbook)**:
  - Spacious card padding (`20.dp`).
  - Left: Icon in soft sage container + Course Title.
  - Right: Day text ("Day X / 30") + Chevron right icon (`Icons.Filled.ChevronRight`).
  - Progress bar height `10.dp` with smooth rounded corners.
  - Bottom row: Learned count on left, completion percentage on right.
- **Favourites Card**:
  - Left: Bookmark icon in soft sage container + "X Favourites Saved" + breakdown subtitle.
  - Right: Chevron right icon (`Icons.Filled.ChevronRight`).
- **Sign Out Button**:
  - Height `54.dp` with red outline, bold text, and Logout icon.

---

### Component 2: Build & Test Verification

- Run `.\gradlew.bat testDebugUnitTest` to verify all unit tests pass.
- Execute `.\gradlew.bat assembleDebug` to confirm clean compilation.
