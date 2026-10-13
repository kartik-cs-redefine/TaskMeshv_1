package com.taskmesh.app.data

data class VocabularyWord(
    val id: String,
    val dayNumber: Int,
    val orderIndex: Int,
    val word: String,
    val pronunciation: String,
    val hindiMeaning: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val hindiTranslation: String,
    val usageTip: String = ""
)

data class DayInfo(
    val dayNumber: Int,
    val themeTitle: String,
    val category: String,
    val description: String
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
