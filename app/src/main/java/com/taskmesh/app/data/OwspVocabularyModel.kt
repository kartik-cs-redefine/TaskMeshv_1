package com.taskmesh.app.data

import android.content.Context
import org.json.JSONObject

data class OwspOption(
    val id: String,
    val optionLabel: String,
    val word: String,
    val partOfSpeech: String,
    val isCorrect: Boolean,
    val englishMeaning: String,
    val hindiMeaning: String,
    val exampleSentence: String,
    val hindiTranslation: String,
    val memoryTip: String
)

data class OwspQuestion(
    val id: String,
    val sheet: Int,
    val questionNumber: Int,
    val dayNumber: Int,
    val questionText: String,
    val correctOptionLetter: String,
    val memoryTip: String,
    val options: List<OwspOption>
)

data class OwspAzEntry(
    val word: String,
    val hindiMeaning: String
)

data class OwspVocabularyEntryWithContext(
    val option: OwspOption,
    val dayNumber: Int,
    val questionNumber: Int,
    val sheet: Int,
    val questionText: String
)

data class OwspDatasetModel(
    val courseTitle: String,
    val totalDays: Int,
    val totalQuestions: Int,
    val totalVocabularyEntries: Int,
    val questions: List<OwspQuestion>,
    val azIndex: List<OwspAzEntry>
)

object OwspDatasetRepository {
    private var cachedDataset: OwspDatasetModel? = null

    fun loadDataset(context: Context): OwspDatasetModel {
        cachedDataset?.let { return it }

        val jsonString = context.assets.open("owsp_workbook_dataset.json")
            .bufferedReader()
            .use { it.readText() }

        val rootObj = JSONObject(jsonString)
        val courseTitle = rootObj.optString("courseTitle", "One Word Substitution")
        val totalDays = rootObj.optInt("totalDays", 59)
        val totalQuestions = rootObj.optInt("totalQuestions", 59)
        val totalVocab = rootObj.optInt("totalVocabularyEntries", 236)

        val questionsArray = rootObj.getJSONArray("questions")
        val questionsList = mutableListOf<OwspQuestion>()

        for (i in 0 until questionsArray.length()) {
            val qObj = questionsArray.getJSONObject(i)
            val optsArray = qObj.getJSONArray("options")
            val optsList = mutableListOf<OwspOption>()

            for (j in 0 until optsArray.length()) {
                val optObj = optsArray.getJSONObject(j)
                optsList.add(
                    OwspOption(
                        id = optObj.getString("id"),
                        optionLabel = optObj.optString("optionLabel", ""),
                        word = optObj.optString("word", ""),
                        partOfSpeech = optObj.optString("partOfSpeech", ""),
                        isCorrect = optObj.optBoolean("isCorrect", false),
                        englishMeaning = optObj.optString("englishMeaning", ""),
                        hindiMeaning = optObj.optString("hindiMeaning", ""),
                        exampleSentence = optObj.optString("exampleSentence", ""),
                        hindiTranslation = optObj.optString("hindiTranslation", ""),
                        memoryTip = optObj.optString("memoryTip", "")
                    )
                )
            }

            questionsList.add(
                OwspQuestion(
                    id = qObj.getString("id"),
                    sheet = qObj.getInt("sheet"),
                    questionNumber = qObj.getInt("questionNumber"),
                    dayNumber = qObj.getInt("dayNumber"),
                    questionText = qObj.getString("questionText"),
                    correctOptionLetter = qObj.optString("correctOptionLetter", ""),
                    memoryTip = qObj.optString("memoryTip", ""),
                    options = optsList
                )
            )
        }

        val azArray = rootObj.getJSONArray("azIndex")
        val azList = mutableListOf<OwspAzEntry>()
        for (i in 0 until azArray.length()) {
            val azObj = azArray.getJSONObject(i)
            azList.add(
                OwspAzEntry(
                    word = azObj.optString("word", ""),
                    hindiMeaning = azObj.optString("hindiMeaning", "")
                )
            )
        }

        val model = OwspDatasetModel(
            courseTitle = courseTitle,
            totalDays = totalDays,
            totalQuestions = totalQuestions,
            totalVocabularyEntries = totalVocab,
            questions = questionsList,
            azIndex = azList
        )
        cachedDataset = model
        return model
    }

    fun getQuestionForDay(context: Context, dayNumber: Int): OwspQuestion? {
        val dataset = loadDataset(context)
        return dataset.questions.find { it.dayNumber == dayNumber }
    }

    fun getAllVocabularyEntries(context: Context): List<OwspVocabularyEntryWithContext> {
        val dataset = loadDataset(context)
        val result = mutableListOf<OwspVocabularyEntryWithContext>()
        for (q in dataset.questions) {
            for (opt in q.options) {
                result.add(
                    OwspVocabularyEntryWithContext(
                        option = opt,
                        dayNumber = q.dayNumber,
                        questionNumber = q.questionNumber,
                        sheet = q.sheet,
                        questionText = q.questionText
                    )
                )
            }
        }
        return result
    }
}
