package com.spellarcade.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpellingListDao {
    @Query("SELECT * FROM spelling_lists ORDER BY displayOrder ASC, updatedAt DESC")
    fun getAllLists(): Flow<List<SpellingListEntity>>

    @Query("SELECT * FROM spelling_lists WHERE isActive = 1 LIMIT 1")
    fun getActiveList(): Flow<SpellingListEntity?>

    @Query("SELECT * FROM spelling_lists WHERE id = :id")
    suspend fun getListById(id: Long): SpellingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: SpellingListEntity): Long

    @Update
    suspend fun update(list: SpellingListEntity)

    @Update
    suspend fun updateAll(lists: List<SpellingListEntity>)

    @Query("UPDATE spelling_lists SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE spelling_lists SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Query("DELETE FROM spelling_lists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM words WHERE listId = :listId")
    suspend fun getWordCount(listId: Long): Int
}

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE listId = :listId ORDER BY text ASC")
    fun getWordsForList(listId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE listId = :listId")
    suspend fun getWordsForListOnce(listId: Long): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun update(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM words WHERE listId = :listId")
    suspend fun deleteAllForList(listId: Long)
}

@Dao
interface WordProgressDao {
    @Query("SELECT * FROM word_progress WHERE wordId = :wordId LIMIT 1")
    suspend fun getProgress(wordId: Long): WordProgressEntity?

    @Query("SELECT * FROM word_progress")
    fun getAllProgress(): Flow<List<WordProgressEntity>>

    @Query("SELECT * FROM word_progress WHERE wordId IN (SELECT id FROM words WHERE listId = :listId)")
    fun getProgressForList(listId: Long): Flow<List<WordProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: WordProgressEntity): Long

    @Update
    suspend fun update(progress: WordProgressEntity)

    @Query("DELETE FROM word_progress WHERE wordId = :wordId")
    suspend fun deleteForWord(wordId: Long)

    @Query("DELETE FROM word_progress")
    suspend fun deleteAllProgress()

    @Query("DELETE FROM word_progress WHERE wordId IN (SELECT id FROM words WHERE listId = :listId)")
    suspend fun deleteForList(listId: Long)
}

@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress WHERE listId = :listId AND gameId = :gameId LIMIT 1")
    suspend fun get(listId: Long, gameId: String): GameProgressEntity?

    @Query("SELECT * FROM game_progress WHERE listId = :listId")
    fun getForList(listId: Long): Flow<List<GameProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: GameProgressEntity): Long

    @Update
    suspend fun update(progress: GameProgressEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AchievementEntity>)

    @Update
    suspend fun update(item: AchievementEntity)
}
