package com.taskmesh.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskmesh.app.ui.components.TopBarAccountAction

data class ModuleItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val status: AvailabilityStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishInitiativeScreen(
    onBackToHome: () -> Unit,
    onNavigateToVocabularyApp: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val modules = listOf(
        ModuleItem(
            id = "vocabulary",
            title = "Vocabulary",
            description = "General Vocabulary (30D) & One Word Substitution Workbook (59D)",
            icon = Icons.Filled.School,
            status = AvailabilityStatus.AVAILABLE
        ),
        ModuleItem(
            id = "speech",
            title = "Daily Speech",
            description = "Pronunciation practice, fluency drills & daily speech feedback",
            icon = Icons.Filled.Mic,
            status = AvailabilityStatus.COMING_SOON
        ),
        ModuleItem(
            id = "analysis",
            title = "Self Analysis",
            description = "Communication progress, confidence tracking & self assessment",
            icon = Icons.Filled.Assessment,
            status = AvailabilityStatus.COMING_SOON
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "English Initiative",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to TaskMesh Home"
                        )
                    }
                },
                actions = {
                    TopBarAccountAction(onNavigateToProfile = onNavigateToProfile)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a module to continue learning.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(modules.size) { index ->
                val module = modules[index]
                val isAvailable = module.status == AvailabilityStatus.AVAILABLE

                val cardBorder = if (isAvailable) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = cardBorder,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAvailable) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isAvailable) {
                            if (module.id == "vocabulary") {
                                onNavigateToVocabularyApp()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = module.icon,
                                        contentDescription = null,
                                        tint = if (isAvailable) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = module.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (isAvailable) "Available" else "Coming Soon",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = module.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        }

                        if (isAvailable) {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Navigate to " + module.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
