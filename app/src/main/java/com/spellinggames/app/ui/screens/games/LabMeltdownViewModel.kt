package com.spellinggames.app.ui.screens.games

import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CharacterMood { CONFIDENT, CONCERNED, NERVOUS, WORRIED, PANICKED, HAPPY, DEFEATED }

data class LabMeltdownUiState(
    val guessedLetters: Set<Char> = emptySet(),
    val wrongGuesses: Int = 0,
    val maxWrong: Int = 6, 
    val maskedWord: String = "",
    val category: String = "Spelling",
    val mood: CharacterMood = CharacterMood.CONFIDENT,
    val score: Int = 0,
    val combo: Int = 0,
    val lastCorrect: Boolean? = null,
    val overallDifficulty: Int = 1
)

@HiltViewModel
class LabMeltdownViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "hangman") {

    private val _labMeltdownState = MutableStateFlow(LabMeltdownUiState())
    val labMeltdownState: StateFlow<LabMeltdownUiState> = _labMeltdownState.asStateFlow()

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val newMaxWrong = when (state.overallDifficulty) {
                    1 -> 8 // Easy: 8 misses
                    2 -> 6 // Medium: 6 misses
                    3 -> 5 // Difficult: 5 misses
                    else -> 8
                }
                _labMeltdownState.update { it.copy(maxWrong = newMaxWrong, overallDifficulty = state.overallDifficulty) }

                val currentWordText = state.currentWord?.text
                if (!currentWordText.isNullOrBlank() && !state.showFeedback && !state.isFinished) {
                    val currentMasked = _labMeltdownState.value.maskedWord
                    if (currentMasked.isBlank() || _labMeltdownState.value.guessedLetters.isNotEmpty() && !currentMasked.contains('_')) {
                        resetLabMeltdownForNewWord(currentWordText, state.listName ?: "Spelling")
                    }
                }
                
                if (state.isFinished) {
                    _labMeltdownState.update { it.copy(mood = if (state.isCorrect == true) CharacterMood.HAPPY else CharacterMood.DEFEATED) }
                }
            }
        }
    }

    override fun startGame() {
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

    override fun loadNextWord() {
        val listId = activeListId ?: return
        viewModelScope.launch {
            val word = repository.getNextWord(listId, usedWordIds)
            if (word == null) {
                if (usedWordIds.isNotEmpty()) {
                    usedWordIds.clear()
                    val retry = repository.getNextWord(listId)
                    if (retry != null) {
                        usedWordIds.add(retry.id)
                        resetLabMeltdownForNewWord(retry.text, uiState.value.listName ?: "Spelling")
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
                    _uiState.update { it.copy(hasWords = false) }
                }
            } else {
                usedWordIds.add(word.id)
                resetLabMeltdownForNewWord(word.text, uiState.value.listName ?: "Spelling")
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

    private fun resetLabMeltdownForNewWord(word: String, listName: String) {
        val initialMasked = word.map { char ->
            if (char == ' ') ' ' else '_'
        }.joinToString("")

        val maxWrong = when (uiState.value.overallDifficulty) {
            1 -> 8 // Easy: 8 misses
            2 -> 6 // Medium: 6 misses
            3 -> 5 // Difficult: 5 misses
            else -> 8
        }

        _labMeltdownState.value = LabMeltdownUiState(
            guessedLetters = emptySet(),
            wrongGuesses = 0,
            maxWrong = maxWrong,
            maskedWord = initialMasked,
            category = listName,
            overallDifficulty = uiState.value.overallDifficulty
        )
    }

    fun guessLetter(letter: Char) {
        if (uiState.value.showFeedback || uiState.value.isFinished) return

        val word = uiState.value.currentWord?.text ?: return
        val requireCaps = uiState.value.requireCapitalLetters

        val isAlreadyGuessed = if (requireCaps) {
            _labMeltdownState.value.guessedLetters.contains(letter)
        } else {
            _labMeltdownState.value.guessedLetters.contains(letter.lowercaseChar()) ||
            _labMeltdownState.value.guessedLetters.contains(letter.uppercaseChar())
        }

        if (isAlreadyGuessed) return

        val isCorrect = if (requireCaps) {
            word.contains(letter)
        } else {
            word.contains(letter, ignoreCase = true)
        }

        _labMeltdownState.update { state ->
            val addedLetters = if (!requireCaps && letter.isLetter()) setOf(letter, letter.lowercaseChar(), letter.uppercaseChar()) else setOf(letter)
            val newGuessed = state.guessedLetters + addedLetters
            val newWrong = if (isCorrect) state.wrongGuesses else state.wrongGuesses + 1
            val newCombo = if (isCorrect) state.combo + 1 else 0
            val newMood = calculateMood(newWrong, state.maxWrong)
            
            state.copy(
                guessedLetters = newGuessed,
                wrongGuesses = newWrong,
                combo = newCombo,
                mood = newMood,
                lastCorrect = isCorrect
            )
        }

        updateMaskedWord()
        checkGameStatus()
    }

    private fun calculateMood(wrong: Int, max: Int): CharacterMood {
        val progress = wrong.toFloat() / max
        return when {
            progress == 0f -> CharacterMood.CONFIDENT
            progress < 0.3f -> CharacterMood.CONCERNED
            progress < 0.6f -> CharacterMood.NERVOUS
            progress < 0.8f -> CharacterMood.WORRIED
            else -> CharacterMood.PANICKED
        }
    }

    private fun updateMaskedWord() {
        val word = uiState.value.currentWord?.text ?: return
        val guessed = _labMeltdownState.value.guessedLetters
        val requireCaps = uiState.value.requireCapitalLetters

        val masked = word.map { char ->
            val isMatch = if (requireCaps) {
                char in guessed
            } else {
                char in guessed || char.lowercaseChar() in guessed || char.uppercaseChar() in guessed
            }
            if (isMatch || char == ' ') char else '_'
        }.joinToString("")

        _labMeltdownState.update { it.copy(maskedWord = masked) }
    }

    private fun checkGameStatus() {
        val state = _labMeltdownState.value
        val word = uiState.value.currentWord?.text ?: return
        val requireCaps = uiState.value.requireCapitalLetters

        val allGuessed = word.all { char ->
            if (char == ' ') {
                true
            } else if (requireCaps) {
                char in state.guessedLetters
            } else {
                char in state.guessedLetters || char.lowercaseChar() in state.guessedLetters || char.uppercaseChar() in state.guessedLetters
            }
        }

        if (allGuessed) {
            submitAnswer(true)
        } else if (state.wrongGuesses >= state.maxWrong) {
            submitAnswer(false)
        }
    }
}
