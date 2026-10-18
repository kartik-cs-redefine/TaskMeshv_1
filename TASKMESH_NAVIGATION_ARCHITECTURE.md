# TASKMESH — NAVIGATION ARCHITECTURE & ROUTING SPECIFICATION

**Date:** October 18, 2026  
**Pattern:** Single-Activity Compose Route State Controller with Dynamic Previous Destination History  

---

## 1. Route Map & State Destinations

| Destination Tag | Screen Composable | Parent Level | Return / Back Behavior |
| :--- | :--- | :--- | :--- |
| `"splash"` | `SplashScreen` | Root | Exits application |
| `"initiatives"` | `InitiativeSelectionScreen` | Tier 1 (Home Hub) | Exits application (Root destination) |
| `"english_initiative"` | `EnglishInitiativeScreen` | Tier 2 (Module Hub) | Returns to `"initiatives"` |
| `"vocabulary_app"` | Main Scaffold (`HomeScreen` / `OwspHomeScreen`) | Tier 3 (Learning Tabs) | Returns to `"english_initiative"` |
| `"see_all_meanings"` | `OwspSeeAllMeaningsScreen` | Tier 4 (Dictionary) | Returns to `"vocabulary_app"` ("today" tab) |
| `"share_work_worksheets"` | `WorksheetSelectionScreen` | Tier 3 (Share Work) | Returns to `"english_initiative"` |
| `"share_work_days"` | `DaySelectionScreen` | Tier 4 (Share Work) | Returns to `"share_work_worksheets"` |
| `"share_work_recording"` | `ShareWorkRecordingScreen` | Tier 4 (Recording Sheet) | Nav Guard Intercepts -> Returns to `"share_work_days"` |
| `"share_work_library"` | `SavedRecordingsScreen` | Tier 4 (Recordings) | Returns to `"share_work_worksheets"` |
| `"profile"` | `ProfileScreen` | TopBar Action | **Dynamic Return**: Restores `previousDestination` |
| `"auth"` | `AuthScreen` | TopBar Action | **Dynamic Return**: Restores `previousDestination` |

---

## 2. In-App Navigation Lock Specification

During active audio recording in `ShareWorkRecordingScreen`:
1. Android Back button and top bar Back arrow trigger an `AlertDialog`:
   > *"Stop & Save Recording? Leaving this screen will stop microphone capture and save your partial recording so far."*
2. If **Cancelled**: Dismisses dialog and recording continues seamlessly without interruption.
3. If **Confirmed**: Calls `ShareWorkSession.stopSession(context)`, finalizes valid partial audio, saves metadata, and restores previous destination.
