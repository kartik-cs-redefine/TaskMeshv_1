package com.taskmesh.app.data

import kotlin.random.Random

object NudgeMessages {
    val MESSAGES = listOf(
        "Bhai, 6 ghante se English ne tera kya bigaada hai? 😂",
        "Instagram pe PhD kar li? Ab 7 words bhi seekh le! 📚",
        "Vocabulary so rahi hai, tu bhi so raha hai kya? 😭",
        "Bhai, English improve karni hai ya bas app ka icon dekhna hai? 💀",
        "Ek word seekh le, Oxford dictionary nahi banna abhi! 🤓",
        "Tera future fluent English bol raha hai… tu kab bolega? 😂",
        "Reels ka streak maintain hai, learning ka kya scene hai? 👀",
        "Bhai, dimaag ko bhi thoda screen time de de! 🧠",
        "Aaj ka word miss kiya toh Shakespeare sapne mein aayega! 👻",
        "Chal champion, 2 minute English ko bhi de de! 🏆",
        "English se dosti kar le, interview mein kaam aayegi! 😎",
        "Reminder main hoon, padhna tujhe hi padega bhai! 🤝"
    )

    fun getRandomNudgeMessage(lastIndex: Int, random: Random = Random.Default): Pair<Int, String> {
        if (MESSAGES.isEmpty()) return Pair(-1, "")
        if (MESSAGES.size == 1) return Pair(0, MESSAGES[0])

        var newIndex: Int
        do {
            newIndex = random.nextInt(MESSAGES.size)
        } while (newIndex == lastIndex)

        return Pair(newIndex, MESSAGES[newIndex])
    }
}
