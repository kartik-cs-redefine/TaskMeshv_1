package com.taskmesh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import com.taskmesh.app.data.OwspStudyDayManager
import com.taskmesh.app.data.PreferencesManager
import com.taskmesh.app.data.StudyDayManager
import com.taskmesh.app.data.ThemeMode
import com.taskmesh.app.ui.screens.*
import com.taskmesh.app.ui.theme.TaskMeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)
        val studyDayManager1 = StudyDayManager(prefs)
        val studyDayManager2 = OwspStudyDayManager(prefs)

        setContent {
            var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
            var selectedCourse by remember { mutableStateOf(prefs.getSelectedCourse()) }
            var currentScreen by remember { mutableStateOf("today") }

            var currentDay1 by remember { mutableIntStateOf(prefs.getSelectedDay()) }
            var currentDay2 by remember { mutableIntStateOf(prefs.getOwspSelectedDay()) }
            var showSplash by remember { mutableStateOf(true) }

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

            TaskMeshTheme(themeMode = themeMode) {
                if (showSplash) {
                    SplashScreen(onFinish = { showSplash = false })
                } else if (currentScreen == "see_all_meanings") {
                    OwspSeeAllMeaningsScreen(
                        onBack = { currentScreen = "today" },
                        onNavigateToDay = { day ->
                            currentDay2 = studyDayManager2.setManualStudyDay(day)
                            currentScreen = "today"
                        }
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
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TASKMESH",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = selectedCourse == "general",
                                            onClick = {
                                                selectedCourse = "general"
                                                prefs.setSelectedCourse("general")
                                            },
                                            label = { Text("General (30D)") },
                                            shape = CircleShape,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )

                                        FilterChip(
                                            selected = selectedCourse == "owsp",
                                            onClick = {
                                                selectedCourse = "owsp"
                                                prefs.setSelectedCourse("owsp")
                                            },
                                            label = { Text("Workbook (59D)") },
                                            shape = CircleShape,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
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
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
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
