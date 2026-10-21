package com.taskmesh.app.data

import android.content.Context
import android.content.SharedPreferences

open class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences? = try {
        context.getSharedPreferences("task_mesh_prefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    // --- First-Launch Onboarding ---
    open fun isOnboardingCompleted(): Boolean = prefs?.getBoolean("onboarding_completed", false) ?: false

    open fun setOnboardingCompleted(completed: Boolean) {
        prefs?.edit()?.putBoolean("onboarding_completed", completed)?.apply()
    }

    // --- Course Selection ---
    open fun getSelectedCourse(): String = prefs?.getString("selected_course", "general") ?: "general"

    open fun setSelectedCourse(courseId: String) {
        prefs?.edit()?.putString("selected_course", courseId)?.apply()
    }

    // --- Course 1 (General Vocabulary 30 Days) ---
    open fun isWordLearned(wordId: String): Boolean = prefs?.getBoolean("learned_$wordId", false) ?: false

    open fun setWordLearned(wordId: String, learned: Boolean) {
        prefs?.edit()?.putBoolean("learned_$wordId", learned)?.apply()
    }

    open fun isWordFavorite(wordId: String): Boolean = prefs?.getBoolean("fav_$wordId", false) ?: false

    open fun setWordFavorite(wordId: String, favorite: Boolean) {
        prefs?.edit()?.putBoolean("fav_$wordId", favorite)?.apply()
    }

    open fun getSelectedDay(): Int = prefs?.getInt("selected_day", 1)?.coerceIn(1, 30) ?: 1

    open fun setSelectedDay(day: Int) {
        prefs?.edit()?.putInt("selected_day", day.coerceIn(1, 30))?.apply()
    }

    open fun getLastAdvancementBoundary(): Long = prefs?.getLong("last_advancement_boundary", 0L) ?: 0L

    open fun setLastAdvancementBoundary(timestampMillis: Long) {
        prefs?.edit()?.putLong("last_advancement_boundary", timestampMillis)?.apply()
    }

    // --- Course 2 (One Word Substitution 59 Days) ---
    open fun isOwspWordLearned(wordId: String): Boolean = prefs?.getBoolean("owsp_learned_$wordId", false) ?: false

    open fun setOwspWordLearned(wordId: String, learned: Boolean) {
        prefs?.edit()?.putBoolean("owsp_learned_$wordId", learned)?.apply()
    }

    open fun isOwspWordFavorite(wordId: String): Boolean = prefs?.getBoolean("owsp_fav_$wordId", false) ?: false

    open fun setOwspWordFavorite(wordId: String, favorite: Boolean) {
        prefs?.edit()?.putBoolean("owsp_fav_$wordId", favorite)?.apply()
    }

    open fun getOwspSelectedDay(): Int = prefs?.getInt("owsp_selected_day", 1)?.coerceIn(1, 59) ?: 1

    open fun setOwspSelectedDay(day: Int) {
        prefs?.edit()?.putInt("owsp_selected_day", day.coerceIn(1, 59))?.apply()
    }

    open fun getOwspLastAdvancementBoundary(): Long = prefs?.getLong("owsp_last_advancement_boundary", 0L) ?: 0L

    open fun setOwspLastAdvancementBoundary(timestampMillis: Long) {
        prefs?.edit()?.putLong("owsp_last_advancement_boundary", timestampMillis)?.apply()
    }

    // --- Course 3 (Daily Speech Speaking Challenge 25 Days) ---
    open fun getDailySpeechStartTimestamp(): Long {
        val stored = prefs?.getLong("daily_speech_start_timestamp", 0L) ?: 0L
        if (stored == 0L) {
            val now = System.currentTimeMillis()
            prefs?.edit()?.putLong("daily_speech_start_timestamp", now)?.apply()
            return now
        }
        return stored
    }

    open fun getDailySpeechSuggestedDay(): Int {
        val startMillis = getDailySpeechStartTimestamp()
        val nowMillis = System.currentTimeMillis()
        val daysElapsed = ((nowMillis - startMillis) / (1000 * 60 * 60 * 24)).toInt()
        return (daysElapsed + 1).coerceIn(1, 25)
    }

    open fun getDailySpeechSelectedDay(): Int = prefs?.getInt("daily_speech_selected_day", getDailySpeechSuggestedDay())?.coerceIn(1, 25) ?: 1

    open fun setDailySpeechSelectedDay(day: Int) {
        prefs?.edit()?.putInt("daily_speech_selected_day", day.coerceIn(1, 25))?.apply()
    }

    open fun isDailySpeechDayCompleted(day: Int): Boolean = prefs?.getBoolean("daily_speech_completed_day_$day", false) ?: false

    open fun setDailySpeechDayCompleted(day: Int, completed: Boolean) {
        prefs?.edit()?.putBoolean("daily_speech_completed_day_$day", completed)?.apply()
    }

    // --- TaskMesh Nudge Notifications ---
    open fun areNotificationsEnabled(): Boolean = prefs?.getBoolean("nudge_notifications_enabled", true) ?: true

    open fun setNotificationsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean("nudge_notifications_enabled", enabled)?.apply()
    }

    open fun getLastNudgeMessageIndex(): Int = prefs?.getInt("last_nudge_message_index", -1) ?: -1

    open fun setLastNudgeMessageIndex(index: Int) {
        prefs?.edit()?.putInt("last_nudge_message_index", index)?.apply()
    }

    // --- Theme Mode ---
    open fun getThemeMode(): ThemeMode {
        val modeStr = prefs?.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    open fun setThemeMode(mode: ThemeMode) {
        prefs?.edit()?.putString("theme_mode", mode.name)?.apply()
    }

    open fun resetAllProgress() {
        val prefsObj = prefs ?: return
        val editor = prefsObj.edit()
        prefsObj.all.keys.filter { it.startsWith("learned_") || it.startsWith("fav_") || it.startsWith("owsp_learned_") || it.startsWith("owsp_fav_") || it.startsWith("daily_speech_") }.forEach {
            editor.remove(it)
        }
        editor.putInt("selected_day", 1)
        editor.putLong("last_advancement_boundary", 0L)
        editor.putInt("owsp_selected_day", 1)
        editor.putLong("owsp_last_advancement_boundary", 0L)
        editor.putInt("daily_speech_selected_day", 1)
        editor.apply()
    }
}
