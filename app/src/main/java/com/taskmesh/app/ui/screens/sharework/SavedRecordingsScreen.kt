package com.taskmesh.app.ui.screens.sharework

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskmesh.app.data.DailySpeechDay
import com.taskmesh.app.data.DailySpeechRepository
import com.taskmesh.app.data.sharework.RecordingStorageManager
import com.taskmesh.app.data.sharework.SavedRecording
import com.taskmesh.app.ui.components.TopBarAccountAction
import com.taskmesh.app.ui.screens.dailyspeech.DailySpeechAiReviewSheet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecordingsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val storageManager = remember(context) { RecordingStorageManager(context) }
    var recordingsList by remember { mutableStateOf(storageManager.getAllSavedRecordings()) }

    var activePlayingId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var deleteCandidate by remember { mutableStateOf<SavedRecording?>(null) }
    var activeReviewRecording by remember { mutableStateOf<SavedRecording?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Recordings Library",
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "${recordingsList.size} Saved Audio Recordings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recordingsList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Saved Recordings Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record answers for any worksheet or Daily Speech day to save, play, and check spoken performance with Meshy AI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(recordingsList, key = { it.id }) { item ->
                        val isPlaying = activePlayingId == item.id
                        val file = remember(item.filePath) { File(item.filePath) }
                        val dateStr = remember(item.recordingDate) {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.recordingDate))
                        }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.worksheetTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.selectedGroupsLabel} · $dateStr",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        val min = item.durationSeconds / 60
                                        val sec = item.durationSeconds % 60
                                        Text(
                                            text = String.format(Locale.getDefault(), "%02d:%02d", min, sec),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Play / Pause Button
                                    Button(
                                        onClick = {
                                            if (isPlaying) {
                                                mediaPlayer?.stop()
                                                mediaPlayer?.release()
                                                mediaPlayer = null
                                                activePlayingId = null
                                            } else {
                                                mediaPlayer?.stop()
                                                mediaPlayer?.release()
                                                val mp = MediaPlayer().apply {
                                                    setDataSource(item.filePath)
                                                    prepare()
                                                    setOnCompletionListener {
                                                        activePlayingId = null
                                                    }
                                                    start()
                                                }
                                                mediaPlayer = mp
                                                activePlayingId = item.id
                                            }
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isPlaying) "Pause" else "Play")
                                    }

                                    // Share via WhatsApp / Sharesheet
                                    OutlinedButton(
                                        onClick = {
                                            if (file.exists()) {
                                                val uri = storageManager.getContentUri(file)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "audio/m4a"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    putExtra(Intent.EXTRA_SUBJECT, "TaskMesh Audio Recording")
                                                    putExtra(Intent.EXTRA_TEXT, "Here is my spoken recording for ${item.worksheetTitle} (${item.selectedGroupsLabel}) from TaskMesh.")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Audio Recording"))
                                            }
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete Recording Button
                                    IconButton(onClick = { deleteCandidate = item }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                // Check with Meshy AI Button
                                Button(
                                    onClick = { activeReviewRecording = item },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (item.reviewResultJson != null) "View Meshy AI Review" else "Check with Meshy AI",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete Recording?") },
            text = { Text("Are you sure you want to delete this audio recording? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteCandidate?.let {
                            storageManager.deleteRecording(it.id)
                            recordingsList = storageManager.getAllSavedRecordings()
                        }
                        deleteCandidate = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (activeReviewRecording != null) {
        val rec = activeReviewRecording!!
        val isDailySpeech = rec.worksheetId == "daily_speech_25d" || rec.selectedGroupIds.any { it.startsWith("daily_speech_") }

        if (isDailySpeech) {
            val dayNumber = deriveDayNumberFromRecording(rec)
            val dayData = DailySpeechRepository.getDay(context, dayNumber) ?: DailySpeechDay(
                dayNumber = dayNumber,
                title = rec.selectedGroupsLabel,
                unit = "Daily Speech Challenge",
                summary = rec.selectedGroupsLabel,
                content = "Daily Speech Practice for ${rec.selectedGroupsLabel}"
            )

            DailySpeechAiReviewSheet(
                recording = rec,
                dayData = dayData,
                onDismiss = { activeReviewRecording = null }
            )
        } else {
            MeshyAiReviewDialog(
                recording = rec,
                onDismiss = { activeReviewRecording = null }
            )
        }
    }
}

private fun deriveDayNumberFromRecording(recording: SavedRecording): Int {
    for (groupId in recording.selectedGroupIds) {
        val match = Regex("\\d+").find(groupId)?.value?.toIntOrNull()
        if (match != null) return match.coerceIn(1, 25)
    }
    val labelMatch = Regex("\\d+").find(recording.selectedGroupsLabel)?.value?.toIntOrNull()
    return labelMatch?.coerceIn(1, 25) ?: 1
}
