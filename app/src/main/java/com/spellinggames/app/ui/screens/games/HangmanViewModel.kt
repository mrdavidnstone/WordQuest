package com.spellinggames.app.ui.screens.games

import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CharacterMood { CONFIDENT, CONCERNED, NERVOUS, WORRIED, PANICKED, HAPPY, DEFEATED }

data class HangmanUiState(
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
class HangmanViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "hangman") {

    private val _hangmanState = MutableStateFlow(HangmanUiState())
    val hangmanState: StateFlow<HangmanUiState> = _hangmanState.asStateFlow()

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val newMaxWrong = 6 
                _hangmanState.update { it.copy(maxWrong = newMaxWrong, overallDifficulty = state.overallDifficulty) }

                if (state.currentWord != null && state.currentWord.text.lowercase() != _hangmanState.value.maskedWord.replace(" ", "").lowercase()) {
                    if (!state.showFeedback && !state.isFinished) {
                        resetHangmanForNewWord(state.currentWord.text, state.listName ?: "Spelling")
                    }
                }
                
                if (state.isFinished) {
                    _hangmanState.update { it.copy(mood = if (state.isCorrect == true) CharacterMood.HAPPY else CharacterMood.DEFEATED) }
                }
            }
        }
    }

    private fun resetHangmanForNewWord(word: String, listName: String) {
        _hangmanState.value = HangmanUiState(
            maskedWord = "_".repeat(word.length),
            category = listName
        )
    }

    fun guessLetter(letter: Char) {
        if (uiState.value.showFeedback || uiState.value.isFinished) return

        val word = uiState.value.currentWord?.text ?: return
        val hasCapitals = word.any { it.isUpperCase() }

        val isAlreadyGuessed = if (hasCapitals) {
            _hangmanState.value.guessedLetters.contains(letter)
        } else {
            _hangmanState.value.guessedLetters.contains(letter.lowercaseChar()) ||
            _hangmanState.value.guessedLetters.contains(letter.uppercaseChar())
        }

        if (isAlreadyGuessed) return

        val isCorrect = if (hasCapitals) {
            word.contains(letter)
        } else {
            word.contains(letter) || word.contains(letter.lowercaseChar()) || word.contains(letter.uppercaseChar())
        }

        _hangmanState.update { state ->
            val addedLetters = if (!hasCapitals && letter.isLetter()) setOf(letter, letter.lowercaseChar(), letter.uppercaseChar()) else setOf(letter)
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
        val guessed = _hangmanState.value.guessedLetters
        val hasCapitals = word.any { it.isUpperCase() }

        val masked = word.map { char ->
            val isMatch = if (hasCapitals) {
                char in guessed
            } else {
                char in guessed || char.lowercaseChar() in guessed || char.uppercaseChar() in guessed
            }
            if (isMatch || char == ' ') char else '_'
        }.joinToString("")

        _hangmanState.update { it.copy(maskedWord = masked) }
    }

    private fun checkGameStatus() {
        val state = _hangmanState.value
        val word = uiState.value.currentWord?.text ?: return
        val hasCapitals = word.any { it.isUpperCase() }

        val allGuessed = word.all { char ->
            if (hasCapitals) {
                char in state.guessedLetters || char == ' '
            } else {
                char in state.guessedLetters || char.lowercaseChar() in state.guessedLetters || char.uppercaseChar() in state.guessedLetters || char == ' '
            }
        }

        if (allGuessed) {
            submitAnswer(true)
        } else if (state.wrongGuesses >= state.maxWrong) {
            submitAnswer(false)
        }
    }
}
