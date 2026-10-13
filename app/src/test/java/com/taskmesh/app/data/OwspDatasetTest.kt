package com.taskmesh.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OwspDatasetTest {

    private fun loadJson(): JSONObject {
        var file = File("src/main/assets/owsp_workbook_dataset.json")
        if (!file.exists()) {
            file = File("app/src/main/assets/owsp_workbook_dataset.json")
        }
        assertTrue("owsp_workbook_dataset.json should exist in assets", file.exists())
        val jsonStr = file.readText(Charsets.UTF_8)
        return JSONObject(jsonStr)
    }

    @Test
    fun `test 1 - dataset has exactly 59 questions and 236 options`() {
        val root = loadJson()
        val questions = root.getJSONArray("questions")
        assertEquals(59, questions.length())

        var totalOptions = 0
        val questionIds = mutableSetOf<String>()

        for (i in 0 until questions.length()) {
            val q = questions.getJSONObject(i)
            val qId = q.getString("id")
            questionIds.add(qId)

            val opts = q.getJSONArray("options")
            assertEquals("Each question must have exactly 4 options", 4, opts.length())
            totalOptions += opts.length()
        }

        assertEquals("59 unique question IDs expected", 59, questionIds.size)
        assertEquals("236 total option vocabulary entries expected", 236, totalOptions)
    }

    @Test
    fun `test 2 - sheet 1 has 29 questions and sheet 2 has 30 questions`() {
        val root = loadJson()
        val questions = root.getJSONArray("questions")

        var sheet1Count = 0
        var sheet2Count = 0

        for (i in 0 until questions.length()) {
            val q = questions.getJSONObject(i)
            val sheet = q.getInt("sheet")
            if (sheet == 1) sheet1Count++
            else if (sheet == 2) sheet2Count++
        }

        assertEquals("Sheet 1 should have 29 questions", 29, sheet1Count)
        assertEquals("Sheet 2 should have 30 questions", 30, sheet2Count)
    }

    @Test
    fun `test 3 - day mapping is sequential 1 to 59 with exactly 1 correct option per question`() {
        val root = loadJson()
        val questions = root.getJSONArray("questions")

        for (i in 0 until questions.length()) {
            val q = questions.getJSONObject(i)
            val expectedDay = i + 1
            assertEquals(expectedDay, q.getInt("dayNumber"))

            val opts = q.getJSONArray("options")
            var correctCount = 0
            for (j in 0 until opts.length()) {
                val opt = opts.getJSONObject(j)
                if (opt.getBoolean("isCorrect")) {
                    correctCount++
                }
            }
            assertEquals("Question $expectedDay must have exactly 1 correct option", 1, correctCount)
        }
    }

    @Test
    fun `test 4 - source PDF A-Z index count is 225`() {
        val root = loadJson()
        val azIndex = root.getJSONArray("azIndex")
        assertEquals(225, azIndex.length())
    }

    @Test
    fun `test 5 - general vocabulary dataset remains intact with 210 entries`() {
        val words = VocabularyDataset.words
        assertEquals(210, words.size)
    }
}
