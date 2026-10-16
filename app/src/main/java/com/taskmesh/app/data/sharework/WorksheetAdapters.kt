package com.taskmesh.app.data.sharework

import android.content.Context
import com.taskmesh.app.data.OwspDatasetRepository
import com.taskmesh.app.data.VocabularyDataset

interface WorksheetAdapter {
    fun getWorksheetItem(context: Context): WorksheetItem
    fun getAvailableGroups(context: Context): List<WorksheetGroup>
    fun getQuestionsForGroups(context: Context, selectedGroupIds: Set<String>): List<WorksheetQuestion>
}

class GeneralVocabAdapter : WorksheetAdapter {
    override fun getWorksheetItem(context: Context): WorksheetItem {
        val totalWords = VocabularyDataset.words.size
        return WorksheetItem(
            id = "general_vocab_30d",
            title = "General Vocabulary",
            description = "30 Days · 210 Words (7 words per day)",
            type = WorksheetType.GENERAL_VOCAB,
            totalGroups = 30,
            totalQuestions = totalWords
        )
    }

    override fun getAvailableGroups(context: Context): List<WorksheetGroup> {
        val allWords = VocabularyDataset.words
        val groups = mutableListOf<WorksheetGroup>()

        for (day in 1..30) {
            val dayWords = allWords.filter { it.dayNumber == day }.sortedBy { it.orderIndex }
            val questions = dayWords.mapIndexed { idx, word ->
                WorksheetQuestion(
                    id = word.id,
                    groupId = "day_$day",
                    groupLabel = "Day $day",
                    questionNumber = idx + 1,
                    primaryText = word.word,
                    secondaryText = "/${word.pronunciation}/ · ${word.hindiMeaning}"
                )
            }
            groups.add(
                WorksheetGroup(
                    id = "day_$day",
                    groupNumber = day,
                    groupLabel = "Day $day",
                    questions = questions
                )
            )
        }
        return groups
    }

    override fun getQuestionsForGroups(context: Context, selectedGroupIds: Set<String>): List<WorksheetQuestion> {
        val allGroups = getAvailableGroups(context)
        val selected = allGroups.filter { selectedGroupIds.contains(it.id) }.sortedBy { it.groupNumber }
        return selected.flatMap { it.questions }
    }
}

class OwspWorksheetAdapter : WorksheetAdapter {
    override fun getWorksheetItem(context: Context): WorksheetItem {
        val dataset = OwspDatasetRepository.loadDataset(context)
        return WorksheetItem(
            id = "owsp_workbook_59d",
            title = "OWSP Workbook",
            description = "59 Days · One Word Substitution Questions",
            type = WorksheetType.OWSP_WORKBOOK,
            totalGroups = dataset.totalDays,
            totalQuestions = dataset.totalQuestions
        )
    }

    override fun getAvailableGroups(context: Context): List<WorksheetGroup> {
        val dataset = OwspDatasetRepository.loadDataset(context)
        val groups = mutableListOf<WorksheetGroup>()

        for (q in dataset.questions) {
            val day = q.dayNumber
            val question = WorksheetQuestion(
                id = q.id,
                groupId = "owsp_day_$day",
                groupLabel = "Day $day",
                questionNumber = q.questionNumber,
                primaryText = q.questionText,
                secondaryText = "Sheet ${q.sheet} · Q${q.questionNumber}"
            )
            groups.add(
                WorksheetGroup(
                    id = "owsp_day_$day",
                    groupNumber = day,
                    groupLabel = "Day $day",
                    questions = listOf(question)
                )
            )
        }
        return groups.sortedBy { it.groupNumber }
    }

    override fun getQuestionsForGroups(context: Context, selectedGroupIds: Set<String>): List<WorksheetQuestion> {
        val allGroups = getAvailableGroups(context)
        val selected = allGroups.filter { selectedGroupIds.contains(it.id) }.sortedBy { it.groupNumber }
        return selected.flatMap { it.questions }
    }
}

object WorksheetRegistry {
    private val adapters = mutableMapOf<String, WorksheetAdapter>()

    init {
        val genAdapter = GeneralVocabAdapter()
        val owspAdapter = OwspWorksheetAdapter()
        adapters["general_vocab_30d"] = genAdapter
        adapters["owsp_workbook_59d"] = owspAdapter
    }

    fun registerAdapter(id: String, adapter: WorksheetAdapter) {
        adapters[id] = adapter
    }

    fun getAllWorksheets(context: Context): List<WorksheetItem> {
        return adapters.values.map { it.getWorksheetItem(context) }
    }

    fun getAdapter(worksheetId: String): WorksheetAdapter? {
        return adapters[worksheetId]
    }
}
