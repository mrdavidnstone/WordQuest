package com.spellarcade.app.data.repository

import com.spellarcade.app.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellingRepository @Inject constructor(
    private val listDao: SpellingListDao,
    private val wordDao: WordDao,
    private val progressDao: WordProgressDao,
    private val gameProgressDao: GameProgressDao,
    private val achievementDao: AchievementDao
) {
    // Lists
    fun getAllLists(): Flow<List<SpellingListEntity>> = listDao.getAllLists()
    fun getActiveList(): Flow<SpellingListEntity?> = listDao.getActiveList()
    suspend fun getListById(id: Long): SpellingListEntity? = listDao.getListById(id)

    suspend fun createList(name: String, description: String = ""): Long {
        val id = listDao.insert(
            SpellingListEntity(name = name.trim(), description = description.trim())
        )
        // If this is the first list, make it active
        val current = listDao.getActiveList().first()
        if (current == null) {
            listDao.setActive(id)
        }
        return id
    }

    suspend fun updateList(list: SpellingListEntity) {
        listDao.update(list.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateLists(lists: List<SpellingListEntity>) {
        listDao.updateAll(lists)
    }

    suspend fun deleteList(id: Long) {
        listDao.delete(id)
    }

    suspend fun setActiveList(id: Long) {
        listDao.clearActive()
        listDao.setActive(id)
        listDao.getListById(id)?.let {
            listDao.update(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun duplicateList(id: Long): Long {
        val original = listDao.getListById(id) ?: return -1
        val newId = createList("${original.name} (copy)", original.description)
        val words = wordDao.getWordsForListOnce(id)
        if (words.isNotEmpty()) {
            wordDao.insertAll(words.map { it.copy(id = 0, listId = newId) })
        }
        return newId
    }

    // Words
    fun getWordsForList(listId: Long): Flow<List<WordEntity>> = wordDao.getWordsForList(listId)

    fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    suspend fun getWordsForListOnce(listId: Long): List<WordEntity> = wordDao.getWordsForListOnce(listId)

    suspend fun addWord(listId: Long, text: String): Long {
        val cleaned = text.trim().lowercase()
        if (cleaned.isBlank()) return -1
        return wordDao.insert(WordEntity(listId = listId, text = cleaned))
    }

    suspend fun addVocabulary(listId: Long, word: String, definition: String): Long {
        val cleanedWord = word.trim().lowercase()
        val cleanedDef = definition.trim()
        if (cleanedWord.isBlank() || cleanedDef.isBlank()) return -1
        return wordDao.insert(WordEntity(listId = listId, text = cleanedWord, definition = cleanedDef))
    }

    suspend fun addWords(listId: Long, texts: List<String>) {
        val entities = texts
            .map { line ->
                val parts = line.split("-", limit = 2)
                val word = parts[0].trim().lowercase()
                val definition = if (parts.size > 1) parts[1].trim() else null
                WordEntity(listId = listId, text = word, definition = definition)
            }
            .filter { it.text.isNotBlank() }
            .distinctBy { it.text }
        if (entities.isNotEmpty()) {
            wordDao.insertAll(entities)
        }
    }

    suspend fun updateListWords(listId: Long, words: List<String>) {
        wordDao.deleteAllForList(listId)
        addWords(listId, words)
    }

    suspend fun updateWord(word: WordEntity) {
        wordDao.update(word.copy(text = word.text.trim().lowercase()))
    }

    suspend fun deleteWord(id: Long) {
        progressDao.deleteForWord(id)
        wordDao.delete(id)
    }

    // Progress
    suspend fun recordAttempt(wordId: Long, correct: Boolean) {
        val existing = progressDao.getProgress(wordId)
        val now = System.currentTimeMillis()
        if (existing == null) {
            progressDao.insert(
                WordProgressEntity(
                    wordId = wordId,
                    correctAttempts = if (correct) 1 else 0,
                    incorrectAttempts = if (correct) 0 else 1,
                    totalAttempts = 1,
                    consecutiveCorrect = if (correct) 1 else 0,
                    masteryLevel = if (correct) "Learning" else "New",
                    lastPracticed = now
                )
            )
        } else {
            val correctAttempts = existing.correctAttempts + if (correct) 1 else 0
            val incorrectAttempts = existing.incorrectAttempts + if (correct) 0 else 1
            val total = correctAttempts + incorrectAttempts
            val consecutive = if (correct) existing.consecutiveCorrect + 1 else 0
            val accuracy = if (total > 0) correctAttempts.toFloat() / total else 0f
            val mastery = when {
                consecutive >= 5 && accuracy >= 0.9f -> "Mastered"
                total >= 3 && accuracy >= 0.7f -> "Practicing"
                total >= 1 -> "Learning"
                else -> "New"
            }
            progressDao.update(
                existing.copy(
                    correctAttempts = correctAttempts,
                    incorrectAttempts = incorrectAttempts,
                    totalAttempts = total,
                    consecutiveCorrect = consecutive,
                    masteryLevel = mastery,
                    lastPracticed = now
                )
            )
        }
    }

    fun getProgressForList(listId: Long): Flow<List<WordProgressEntity>> =
        progressDao.getProgressForList(listId)

    fun getProgressForAll(): Flow<List<WordProgressEntity>> =
        progressDao.getAllProgress()

    suspend fun resetWordProgress(wordId: Long) {
        progressDao.deleteForWord(wordId)
    }

    suspend fun resetListProgress(listId: Long) {
        progressDao.deleteForList(listId)
    }

    suspend fun resetAllProgress() {
        progressDao.deleteAllProgress()
    }

    // Game progress
    suspend fun recordGamePlayed(listId: Long, gameId: String, score: Int, wordsPracticed: Int) {
        val existing = gameProgressDao.get(listId, gameId)
        if (existing == null) {
            gameProgressDao.insert(
                GameProgressEntity(
                    listId = listId,
                    gameId = gameId,
                    gamesPlayed = 1,
                    bestScore = score,
                    wordsPracticed = wordsPracticed,
                    lastPlayed = System.currentTimeMillis()
                )
            )
        } else {
            gameProgressDao.update(
                existing.copy(
                    gamesPlayed = existing.gamesPlayed + 1,
                    bestScore = maxOf(existing.bestScore, score),
                    wordsPracticed = existing.wordsPracticed + wordsPracticed,
                    lastPlayed = System.currentTimeMillis()
                )
            )
        }
    }

    fun getGameProgressForList(listId: Long): Flow<List<GameProgressEntity>> =
        gameProgressDao.getForList(listId)

    // Weighted word selection (prioritize struggling words)
    suspend fun getNextWord(listId: Long, excludeIds: Set<Long> = emptySet()): WordEntity? {
        val words = wordDao.getWordsForListOnce(listId)
        if (words.isEmpty()) return null
        val available = words.filter { it.id !in excludeIds }
        if (available.isEmpty()) return words.random()

        // Build weights: lower mastery / more errors = higher weight
        val weighted = available.map { word ->
            val prog = progressDao.getProgress(word.id)
            val weight = when (prog?.masteryLevel) {
                "Mastered" -> 1
                "Practicing" -> 3
                "Learning" -> 6
                else -> 8 // New
            } + (prog?.incorrectAttempts ?: 0) * 2
            word to weight.coerceAtLeast(1)
        }
        val totalWeight = weighted.sumOf { it.second }
        var r = (0 until totalWeight).random()
        for ((word, w) in weighted) {
            if (r < w) return word
            r -= w
        }
        return available.random()
    }

    suspend fun getWordCount(listId: Long): Int = listDao.getWordCount(listId)
}
