package com.spellarcade.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "spelling_lists")
data class SpellingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val displayOrder: Int = 0
)

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = SpellingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val text: String,
    // Optional vocabulary fields (null/blank = pure spelling word)
    val definition: String? = null,
    val exampleSentence: String? = null,
    val partOfSpeech: String? = null,
    val hint: String? = null,
    val difficulty: Int = 1, // 1–5
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "word_progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class WordProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val correctAttempts: Int = 0,
    val incorrectAttempts: Int = 0,
    val totalAttempts: Int = 0,
    val consecutiveCorrect: Int = 0,
    val masteryLevel: String = "New", // New, Learning, Practicing, Mastered
    val lastPracticed: Long = 0L
)

@Entity(tableName = "game_progress")
data class GameProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val gameId: String,
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val wordsPracticed: Int = 0,
    val lastPlayed: Long = 0L
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val key: String,
    val name: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0L
)
