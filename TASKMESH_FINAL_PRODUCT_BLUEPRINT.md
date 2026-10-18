# TASKMESH — FINAL PRODUCT BLUEPRINT & AUDIT RECONCILIATION

**Date:** October 18, 2026  
**Application ID:** `com.taskmesh.app`  
**Platform:** Native Android (Kotlin + Jetpack Compose + Material 3 + Firebase)  

---

## 1. Audit Reconciliation & Ground Truth

### Screen Count & Component Reconciliation
- **Total User-Facing Screens Cataloged**: 15 distinct screens across 4 primary navigation tiers.
- **Shared Components**: `WordCardItem` (48dp min targets) and `TopBarAccountAction` (universal top bar account icon).
- **Theme & Design Tokens**: [`Theme.kt`](file:///C:/Users/kk824/Downloads/TaskMesh-Native-Android-Project/app/src/main/java/com/taskmesh/app/ui/theme/Theme.kt) already contains the custom calm `Typography` scale (line heights `20.sp` to `38.sp`) and 4dp-grid spacing tokens (`SpaceMicro = 4.dp`, `SpaceCompact = 8.dp`, `SpaceElement = 12.dp`, `SpaceComponent = 16.dp`, `SpaceSection = 20.dp`, `SpaceMajor = 24.dp`, `SpaceHero = 32.dp`).

### Endpoint & Integration Verification
- **Meshy AI Endpoint**: `MeshyAiService.ENDPOINT_URL` is set to the live Google Apps Script proxy (`https://script.google.com/macros/s/AKfycbyySsRbreoJ0Vs7Yo4RaiLtgGUPK4jItsLXU-qyU8lzt6l5Pwit_r-14fQysEpal0y1/exec`).
- **Authentication Header**: Attached via `currentUser.getIdToken(true)` as `Authorization: Bearer <idToken>`.
- **Response Validation**: Validates `0 <= totalScore <= maxScore`, percentages, and required feedback arrays, caching results in `recordings_metadata.json`.

---

## 2. Multi-Initiative Platform Architecture

TaskMesh is designed as a unified personal-growth platform with clear initiative differentiation:

```
                                  ┌───────────────────────────────┐
                                  │   Onboarding / Splash Entry   │
                                  └───────────────┬───────────────┘
                                                  │
                                  ┌───────────────▼───────────────┐
                                  │   TaskMesh Home / Dashboard   │
                                  │   (Continue Learning Hero)    │
                                  └───────────────┬───────────────┘
                                                  │
             ┌────────────────────────────────────┼────────────────────────────────────┐
             │                                    │                                    │
┌────────────▼──────────────┐        ┌────────────▼──────────────┐        ┌────────────▼──────────────┐
│     English Initiative    │        │  DSA Initiative (Future)  │        │ Project Initiative (Future│
│  (Vocab, Speech, Share)   │        │   (Algorithms & Code)     │        │   (Real Engineering)      │
└───────────────────────────┘        └───────────────────────────┘        └───────────────────────────┘
```

---

## 3. Core Experience Modules

1. **Continue Learning Hero**: Prominent dashboard banner on Home screen showing active curriculum progress, streak counter, and a 1-tap action to jump straight back into learning.
2. **English Initiative Hub**: Access point for General Vocabulary (30D), OWSP Workbook (59D), Share Work audio recording, and future Daily Speech / Self Analysis modules.
3. **Share Work Engine**: Single vertically scrollable question sheet for continuous recording, live timer, navigation lock, and native WhatsApp / Sharesheet export.
4. **Meshy AI Spoken Answer Review**: On-demand AI evaluation of user recordings with dynamic answer key generation directly from curriculum datasets.
5. **Profile & Analytics**: User account, provider badges, display name editing, learning dashboard, and local bookmark/favorite tracking.
