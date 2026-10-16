package com.taskmesh.app.data

import com.taskmesh.app.data.sharework.GeneralVocabAdapter
import com.taskmesh.app.data.sharework.WorksheetType
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareWorkAdapterTest {

    @Test
    fun `test general vocabulary adapter item and groups`() {
        val adapter = GeneralVocabAdapter()
        val mockContext = DummyContext()

        val item = adapter.getWorksheetItem(mockContext)

        assertEquals("general_vocab_30d", item.id)
        assertEquals("General Vocabulary", item.title)
        assertEquals(WorksheetType.GENERAL_VOCAB, item.type)
        assertEquals(30, item.totalGroups)
        assertEquals(210, item.totalQuestions)
    }

    @Test
    fun `test non consecutive day selection ordering`() {
        val adapter = GeneralVocabAdapter()
        val mockContext = DummyContext()

        val selectedGroupIds = setOf("day_10", "day_8", "day_12")
        val questions = adapter.getQuestionsForGroups(mockContext, selectedGroupIds)

        assertEquals(21, questions.size) // 3 days * 7 words = 21 questions
        assertEquals("Day 8", questions[0].groupLabel)
        assertEquals("Day 10", questions[7].groupLabel)
        assertEquals("Day 12", questions[14].groupLabel)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
