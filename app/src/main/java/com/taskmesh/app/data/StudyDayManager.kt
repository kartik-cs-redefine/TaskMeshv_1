package com.taskmesh.app.data

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class StudyDayManager(private val prefs: PreferencesManager) {

    companion object {
        val CUTOFF_TIME: LocalTime = LocalTime.of(5, 30)

        fun getMostRecent530Boundary(dateTime: ZonedDateTime): ZonedDateTime {
            val localTime = dateTime.toLocalTime()
            val boundaryDate = if (localTime.isBefore(CUTOFF_TIME)) {
                dateTime.toLocalDate().minusDays(1)
            } else {
                dateTime.toLocalDate()
            }
            return ZonedDateTime.of(boundaryDate, CUTOFF_TIME, dateTime.zone)
        }

        fun getNext530Boundary(dateTime: ZonedDateTime): ZonedDateTime {
            val mostRecent = getMostRecent530Boundary(dateTime)
            return if (dateTime.isBefore(mostRecent)) {
                mostRecent
            } else {
                mostRecent.plusDays(1)
            }
        }
    }

    fun reconcile(
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val currentDay = prefs.getSelectedDay().coerceIn(1, 30)
        val lastBoundaryMillis = prefs.getLastAdvancementBoundary()

        val now = ZonedDateTime.now(clock.withZone(zoneId))
        val currentBoundary = getMostRecent530Boundary(now)
        val currentBoundaryMillis = currentBoundary.toInstant().toEpochMilli()

        if (lastBoundaryMillis == 0L) {
            prefs.setLastAdvancementBoundary(currentBoundaryMillis)
            return currentDay
        }

        if (currentBoundaryMillis <= lastBoundaryMillis) {
            return currentDay
        }

        val lastBoundary = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastBoundaryMillis), zoneId)
        val daysElapsed = ChronoUnit.DAYS.between(lastBoundary.toLocalDate(), currentBoundary.toLocalDate()).toInt()

        val updatedDay = if (daysElapsed > 0 && currentDay < 30) {
            (currentDay + daysElapsed).coerceAtMost(30)
        } else {
            currentDay
        }

        prefs.setSelectedDay(updatedDay)
        prefs.setLastAdvancementBoundary(currentBoundaryMillis)
        return updatedDay
    }

    fun setManualStudyDay(
        day: Int,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val validDay = day.coerceIn(1, 30)
        val now = ZonedDateTime.now(clock.withZone(zoneId))
        val currentBoundary = getMostRecent530Boundary(now)

        prefs.setSelectedDay(validDay)
        prefs.setLastAdvancementBoundary(currentBoundary.toInstant().toEpochMilli())
        return validDay
    }

    fun getNextAdvancementBoundary(
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime {
        val now = ZonedDateTime.now(clock.withZone(zoneId))
        return getNext530Boundary(now)
    }
}
