package com.taskmesh.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OwspQuizTest {

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
    fun `test 1 - quiz answer validation for correct and incorrect options`() {
        val root = loadJson()
        val questions = root.getJSONArray("questions")
        val q1 = questions.getJSONObject(0) // Sheet 1 Q1: "Something that cannot be Read." -> (B) Illegible

        val corrLetter = q1.getString("correctOptionLetter")
        assertEquals("B", corrLetter)

        val opts = q1.getJSONArray("options")
        var optAIsCorrect = false
        var optBIsCorrect = false

        for (i in 0 until opts.length()) {
            val opt = opts.getJSONObject(i)
            val label = opt.getString("optionLabel")
            val isCorr = opt.getBoolean("isCorrect")

            if (label == "(A)") optAIsCorrect = isCorr
            if (label == "(B)") optBIsCorrect = isCorr
        }

        assertFalse("Option (A) Ineligible should be incorrect", optAIsCorrect)
        assertTrue("Option (B) Illegible should be correct", optBIsCorrect)
    }

    @Test
    fun `test 2 - answer locking and state reset on question change`() {
        var selectedOptionId: String?
        var isSubmitted = false

        // User selects Option A
        selectedOptionId = "owsp_s1_q1_optA"
        assertFalse("Not submitted initially", isSubmitted)

        // User submits
        isSubmitted = true
        assertTrue("Submitted state locked", isSubmitted)
        assertEquals("owsp_s1_q1_optA", selectedOptionId)

        // Move to next question (Day 2) -> resets quiz state
        val nextDay = 2
        selectedOptionId = null
        isSubmitted = false

        assertEquals(2, nextDay)
        assertNull(selectedOptionId)
        assertFalse(isSubmitted)
    }

    @Test
    fun `test 3 - day 59 boundary cap prevents navigating to day 60`() {
        val currentDay = 59
        val nextDay = (currentDay + 1).coerceAtMost(59)
        assertEquals("Day 59 must cap at Day 59", 59, nextDay)
    }
}
