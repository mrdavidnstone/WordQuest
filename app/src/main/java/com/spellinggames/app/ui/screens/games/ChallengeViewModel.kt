package com.spellinggames.app.ui.screens.games

import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

enum class ChallengeType {
    ListenSpell, MissingLetters, Scrambled, WhichCorrect, Vocabulary
}

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "challenge") {

    private val _currentChallengeType = MutableStateFlow(ChallengeType.ListenSpell)
    val currentChallengeType: StateFlow<ChallengeType> = _currentChallengeType.asStateFlow()

    // Missing Letters State
    private val _missingPuzzle = MutableStateFlow(MissingPuzzle(emptyList(), emptyList(), 0, emptyList()))
    val missingPuzzle: StateFlow<MissingPuzzle> = _missingPuzzle.asStateFlow()

    // Scrambled State
    private val _tiles = MutableStateFlow<List<Char>>(emptyList())
    val tiles: StateFlow<List<Char>> = _tiles.asStateFlow()
    private val _selected = MutableStateFlow<List<Char>>(emptyList())
    val selected: StateFlow<List<Char>> = _selected.asStateFlow()
    private val _usedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val usedIndices: StateFlow<Set<Int>> = _usedIndices.asStateFlow()

    // Which Correct State
    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options: StateFlow<List<String>> = _options.asStateFlow()

    private val _timerProgress = MutableStateFlow(1f)
    val timerProgress: StateFlow<Float> = _timerProgress.asStateFlow()

    private var allWordsInList = setOf<String>()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                state.activeListId?.let { id ->
                    allWordsInList = repository.getWordsForListOnce(id)
                        .map { it.text.lowercase() }
                        .toSet()
                }
            }
        }
    }

    override fun loadNextWord() {
        super.loadNextWord()
        timerJob?.cancel()
        val nextType = ChallengeType.entries.random()
        _currentChallengeType.value = nextType
        
        // Prepare specific states when word is loaded
        viewModelScope.launch {
            uiState.filter { it.currentWord != null }.first().let { state ->
                prepareChallenge(nextType, state.currentWord?.text ?: "")

                if (nextType == ChallengeType.WhichCorrect || nextType == ChallengeType.Vocabulary) {
                    startTimer()
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val duration = when (uiState.value.overallDifficulty) {
            3 -> 3000L
            2 -> 6000L
            else -> 10000L
        }

        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration) {
                if (uiState.value.showFeedback || uiState.value.isFinished) break
                val elapsed = System.currentTimeMillis() - startTime
                _timerProgress.value = 1f - (elapsed.toFloat() / duration)
                delay(16)
            }
            if (!uiState.value.showFeedback && !uiState.value.isFinished) {
                _timerProgress.value = 0f
                submitAnswer(false)
            }
        }
    }

    private fun prepareChallenge(type: ChallengeType, word: String) {
        when (type) {
            ChallengeType.ListenSpell -> { /* Nothing extra */ }
            ChallengeType.MissingLetters -> prepareMissingLetters(word)
            ChallengeType.Scrambled -> prepareScrambled(word)
            ChallengeType.WhichCorrect -> prepareWhichCorrect(word)
            ChallengeType.Vocabulary -> {
                if (uiState.value.currentWord?.definition.isNullOrBlank()) {
                    _currentChallengeType.value = ChallengeType.WhichCorrect
                    prepareWhichCorrect(word)
                } else {
                    prepareWhichCorrect(word)
                }
            }
        }
    }

    private fun prepareMissingLetters(word: String) {
        if (word.length < 2) {
            _currentChallengeType.value = ChallengeType.ListenSpell
            return
        }
        val numToHide = Random.nextInt(1, (word.length / 2).coerceAtLeast(2)).coerceAtMost(word.length - 1)
        val missingIndices = (word.indices).shuffled().take(numToHide).sorted()
        val maskedWord = word.mapIndexed { i, c -> if (i in missingIndices) '_' else c }
        _missingPuzzle.value = MissingPuzzle(maskedWord, missingIndices, 0, generateChoices(word[missingIndices[0]]))
    }

    private fun prepareScrambled(word: String) {
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val charList = if (requireCaps) word.toList() else word.lowercase().toList()
        _tiles.value = charList.shuffled()
        _selected.value = emptyList()
        _usedIndices.value = emptySet()
    }

    private fun prepareWhichCorrect(word: String) {
        val distractors = mutableListOf<String>()
        val lowercaseTarget = word.lowercase()
        fun addDistractor(d: String) {
            val clean = d.trim().lowercase()
            if (clean != lowercaseTarget && clean !in allWordsInList && clean.isNotEmpty()) distractors.add(d)
        }
        if (word.length > 2) {
            addDistractor(word.dropLast(1) + (word.last() + 1).toChar())
            addDistractor(word.reversed())
            if (word.contains("ie")) addDistractor(word.replace("ie", "ei"))
        }
        while (distractors.size < 3) {
            val chars = word.toMutableList()
            if (chars.size > 1) {
                val i = Random.nextInt(chars.size - 1)
                val tmp = chars[i]; chars[i] = chars[i+1]; chars[i+1] = tmp
            }
            addDistractor(chars.joinToString(""))
        }
        _options.value = (distractors.take(3) + word).distinct().shuffled()
    }

    private fun generateChoices(correct: Char): List<Char> {
        val word = uiState.value.currentWord?.text ?: ""
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val charTarget = if (requireCaps) correct else correct.lowercaseChar()
        val alphabet = if (requireCaps && correct.isUpperCase()) ('A'..'Z').toList() else ('a'..'z').toList()
        val distractors = alphabet.filter { it != charTarget }.shuffled().take(3)
        return (distractors + charTarget).shuffled()
    }

    // Actions for sub-games
    fun selectMissingLetter(letter: Char) {
        val state = _missingPuzzle.value
        val word = uiState.value.currentWord?.text ?: return
        if (state.currentPointer >= state.missingIndices.size) return
        val targetIdx = state.missingIndices[state.currentPointer]
        val correctLetter = word[targetIdx]
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val isCorrect = if (requireCaps) letter == correctLetter else letter.equals(correctLetter, ignoreCase = true)

        if (isCorrect) {
            val newMasked = state.maskedWord.toMutableList()
            newMasked[targetIdx] = word[targetIdx]
            val nextPointer = state.currentPointer + 1
            if (nextPointer >= state.missingIndices.size) {
                submitAnswer(true)
            } else {
                _missingPuzzle.update {
                    it.copy(maskedWord = newMasked, currentPointer = nextPointer, choices = generateChoices(word[state.missingIndices[nextPointer]]))
                }
            }
        } else {
            submitAnswer(false)
        }
    }

    fun tapScrambledTile(index: Int) {
        if (index in _usedIndices.value) return
        val char = _tiles.value[index]
        _selected.value = _selected.value + char
        _usedIndices.value = _usedIndices.value + index
    }

    fun clearScrambled() {
        _selected.value = emptyList()
        _usedIndices.value = emptySet()
    }

    fun checkScrambled() {
        val built = _selected.value.joinToString("")
        submitAnswer(isSpellingCorrect(built))
    }

    fun selectWhichCorrect(option: String) {
        timerJob?.cancel()
        submitAnswer(isSpellingCorrect(option))
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
