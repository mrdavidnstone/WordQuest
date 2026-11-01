package com.spellarcade.app.ui.screens.games

import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MissingPuzzle(
    val maskedWord: List<Char>,
    val missingIndices: List<Int>,
    val currentPointer: Int,
    val choices: List<Char>
)

@HiltViewModel
class MissingLettersViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "missing_letters") {

    private val _puzzle = MutableStateFlow(MissingPuzzle(emptyList(), emptyList(), 0, emptyList()))
    val puzzle: StateFlow<MissingPuzzle> = _puzzle.asStateFlow()

    fun selectLetter(letter: Char) {
        val state = _puzzle.value
        val word = uiState.value.currentWord?.text ?: return
        
        if (state.currentPointer >= state.missingIndices.size) return

        val targetIdx = state.missingIndices[state.currentPointer]
        val correctLetter = word[targetIdx]
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val isCorrect = if (requireCaps) letter == correctLetter else letter.equals(correctLetter, ignoreCase = true)

        if (isCorrect) {
            val newMasked = state.maskedWord.toMutableList()
            newMasked[targetIdx] = correctLetter
            
            val nextPointer = state.currentPointer + 1
            if (nextPointer >= state.missingIndices.size) {
                // Puzzle complete!
                submitAnswer(true)
            } else {
                // Fill next letter
                _puzzle.update {
                    it.copy(
                        maskedWord = newMasked,
                        currentPointer = nextPointer,
                        choices = generateChoices(word[state.missingIndices[nextPointer]])
                    )
                }
            }
        } else {
            // Wrong letter
            submitAnswer(false)
        }
    }

    fun buildPuzzleForCurrentWord() {
        val word = uiState.value.currentWord?.text ?: return
        if (word.length < 2) return

        // Use the global difficulty level from uiState (Easy=1, Medium=2, Hard=3)
        val level = uiState.value.overallDifficulty
        
        val numToHide = when (level) {
            1 -> 1 // Easy: 1 letter
            2 -> (word.length * 0.5).toInt().coerceAtLeast(1) // Medium: 50%
            3 -> (word.length * 0.9).toInt().coerceAtLeast(1) // Hard: 90%
            else -> 1
        }.coerceAtMost(word.length - 1)
        
        // Pick random unique indices
        val missingIndices = (word.indices).shuffled().take(numToHide).sorted()
        
        val maskedWord = word.mapIndexed { i, c ->
            if (i in missingIndices) '_' else c
        }

        val firstMissingIdx = missingIndices[0]
        val choices = generateChoices(word[firstMissingIdx])

        _puzzle.value = MissingPuzzle(
            maskedWord = maskedWord,
            missingIndices = missingIndices,
            currentPointer = 0,
            choices = choices
        )
    }

    private fun generateChoices(correct: Char): List<Char> {
        val word = uiState.value.currentWord?.text ?: ""
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val charTarget = if (requireCaps) correct else correct.lowercaseChar()
        val alphabet = if (requireCaps && correct.isUpperCase()) ('A'..'Z').toList() else ('a'..'z').toList()
        val distractors = alphabet
            .filter { it != charTarget }
            .shuffled()
            .take(3)
        return (distractors + charTarget).shuffled()
    }
}
