package com.taskmesh.app.data

import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class OwspStudyDayManager(private val prefs: PreferencesManager) {

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
        val currentDay = prefs.getOwspSelectedDay().coerceIn(1, 59)
        val lastBoundaryMillis = prefs.getOwspLastAdvancementBoundary()

        val now = ZonedDateTime.now(clock.withZone(zoneId))
        val currentBoundary = getMostRecent530Boundary(now)
        val currentBoundaryMillis = currentBoundary.toInstant().toEpochMilli()

        if (lastBoundaryMillis == 0L) {
            prefs.setOwspLastAdvancementBoundary(currentBoundaryMillis)
            return currentDay
        }

        if (currentBoundaryMillis <= lastBoundaryMillis) {
            return currentDay
        }

        val lastBoundary = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastBoundaryMillis), zoneId)
        val daysElapsed = ChronoUnit.DAYS.between(lastBoundary.toLocalDate(), currentBoundary.toLocalDate()).toInt()

        val updatedDay = if (daysElapsed > 0 && currentDay < 59) {
            (currentDay + daysElapsed).coerceAtMost(59)
        } else {
            currentDay
        }

        prefs.setOwspSelectedDay(updatedDay)
        prefs.setOwspLastAdvancementBoundary(currentBoundaryMillis)
        return updatedDay
    }

    fun setManualStudyDay(
        day: Int,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val validDay = day.coerceIn(1, 59)
        val now = ZonedDateTime.now(clock.withZone(zoneId))
        val currentBoundary = getMostRecent530Boundary(now)

        prefs.setOwspSelectedDay(validDay)
        prefs.setOwspLastAdvancementBoundary(currentBoundary.toInstant().toEpochMilli())
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
