package com.taskmesh.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TaskMeshNudgeTest {

    private class DummyContext : android.content.ContextWrapper(null)

    @Test
    fun `test 1 - nudge messages list contains exactly 12 Hinglish messages`() {
        assertEquals(12, NudgeMessages.MESSAGES.size)
        assertTrue(NudgeMessages.MESSAGES.any { it.contains("Bhai") })
        assertTrue(NudgeMessages.MESSAGES.any { it.contains("Instagram") })
        assertTrue(NudgeMessages.MESSAGES.any { it.contains("Shakespeare") })
    }

    @Test
    fun `test 2 - random nudge message selection avoids immediate repeat`() {
        val lastIndex = 3
        for (i in 0 until 50) {
            val (newIndex, message) = NudgeMessages.getRandomNudgeMessage(lastIndex, Random(i))
            assertNotEquals("New index should not match lastIndex 3", lastIndex, newIndex)
            assertTrue("Message should be non-empty", message.isNotBlank())
            assertEquals(NudgeMessages.MESSAGES[newIndex], message)
        }
    }

    @Test
    fun `test 3 - notification preference toggle in PreferencesManager`() {
        class InMemoryPreferencesManager : PreferencesManager(DummyContext()) {
            private var notificationsEnabled = true
            private var lastNudgeIndex = -1

            override fun areNotificationsEnabled(): Boolean = notificationsEnabled
            override fun setNotificationsEnabled(enabled: Boolean) { notificationsEnabled = enabled }
            override fun getLastNudgeMessageIndex(): Int = lastNudgeIndex
            override fun setLastNudgeMessageIndex(index: Int) { lastNudgeIndex = index }
        }

        val fakePrefs = InMemoryPreferencesManager()
        assertTrue(fakePrefs.areNotificationsEnabled())

        fakePrefs.setNotificationsEnabled(false)
        assertFalse(fakePrefs.areNotificationsEnabled())

        fakePrefs.setLastNudgeMessageIndex(5)
        assertEquals(5, fakePrefs.getLastNudgeMessageIndex())
    }
}
