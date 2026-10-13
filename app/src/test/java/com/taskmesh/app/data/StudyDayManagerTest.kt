package com.taskmesh.app.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class StudyDayManagerTest {

    private class InMemoryPreferencesManager : PreferencesManager(DummyContext()) {
        private var selectedDay = 1
        private var lastAdvancementBoundary = 0L
        private val learnedMap = mutableMapOf<String, Boolean>()
        private val favMap = mutableMapOf<String, Boolean>()
        private var themeMode = ThemeMode.SYSTEM

        override fun getSelectedDay(): Int = selectedDay
        override fun setSelectedDay(day: Int) { selectedDay = day.coerceIn(1, 30) }
        override fun getLastAdvancementBoundary(): Long = lastAdvancementBoundary
        override fun setLastAdvancementBoundary(timestampMillis: Long) { lastAdvancementBoundary = timestampMillis }
        override fun isWordLearned(wordId: String): Boolean = learnedMap[wordId] ?: false
        override fun setWordLearned(wordId: String, learned: Boolean) { learnedMap[wordId] = learned }
        override fun isWordFavorite(wordId: String): Boolean = favMap[wordId] ?: false
        override fun setWordFavorite(wordId: String, favorite: Boolean) { favMap[wordId] = favorite }
        override fun getThemeMode(): ThemeMode = themeMode
        override fun setThemeMode(mode: ThemeMode) { themeMode = mode }
    }

    private class DummyContext : android.content.ContextWrapper(null)

    private lateinit var fakePrefs: PreferencesManager
    private lateinit var studyDayManager: StudyDayManager
    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Before
    fun setUp() {
        fakePrefs = InMemoryPreferencesManager()
        studyDayManager = StudyDayManager(fakePrefs)
    }

    private fun makeClock(dateTimeString: String): Clock {
        val zdt = ZonedDateTime.parse(dateTimeString)
        return Clock.fixed(zdt.toInstant(), zdt.zone)
    }

    @Test
    fun `test 1 - manual set day 9 at 7pm stays day 9 overnight`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-13T23:00:00+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(9, currentDay)
    }

    @Test
    fun `test 2 - stays day 9 at 5-29 AM next morning`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-14T05:29:59+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(9, currentDay)
    }

    @Test
    fun `test 3 - advances to day 10 at exactly 5-30 AM next morning`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-14T05:30:00+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(10, currentDay)
    }

    @Test
    fun `test 4 - multiple reconciliations at same boundary cause no duplicate advancement`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-14T05:30:00+05:30[Asia/Kolkata]")
        val dayFirst = studyDayManager.reconcile(clock2, zoneId)
        val daySecond = studyDayManager.reconcile(clock2, zoneId)

        val clock3 = makeClock("2026-10-14T12:00:00+05:30[Asia/Kolkata]")
        val dayThird = studyDayManager.reconcile(clock3, zoneId)

        assertEquals(10, dayFirst)
        assertEquals(10, daySecond)
        assertEquals(10, dayThird)
    }

    @Test
    fun `test 5 - manual set day 9 at 4-45 AM advances at 5-30 AM same morning`() {
        val clock1 = makeClock("2026-10-14T04:45:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-14T05:30:00+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(10, currentDay)
    }

    @Test
    fun `test 6 - missed 4 boundaries advances from 9 to 13 up to cap`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-17T08:00:00+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(13, currentDay)
    }

    @Test
    fun `test 7 - day 30 does not advance to 31`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(30, clock1, zoneId)

        val clock2 = makeClock("2026-10-15T08:00:00+05:30[Asia/Kolkata]")
        val currentDay = studyDayManager.reconcile(clock2, zoneId)

        assertEquals(30, currentDay)
    }

    @Test
    fun `test 8 - manual set overrides previous scheduling baseline`() {
        val clock1 = makeClock("2026-10-13T19:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(9, clock1, zoneId)

        val clock2 = makeClock("2026-10-14T08:00:00+05:30[Asia/Kolkata]")
        studyDayManager.setManualStudyDay(15, clock2, zoneId)

        val currentDay = studyDayManager.reconcile(clock2, zoneId)
        assertEquals(15, currentDay)

        val clock3 = makeClock("2026-10-15T05:30:00+05:30[Asia/Kolkata]")
        val nextDay = studyDayManager.reconcile(clock3, zoneId)
        assertEquals(16, nextDay)
    }
}
