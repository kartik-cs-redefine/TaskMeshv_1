package com.taskmesh.app.ui.screens.sharework

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskmesh.app.data.sharework.WorksheetGroup
import com.taskmesh.app.data.sharework.WorksheetItem
import com.taskmesh.app.data.sharework.WorksheetQuestion
import com.taskmesh.app.data.sharework.WorksheetRegistry
import com.taskmesh.app.ui.components.TopBarAccountAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySelectionScreen(
    worksheetItem: WorksheetItem,
    onBack: () -> Unit,
    onStartRecording: (WorksheetItem, Set<WorksheetGroup>, List<WorksheetQuestion>) -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val adapter = remember(worksheetItem.id) { WorksheetRegistry.getAdapter(worksheetItem.id) }
    val availableGroups = remember(context, adapter) { adapter?.getAvailableGroups(context) ?: emptyList() }

    val selectedGroupIds = remember { mutableStateListOf<String>() }

    val selectedGroupsSet = remember(selectedGroupIds.toList(), availableGroups) {
        availableGroups.filter { selectedGroupIds.contains(it.id) }.toSet()
    }

    val orderedQuestions = remember(selectedGroupIds.toList(), adapter, context) {
        adapter?.getQuestionsForGroups(context, selectedGroupIds.toSet()) ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Days / Sections",
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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
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
                        Text(
                            text = "${selectedGroupIds.size} days selected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${orderedQuestions.size} questions to record",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedGroupIds.isNotEmpty() && orderedQuestions.isNotEmpty()) {
                                onStartRecording(worksheetItem, selectedGroupsSet, orderedQuestions)
                            }
                        },
                        enabled = selectedGroupIds.isNotEmpty() && orderedQuestions.isNotEmpty(),
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
                            text = "Start Recording Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worksheetItem.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        selectedGroupIds.clear()
                        selectedGroupIds.addAll(availableGroups.map { it.id })
                    }) {
                        Text("Select All")
                    }
                    TextButton(onClick = {
                        selectedGroupIds.clear()
                    }) {
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableGroups, key = { it.id }) { group ->
                    val isSelected = selectedGroupIds.contains(group.id)
                    val cardBorder = if (isSelected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        border = cardBorder,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 52.dp)
                            .clickable {
                                if (isSelected) {
                                    selectedGroupIds.remove(group.id)
                                } else {
                                    selectedGroupIds.add(group.id)
                                }
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = group.groupLabel,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
