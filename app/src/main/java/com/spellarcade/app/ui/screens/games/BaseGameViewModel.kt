package com.spellarcade.app.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.local.SpellingListEntity
import com.spellarcade.app.data.local.WordEntity
import com.spellarcade.app.data.repository.SpellingRepository
import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.ui.screens.profile.Avatar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameUiState(
    val currentWord: WordEntity? = null,
    val score: Int = 0,
    val total: Int = 0,
    val feedback: String? = null,
    val isCorrect: Boolean? = null,
    val showFeedback: Boolean = false,
    val isFinished: Boolean = false,
    val isReady: Boolean = false,
    val listName: String? = null,
    val activeListId: Long? = null,
    val allLists: List<SpellingListEntity> = emptyList(),
    val hasWords: Boolean = false,
    val avatar: Avatar = Avatar(),
    val overallDifficulty: Int = 1,
    val requireCapitalLetters: Boolean = false
)

abstract class BaseGameViewModel(
    protected val repository: SpellingRepository,
    protected val settings: SettingsRepository,
    private val gameId: String
) : ViewModel() {

    protected val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    protected var activeListId: Long? = null
    protected val usedWordIds = mutableSetOf<Long>()
    private var consecutiveCorrect = 0

    init {
        viewModelScope.launch {
            val listFlow = combine(repository.getActiveList(), repository.getAllLists()) { active: SpellingListEntity?, all: List<SpellingListEntity> -> active to all }

            combine(listFlow, settings.avatar, settings.overallDifficulty, settings.requireCapitalLetters) { listData, avatar, diff, requireCaps ->
                val (list, all) = listData
                activeListId = list?.id
                _uiState.update { 
                    it.copy(
                        listName = list?.name,
                        activeListId = list?.id,
                        allLists = all,
                        avatar = avatar,
                        overallDifficulty = diff,
                        requireCapitalLetters = requireCaps
                    ) 
                }
                if (list != null) {
                    val count = repository.getWordCount(list.id)
                    _uiState.update { it.copy(hasWords = count > 0) }
                }
            }.collect { }
        }
    }

    fun isSpellingCorrect(userInput: String): Boolean {
        val targetWord = _uiState.value.currentWord?.text ?: return false
        val requireCaps = _uiState.value.requireCapitalLetters || targetWord.any { it.isUpperCase() }

        return if (requireCaps) {
            userInput.trim() == targetWord.trim()
        } else {
            userInput.trim().equals(targetWord.trim(), ignoreCase = true)
        }
    }

    open fun startGame() {
        if (!_uiState.value.hasWords) return
        usedWordIds.clear()
        _uiState.update { 
            it.copy(
                isReady = true,
                score = 0,
                total = 0,
                isFinished = false,
                showFeedback = false
            ) 
        }
        loadNextWord()
    }

    fun switchList(id: Long) {
        viewModelScope.launch {
            repository.setActiveList(id)
            // Reset game state for the new list
            usedWordIds.clear()
            _uiState.update { 
                it.copy(
                    currentWord = null,
                    score = 0,
                    total = 0,
                    showFeedback = false,
                    isFinished = false,
                    isReady = false
                ) 
            }
        }
    }

    open fun loadNextWord() {
        val listId = activeListId ?: return
        viewModelScope.launch {
            val word = repository.getNextWord(listId, usedWordIds)
            if (word == null) {
                // All words used or empty — finish or reshuffle
                if (usedWordIds.isNotEmpty()) {
                    usedWordIds.clear()
                    val retry = repository.getNextWord(listId)
                    _uiState.update {
                        it.copy(
                            currentWord = retry,
                            showFeedback = false,
                            feedback = null,
                            isCorrect = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(hasWords = false) }
                }
            } else {
                usedWordIds.add(word.id)
                _uiState.update {
                    it.copy(
                        currentWord = word,
                        showFeedback = false,
                        feedback = null,
                        isCorrect = null
                    )
                }
            }
        }
    }

    fun submitAnswer(correct: Boolean) {
        val word = _uiState.value.currentWord ?: return
        viewModelScope.launch {
            repository.recordAttempt(word.id, correct)
            if (correct) {
                consecutiveCorrect++
                settings.addPoints(10)
                if (consecutiveCorrect >= 5) {
                    // streak celebration could be added
                }
            } else {
                consecutiveCorrect = 0
            }

            val newScore = _uiState.value.score + if (correct) 1 else 0
            val newTotal = _uiState.value.total + 1

            _uiState.update {
                it.copy(
                    score = newScore,
                    total = newTotal,
                    isCorrect = correct,
                    showFeedback = true,
                    feedback = if (correct) "⭐ Great job!" else "Almost! The correct spelling is: ${word.text}"
                )
            }

            // Auto advance after short delay handled in UI
        }
    }

    fun finishGame() {
        val listId = activeListId ?: return
        viewModelScope.launch {
            repository.recordGamePlayed(
                listId = listId,
                gameId = gameId,
                score = _uiState.value.score,
                wordsPracticed = _uiState.value.total
            )
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun nextAfterFeedback() {
        val score = _uiState.value.score
        val total = _uiState.value.total
        val missed = total - score

        val maxAllowedMissedWords = when (_uiState.value.overallDifficulty) {
            1 -> 6 // Easy: 6 missed words
            2 -> 5 // Medium: 5 missed words
            3 -> 4 // Hard: 4 missed words
            else -> 6
        }

        if (score >= 7 || missed >= maxAllowedMissedWords || total >= 10) {
            finishGame()
        } else {
            loadNextWord()
        }
    }
}
