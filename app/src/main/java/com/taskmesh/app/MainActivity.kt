package com.taskmesh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.taskmesh.app.data.AuthRepository
import com.taskmesh.app.data.OwspStudyDayManager
import com.taskmesh.app.data.PreferencesManager
import com.taskmesh.app.data.StudyDayManager
import com.taskmesh.app.data.UserRepository
import com.taskmesh.app.data.sharework.WorksheetGroup
import com.taskmesh.app.data.sharework.WorksheetItem
import com.taskmesh.app.data.sharework.WorksheetQuestion
import com.taskmesh.app.ui.components.TopBarAccountAction
import com.taskmesh.app.ui.screens.*
import com.taskmesh.app.ui.screens.sharework.*
import com.taskmesh.app.ui.theme.TaskMeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)
        val studyDayManager1 = StudyDayManager(prefs)
        val studyDayManager2 = OwspStudyDayManager(prefs)
        val userRepository = UserRepository()
        val authRepository = AuthRepository(userRepository)

        setContent {
            var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
            var selectedCourse by remember { mutableStateOf(prefs.getSelectedCourse()) }
            var mainDestination by remember { mutableStateOf("initiatives") } // "initiatives", "english_initiative", "vocabulary_app", "profile", "auth", "share_work_worksheets", "share_work_days", "share_work_recording", "share_work_library"
            var previousDestination by remember { mutableStateOf("initiatives") }
            var currentScreen by remember { mutableStateOf("today") } // "today", "journey", "favourites", "settings", "see_all_meanings"

            var currentDay1 by remember { mutableIntStateOf(prefs.getSelectedDay()) }
            var currentDay2 by remember { mutableIntStateOf(prefs.getOwspSelectedDay()) }
            var showSplash by remember { mutableStateOf(true) }
            var showCourseDropdown by remember { mutableStateOf(false) }

            // Share Work Active Selection State
            var activeWorksheet by remember { mutableStateOf<WorksheetItem?>(null) }
            var activeGroups by remember { mutableStateOf<Set<WorksheetGroup>>(emptySet()) }
            var activeQuestions by remember { mutableStateOf<List<WorksheetQuestion>>(emptyList()) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        currentDay1 = studyDayManager1.reconcile()
                        currentDay2 = studyDayManager2.reconcile()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            fun openProfileOrAuth() {
                previousDestination = mainDestination
                if (authRepository.isLoggedIn()) {
                    mainDestination = "profile"
                } else {
                    mainDestination = "auth"
                }
            }

            // System Back Handler logic for multi-level navigation with dynamic return history
            BackHandler(enabled = !showSplash && (mainDestination != "initiatives" || currentScreen == "see_all_meanings")) {
                if (currentScreen == "see_all_meanings") {
                    currentScreen = "today"
                } else if (mainDestination == "profile" || mainDestination == "auth") {
                    mainDestination = previousDestination
                } else if (mainDestination == "share_work_recording") {
                    mainDestination = "share_work_days"
                } else if (mainDestination == "share_work_days") {
                    mainDestination = "share_work_worksheets"
                } else if (mainDestination == "share_work_worksheets" || mainDestination == "share_work_library") {
                    mainDestination = "english_initiative"
                } else if (mainDestination == "vocabulary_app") {
                    mainDestination = "english_initiative"
                } else if (mainDestination == "english_initiative") {
                    mainDestination = "initiatives"
                }
            }

            TaskMeshTheme(themeMode = themeMode) {
                if (showSplash) {
                    SplashScreen(onFinish = { showSplash = false })
                } else if (mainDestination == "profile") {
                    ProfileScreen(
                        authRepository = authRepository,
                        userRepository = userRepository,
                        prefs = prefs,
                        onBack = { mainDestination = previousDestination },
                        onSignOut = { mainDestination = "initiatives" }
                    )
                } else if (mainDestination == "auth") {
                    AuthScreen(
                        authRepository = authRepository,
                        onBack = { mainDestination = previousDestination },
                        onAuthSuccess = { mainDestination = "profile" }
                    )
                } else if (mainDestination == "share_work_worksheets") {
                    WorksheetSelectionScreen(
                        onBack = { mainDestination = "english_initiative" },
                        onSelectWorksheet = { worksheet ->
                            activeWorksheet = worksheet
                            mainDestination = "share_work_days"
                        },
                        onOpenRecordingsLibrary = {
                            mainDestination = "share_work_library"
                        },
                        onNavigateToProfile = { openProfileOrAuth() }
                    )
                } else if (mainDestination == "share_work_days") {
                    val worksheet = activeWorksheet
                    if (worksheet != null) {
                        DaySelectionScreen(
                            worksheetItem = worksheet,
                            onBack = { mainDestination = "share_work_worksheets" },
                            onStartRecording = { item, groups, questions ->
                                activeWorksheet = item
                                activeGroups = groups
                                activeQuestions = questions
                                mainDestination = "share_work_recording"
                            },
                            onNavigateToProfile = { openProfileOrAuth() }
                        )
                    } else {
                        mainDestination = "share_work_worksheets"
                    }
                } else if (mainDestination == "share_work_recording") {
                    val worksheet = activeWorksheet
                    if (worksheet != null) {
                        ShareWorkRecordingScreen(
                            worksheetItem = worksheet,
                            selectedGroups = activeGroups,
                            orderedQuestions = activeQuestions,
                            onBack = { mainDestination = "share_work_days" },
                            onRecordingFinished = {
                                mainDestination = "share_work_library"
                            }
                        )
                    } else {
                        mainDestination = "share_work_worksheets"
                    }
                } else if (mainDestination == "share_work_library") {
                    SavedRecordingsScreen(
                        onBack = { mainDestination = "share_work_worksheets" },
                        onNavigateToProfile = { openProfileOrAuth() }
                    )
                } else if (mainDestination == "initiatives") {
                    InitiativeSelectionScreen(
                        onNavigateToEnglishInitiative = {
                            mainDestination = "english_initiative"
                        },
                        onNavigateToProfile = { openProfileOrAuth() }
                    )
                } else if (mainDestination == "english_initiative") {
                    EnglishInitiativeScreen(
                        onBackToHome = {
                            mainDestination = "initiatives"
                        },
                        onNavigateToVocabularyApp = {
                            mainDestination = "vocabulary_app"
                        },
                        onNavigateToShareWork = {
                            mainDestination = "share_work_worksheets"
                        },
                        onNavigateToProfile = { openProfileOrAuth() }
                    )
                } else if (currentScreen == "see_all_meanings") {
                    OwspSeeAllMeaningsScreen(
                        onBack = { currentScreen = "today" },
                        onNavigateToDay = { day ->
                            currentDay2 = studyDayManager2.setManualStudyDay(day)
                            currentScreen = "today"
                        },
                        onNavigateToProfile = { openProfileOrAuth() }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { mainDestination = "english_initiative" },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back to English Initiative",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = "TASKMESH",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    TopBarAccountAction(onNavigateToProfile = { openProfileOrAuth() })
                                }
                            }
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == "today",
                                    onClick = { currentScreen = "today" },
                                    icon = {
                                        Icon(
                                            if (currentScreen == "today") Icons.Filled.Today else Icons.Outlined.Today,
                                            contentDescription = "Today"
                                        )
                                    },
                                    label = { Text("Today") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "journey",
                                    onClick = { currentScreen = "journey" },
                                    icon = {
                                        Icon(
                                            if (currentScreen == "journey") Icons.Filled.Explore else Icons.Outlined.Explore,
                                            contentDescription = "Journey"
                                        )
                                    },
                                    label = { Text("Journey") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "favourites",
                                    onClick = { currentScreen = "favourites" },
                                    icon = {
                                        Icon(
                                            if (currentScreen == "favourites") Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Favourites"
                                        )
                                    },
                                    label = { Text("Favourites") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "settings",
                                    onClick = { currentScreen = "settings" },
                                    icon = {
                                        Icon(
                                            if (currentScreen == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            // Full-width Course Dropdown Selector Bar directly below TopAppBar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                OutlinedCard(
                                    onClick = { showCourseDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "Active Initiative",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (selectedCourse == "general") "General Vocabulary — 30 Days, 210 Words" else "OWSP Workbook — 59 Days, One Word Substitution",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Course",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showCourseDropdown,
                                    onDismissRequest = { showCourseDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.92f)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "General Vocabulary",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "30 Days, 210 Words",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCourse = "general"
                                            prefs.setSelectedCourse("general")
                                            showCourseDropdown = false
                                        },
                                        trailingIcon = {
                                            if (selectedCourse == "general") {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "OWSP Workbook",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "59 Days, One Word Substitution",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCourse = "owsp"
                                            prefs.setSelectedCourse("owsp")
                                            showCourseDropdown = false
                                        },
                                        trailingIcon = {
                                            if (selectedCourse == "owsp") {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                when (currentScreen) {
                                    "today" -> {
                                        if (selectedCourse == "general") {
                                            HomeScreen(
                                                currentDay = currentDay1,
                                                studyDayManager = studyDayManager1,
                                                onDayChanged = { day ->
                                                    currentDay1 = studyDayManager1.setManualStudyDay(day)
                                                },
                                                prefs = prefs,
                                                onNavigateToJourney = { currentScreen = "journey" },
                                                onNavigateToSettings = { currentScreen = "settings" }
                                            )
                                        } else {
                                            OwspHomeScreen(
                                                currentDay = currentDay2,
                                                studyDayManager = studyDayManager2,
                                                onDayChanged = { day ->
                                                    currentDay2 = studyDayManager2.setManualStudyDay(day)
                                                },
                                                prefs = prefs,
                                                onNavigateToJourney = { currentScreen = "journey" },
                                                onNavigateToSettings = { currentScreen = "settings" },
                                                onNavigateToSeeAllMeanings = { currentScreen = "see_all_meanings" }
                                            )
                                        }
                                    }
                                    "journey" -> {
                                        if (selectedCourse == "general") {
                                            JourneyScreen(
                                                currentDay = currentDay1,
                                                prefs = prefs,
                                                onSelectDay = { day ->
                                                    currentDay1 = studyDayManager1.setManualStudyDay(day)
                                                    currentScreen = "today"
                                                }
                                            )
                                        } else {
                                            OwspJourneyScreen(
                                                currentDay = currentDay2,
                                                prefs = prefs,
                                                onSelectDay = { day ->
                                                    currentDay2 = studyDayManager2.setManualStudyDay(day)
                                                    currentScreen = "today"
                                                }
                                            )
                                        }
                                    }
                                    "favourites" -> FavouritesScreen(
                                        prefs = prefs,
                                        onNavigateToDay = { day ->
                                            if (selectedCourse == "general") {
                                                currentDay1 = studyDayManager1.setManualStudyDay(day)
                                            } else {
                                                currentDay2 = studyDayManager2.setManualStudyDay(day)
                                            }
                                            currentScreen = "today"
                                        }
                                    )
                                    "settings" -> SettingsScreen(
                                        prefs = prefs,
                                        studyDayManager = studyDayManager1,
                                        owspStudyDayManager = studyDayManager2,
                                        currentDay1 = currentDay1,
                                        currentDay2 = currentDay2,
                                        selectedCourse = selectedCourse,
                                        currentThemeMode = themeMode,
                                        onCourseChanged = { course ->
                                            selectedCourse = course
                                            prefs.setSelectedCourse(course)
                                        },
                                        onStudyDayChanged1 = { day ->
                                            currentDay1 = studyDayManager1.setManualStudyDay(day)
                                        },
                                        onStudyDayChanged2 = { day ->
                                            currentDay2 = studyDayManager2.setManualStudyDay(day)
                                        },
                                        onThemeChanged = { mode ->
                                            themeMode = mode
                                            prefs.setThemeMode(mode)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
