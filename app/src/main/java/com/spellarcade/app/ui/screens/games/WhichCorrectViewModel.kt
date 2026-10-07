package com.spellarcade.app.ui.screens.games

import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WhichCorrectViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "which_correct") {

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

        // Start timer when a new word is loaded and feedback is hidden
        viewModelScope.launch {
            uiState.map { it.currentWord to it.showFeedback }
                .distinctUntilChanged()
                .collect { (word, showFeedback) ->
                    if (word != null && !showFeedback && !uiState.value.isFinished && uiState.value.isReady) {
                        startTimer()
                    } else if (showFeedback || uiState.value.isFinished) {
                        timerJob?.cancel()
                    }
                }
        }
    }

    override fun loadNextWord() {
        super.loadNextWord()
        // startTimer() removed here, handled by observer above
    }

    override fun startGame() {
        super.startGame()
        // startTimer() removed here, handled by observer above
    }

    private fun startTimer() {
        timerJob?.cancel()
        val duration = when (uiState.value.overallDifficulty) {
            3 -> 2000L // Hard: 2 seconds
            2 -> 3000L // Medium: 3 seconds
            else -> 5000L // Easy: 5 seconds
        }

        timerJob = viewModelScope.launch {
            _timerProgress.value = 1f
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration) {
                if (uiState.value.showFeedback || uiState.value.isFinished) break
                val elapsed = System.currentTimeMillis() - startTime
                _timerProgress.value = 1f - (elapsed.toFloat() / duration)
                delay(16)
            }
            if (!uiState.value.showFeedback && !uiState.value.isFinished) {
                _timerProgress.value = 0f
                submitAnswer(false) // Time's up!
            }
        }
    }

    fun generateOptions() {
        val word = uiState.value.currentWord?.text ?: return
        val distractors = mutableListOf<String>()
        val lowercaseTarget = word.lowercase()
        
        // Simple common-mistake generators
        fun addDistractor(d: String) {
            val clean = d.trim().lowercase()
            if (clean != lowercaseTarget && clean !in allWordsInList && clean.isNotEmpty()) {
                distractors.add(d)
            }
        }

        if (word.length > 2) {
            addDistractor(word.dropLast(1) + (word.last() + 1).toChar())
            addDistractor(word.reversed().take(word.length))
            if (word.contains("ie") || word.contains("ei")) {
                addDistractor(word.replace("ie", "ei").replace("ei", "ie"))
            }
            addDistractor(word.drop(1) + word.first())
        }

        var attempts = 0
        while (distractors.size < 3 && attempts < 20) {
            attempts++
            val chars = word.toMutableList()
            if (chars.size > 1) {
                val i = Random.nextInt(chars.size - 1)
                val tmp = chars[i]
                chars[i] = chars[i + 1]
                chars[i + 1] = tmp
            }
            addDistractor(chars.joinToString(""))
        }
        
        // If we still can't find enough "incorrect" distractors, 
        // just add some random letters to the end
        while (distractors.size < 3) {
            addDistractor(word + ('a'..'z').random())
        }

        val opts = (distractors.take(3) + word).distinct().shuffled()
        _options.value = opts
    }

    fun select(option: String) {
        timerJob?.cancel()
        val correct = isSpellingCorrect(option)
        submitAnswer(correct)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
