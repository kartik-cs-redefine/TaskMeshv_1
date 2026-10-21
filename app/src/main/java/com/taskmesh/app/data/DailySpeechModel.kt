package com.taskmesh.app.data

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

data class DailySpeechDay(
    val dayNumber: Int,
    val title: String,
    val unit: String,
    val summary: String,
    val content: String
)

data class DailySpeechDatasetModel(
    val title: String,
    val totalDays: Int,
    val days: List<DailySpeechDay>
)

object DailySpeechRepository {
    private var cachedDataset: DailySpeechDatasetModel? = null

    fun loadDataset(context: Context): DailySpeechDatasetModel {
        cachedDataset?.let { return it }

        val jsonString = try {
            context.assets.open("daily_speech_dataset.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            // JVM Unit test asset loading fallback
            DailySpeechRepository::class.java.classLoader
                ?.getResourceAsStream("daily_speech_dataset.json")
                ?.bufferedReader()
                ?.use { it.readText() } ?: ""
        }

        if (jsonString.isBlank()) {
            return DailySpeechDatasetModel(
                title = "Daily Speech Speaking Challenge",
                totalDays = 25,
                days = (1..25).map { d ->
                    DailySpeechDay(
                        dayNumber = d,
                        title = "Day $d Speaking Topic",
                        unit = "Unit 1: Fundamentals of Technical Communication",
                        summary = "Daily speech speaking topic for Day $d.",
                        content = "Day $d learning material and speaking prompts."
                    )
                }
            )
        }

        val rootObj = JSONObject(jsonString)
        val title = rootObj.optString("title", "Daily Speech Speaking Challenge")
        val totalDays = rootObj.optInt("totalDays", 25)

        val daysArray = rootObj.getJSONArray("days")
        val daysList = mutableListOf<DailySpeechDay>()

        for (i in 0 until daysArray.length()) {
            val dObj = daysArray.getJSONObject(i)
            daysList.add(
                DailySpeechDay(
                    dayNumber = dObj.getInt("dayNumber"),
                    title = dObj.getString("title"),
                    unit = dObj.getString("unit"),
                    summary = dObj.getString("summary"),
                    content = dObj.getString("content")
                )
            )
        }

        val dataset = DailySpeechDatasetModel(
            title = title,
            totalDays = totalDays,
            days = daysList.sortedBy { it.dayNumber }
        )
        cachedDataset = dataset
        return dataset
    }

    fun getDay(context: Context, dayNumber: Int): DailySpeechDay? {
        val dataset = loadDataset(context)
        return dataset.days.find { it.dayNumber == dayNumber.coerceIn(1, 25) }
    }
}
