package com.taskmesh.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskmesh.app.data.PreferencesManager
import com.taskmesh.app.data.StudyDayManager
import com.taskmesh.app.data.VocabularyDataset
import com.taskmesh.app.ui.components.WordCardItem

@Composable
fun HomeScreen(
    currentDay: Int,
    studyDayManager: StudyDayManager,
    onDayChanged: (Int) -> Unit,
    prefs: PreferencesManager,
    onNavigateToJourney: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val words = remember(currentDay) { VocabularyDataset.getWordsForDay(currentDay) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val learnedCount = remember(currentDay, refreshTrigger) {
        words.count { prefs.isWordLearned(it.id) }
    }

    val progressFraction = if (words.isNotEmpty()) learnedCount.toFloat() / words.size.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(500),
        label = "DailyProgress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (currentDay >= 30) "DAY 30 OF 30 · JOURNEY COMPLETE" else String.format("DAY %02d OF 30", currentDay),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Change Study Day in Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ready to grow your vocabulary?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Small steps. Stronger English.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$learnedCount of ${words.size} words learned",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        itemsIndexed(
            items = words,
            key = { _, word -> word.id }
        ) { index, word ->
            val isLearned = remember(refreshTrigger, currentDay) { prefs.isWordLearned(word.id) }
            val isFav = remember(refreshTrigger, currentDay) { prefs.isWordFavorite(word.id) }

            WordCardItem(
                word = word,
                index = index + 1,
                isLearned = isLearned,
                isFavorite = isFav,
                onToggleLearned = {
                    prefs.setWordLearned(word.id, !isLearned)
                    refreshTrigger++
                },
                onToggleFavorite = {
                    prefs.setWordFavorite(word.id, !isFav)
                    refreshTrigger++
                }
            )
        }

        item(key = "footer_navigation") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentDay > 1) onDayChanged(currentDay - 1) },
                    enabled = currentDay > 1,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Prev Day")
                }

                Button(
                    onClick = onNavigateToJourney,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1.2f).defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("30-Day Journey")
                }

                OutlinedButton(
                    onClick = { if (currentDay < 30) onDayChanged(currentDay + 1) },
                    enabled = currentDay < 30,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Next Day")
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
