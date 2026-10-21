package com.taskmesh.app.ui.screens.dailyspeech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskmesh.app.data.DailySpeechRepository
import com.taskmesh.app.data.PreferencesManager
import com.taskmesh.app.data.sharework.RecordingStorageManager
import com.taskmesh.app.data.sharework.SavedRecording
import com.taskmesh.app.data.sharework.WorksheetGroup
import com.taskmesh.app.data.sharework.WorksheetItem
import com.taskmesh.app.data.sharework.WorksheetQuestion
import com.taskmesh.app.data.sharework.WorksheetRegistry
import com.taskmesh.app.ui.components.TopBarAccountAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySpeechContentScreen(
    currentDayNumber: Int,
    prefs: PreferencesManager,
    onBack: () -> Unit,
    onNavigateToDay: (Int) -> Unit,
    onStartRecordingForDay: (WorksheetItem, Set<WorksheetGroup>, List<WorksheetQuestion>) -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val storageManager = remember(context) { RecordingStorageManager(context) }

    val dayData = remember(context, currentDayNumber) {
        DailySpeechRepository.getDay(context, currentDayNumber)
    }

    var isCompleted by remember(currentDayNumber) {
        mutableStateOf(prefs.isDailySpeechDayCompleted(currentDayNumber))
    }

    // Dynamic recording eligibility lookup
    var savedRecordingsForDay by remember(currentDayNumber) {
        mutableStateOf(
            storageManager.getAllSavedRecordings().filter { rec ->
                rec.worksheetId == "daily_speech_25d" &&
                        (rec.selectedGroupIds.contains("daily_speech_day_$currentDayNumber") ||
                                rec.selectedGroupsLabel.contains("Day $currentDayNumber"))
            }
        )
    }

    var showAiComingSoonDialog by remember { mutableStateOf(false) }
    var showRecordingSelectorDialog by remember { mutableStateOf(false) }

    DisposableEffect(currentDayNumber) {
        onDispose {
            savedRecordingsForDay = storageManager.getAllSavedRecordings().filter { rec ->
                rec.worksheetId == "daily_speech_25d" &&
                        (rec.selectedGroupIds.contains("daily_speech_day_$currentDayNumber") ||
                                rec.selectedGroupsLabel.contains("Day $currentDayNumber"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Day $currentDayNumber · Daily Speech",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TopBarAccountAction(onNavigateToProfile = onNavigateToProfile)
                }
            )
        }
    ) { innerPadding ->
        if (dayData == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Day content not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Day Header Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "DAY $currentDayNumber OF 25",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            if (isCompleted) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Completed",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = dayData.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = dayData.unit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Structured Formatted Learning Material Card
                FormattedSpeechContent(content = dayData.content)

                // Practice & Completion Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mark Learning Complete Button
                    OutlinedButton(
                        onClick = {
                            val newCompleted = !isCompleted
                            isCompleted = newCompleted
                            prefs.setDailySpeechDayCompleted(currentDayNumber, newCompleted)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) "Completed · Tap to undo" else "Mark Day $currentDayNumber Complete",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Record Today's Speech Action
                    Button(
                        onClick = {
                            val adapter = WorksheetRegistry.getAdapter("daily_speech_25d")
                            if (adapter != null) {
                                val item = adapter.getWorksheetItem(context)
                                val groupId = "daily_speech_day_$currentDayNumber"
                                val groups = adapter.getAvailableGroups(context).filter { it.id == groupId }.toSet()
                                val questions = adapter.getQuestionsForGroups(context, setOf(groupId))
                                if (groups.isNotEmpty() && questions.isNotEmpty()) {
                                    onStartRecordingForDay(item, groups, questions)
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Day $currentDayNumber Speech Practice",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Check with Meshy AI — ONLY VISIBLE IF SAVED RECORDING EXISTS FOR THIS DAY
                    if (savedRecordingsForDay.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (savedRecordingsForDay.size > 1) {
                                    showRecordingSelectorDialog = true
                                } else {
                                    showAiComingSoonDialog = true
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check with Meshy AI (${savedRecordingsForDay.size} Recording${if (savedRecordingsForDay.size > 1) "s" else ""})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Previous / Next Day Bottom Navigation Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentDayNumber > 1) onNavigateToDay(currentDayNumber - 1) },
                        enabled = currentDayNumber > 1,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Day")
                    }

                    OutlinedButton(
                        onClick = { if (currentDayNumber < 25) onNavigateToDay(currentDayNumber + 1) },
                        enabled = currentDayNumber < 25,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Next Day")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showRecordingSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showRecordingSelectorDialog = false },
            title = { Text("Select Recording for Meshy AI") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    savedRecordingsForDay.forEach { rec ->
                        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(rec.recordingDate))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRecordingSelectorDialog = false
                                    showAiComingSoonDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${rec.durationSeconds}s duration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecordingSelectorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAiComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showAiComingSoonDialog = false },
            title = { Text("Daily Speech AI Evaluation Coming Soon") },
            text = { Text("Meshy AI evaluation for Daily Speech open-ended speaking practice is under active development. Your audio recording for Day $currentDayNumber is safely saved locally.") },
            confirmButton = {
                TextButton(onClick = { showAiComingSoonDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

/**
 * Structured Content Renderer for Daily Speech study material
 */
@Composable
fun FormattedSpeechContent(content: String) {
    val sections = remember(content) {
        content.split(Regex("\n(?=Q\\d+|VIDEO TASK:|EXAM TIP:|TASK INSTRUCTIONS|TEACHER’S EXPLANATION|FORMAT|Source:)|\n---\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        sections.forEach { sectionText ->
            val isQuestion = sectionText.startsWith("Q") || sectionText.startsWith("VIDEO TASK:") || sectionText.startsWith("EXAM TIP:")
            
            if (isQuestion) {
                val parts = sectionText.split(Regex("\n(?=Answer|Simple Explanation|TEACHER’S EXPLANATION)"))
                val promptText = parts.firstOrNull() ?: sectionText

                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = promptText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        for (i in 1 until parts.size) {
                            val part = parts[i].trim()
                            if (part.startsWith("Answer")) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "ANSWER FROM STUDY MATERIAL",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = part.removePrefix("Answer (from the study material):").removePrefix("Answer:").trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else if (part.startsWith("Simple Explanation")) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "SIMPLE EXPLANATION",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = part.removePrefix("Simple Explanation (for your understanding):").removePrefix("Simple Explanation:").trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // General Explanatory / Instruction Block
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = sectionText,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
