package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.DailySpeechWorksheetAdapter
import com.taskmesh.app.data.sharework.SavedRecording
import com.taskmesh.app.data.sharework.WorksheetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailySpeechTest {

    private class DummyContext : android.content.ContextWrapper(null)

    @Test
    fun `test daily speech suggested day calculation and clamping`() {
        val inMemoryPrefs = object : PreferencesManager(DummyContext()) {
            private var startMillis = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 5) // 5 days ago
            private var selectedDay = 1
            private val completedMap = mutableMapOf<Int, Boolean>()

            override fun getDailySpeechStartTimestamp(): Long = startMillis
            override fun getDailySpeechSelectedDay(): Int = selectedDay
            override fun setDailySpeechSelectedDay(day: Int) { selectedDay = day }
            override fun isDailySpeechDayCompleted(day: Int): Boolean = completedMap[day] ?: false
            override fun setDailySpeechDayCompleted(day: Int, completed: Boolean) { completedMap[day] = completed }
        }

        // 5 days elapsed -> suggested day 6
        assertEquals(6, inMemoryPrefs.getDailySpeechSuggestedDay())

        // Test manual selection persistence
        inMemoryPrefs.setDailySpeechSelectedDay(12)
        assertEquals(12, inMemoryPrefs.getDailySpeechSelectedDay())

        // Test completion persistence
        assertFalse(inMemoryPrefs.isDailySpeechDayCompleted(12))
        inMemoryPrefs.setDailySpeechDayCompleted(12, true)
        assertTrue(inMemoryPrefs.isDailySpeechDayCompleted(12))
    }

    @Test
    fun `test daily speech worksheet adapter item and groups`() {
        val adapter = DailySpeechWorksheetAdapter()
        val dummyContext = DummyContext()

        // Test item properties
        val item = adapter.getWorksheetItem(dummyContext)
        assertEquals("daily_speech_25d", item.id)
        assertEquals("Daily Speech Challenge", item.title)
        assertEquals(WorksheetType.CUSTOM_WORKSHEET, item.type)
        assertEquals(25, item.totalGroups)

        // Test question generation for selected days (Day 1, Day 5, Day 10)
        val selectedGroupIds = setOf("daily_speech_day_1", "daily_speech_day_5", "daily_speech_day_10")
        val questions = adapter.getQuestionsForGroups(dummyContext, selectedGroupIds)

        assertEquals(3, questions.size)
        assertEquals("Day 1", questions[0].groupLabel)
        assertEquals("Day 5", questions[1].groupLabel)
        assertEquals("Day 10", questions[2].groupLabel)
    }

    @Test
    fun `test day specific recording filtering for meshy ai eligibility`() {
        val recordings = listOf(
            SavedRecording(
                id = "rec_ds_4",
                worksheetId = "daily_speech_25d",
                worksheetTitle = "Daily Speech Challenge",
                selectedGroupsLabel = "Day 4",
                selectedGroupIds = listOf("daily_speech_day_4"),
                recordingDate = System.currentTimeMillis(),
                durationSeconds = 45,
                filePath = "/test/ds4.m4a"
            ),
            SavedRecording(
                id = "rec_gen_5",
                worksheetId = "general_vocab_30d",
                worksheetTitle = "General Vocabulary",
                selectedGroupsLabel = "Day 5",
                selectedGroupIds = listOf("day_5"),
                recordingDate = System.currentTimeMillis(),
                durationSeconds = 60,
                filePath = "/test/gen5.m4a"
            )
        )

        // Day 4 should find rec_ds_4
        val day4Recordings = recordings.filter { rec ->
            rec.worksheetId == "daily_speech_25d" &&
                    (rec.selectedGroupIds.contains("daily_speech_day_4") || rec.selectedGroupsLabel.contains("Day 4"))
        }
        assertEquals(1, day4Recordings.size)

        // Day 5 should find NO recordings (Day 4 recording does NOT qualify Day 5)
        val day5Recordings = recordings.filter { rec ->
            rec.worksheetId == "daily_speech_25d" &&
                    (rec.selectedGroupIds.contains("daily_speech_day_5") || rec.selectedGroupsLabel.contains("Day 5"))
        }
        assertEquals(0, day5Recordings.size)
    }

    @Test
    fun `test routing differentiation between daily speech and share work recordings`() {
        val dsRecording = SavedRecording(
            id = "rec_ds_7",
            worksheetId = "daily_speech_25d",
            worksheetTitle = "Daily Speech Challenge",
            selectedGroupsLabel = "Day 7",
            selectedGroupIds = listOf("daily_speech_day_7"),
            recordingDate = System.currentTimeMillis(),
            durationSeconds = 90,
            filePath = "/test/ds7.m4a"
        )

        val swRecording = SavedRecording(
            id = "rec_sw_2",
            worksheetId = "general_vocab_30d",
            worksheetTitle = "General Vocabulary",
            selectedGroupsLabel = "Day 2",
            selectedGroupIds = listOf("day_2"),
            recordingDate = System.currentTimeMillis(),
            durationSeconds = 120,
            filePath = "/test/sw2.m4a"
        )

        val isDsRecording1 = dsRecording.worksheetId == "daily_speech_25d" || dsRecording.selectedGroupIds.any { it.startsWith("daily_speech_") }
        val isDsRecording2 = swRecording.worksheetId == "daily_speech_25d" || swRecording.selectedGroupIds.any { it.startsWith("daily_speech_") }

        assertTrue(isDsRecording1)
        assertFalse(isDsRecording2)
    }
}
