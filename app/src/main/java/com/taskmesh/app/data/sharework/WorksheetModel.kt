package com.taskmesh.app.data.sharework

enum class WorksheetType {
    GENERAL_VOCAB,
    OWSP_WORKBOOK,
    CUSTOM_WORKSHEET
}

data class WorksheetItem(
    val id: String,
    val title: String,
    val description: String,
    val type: WorksheetType,
    val totalGroups: Int,
    val totalQuestions: Int
)

data class WorksheetGroup(
    val id: String,
    val groupNumber: Int,
    val groupLabel: String,
    val questions: List<WorksheetQuestion>
)

data class WorksheetQuestion(
    val id: String,
    val groupId: String,
    val groupLabel: String,
    val questionNumber: Int,
    val primaryText: String,
    val secondaryText: String
)

data class SavedRecording(
    val id: String,
    val worksheetId: String,
    val worksheetTitle: String,
    val selectedGroupsLabel: String,
    val selectedGroupIds: List<String> = emptyList(),
    val recordingDate: Long,
    val durationSeconds: Int,
    val filePath: String,
    val fileSizeBytes: Long = 0L,
    val isTemporary: Boolean = false,
    val reviewResultJson: String? = null
)
