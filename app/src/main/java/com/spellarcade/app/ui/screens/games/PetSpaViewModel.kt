package com.spellarcade.app.ui.screens.games

import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PetSpaPhase {
    SPELLING,
    GROOMING,
    ROUND_COMPLETE
}

enum class GroomingTool(val title: String, val emoji: String, val description: String) {
    WASH("Wash", "🚿", "Spray with warm water"),
    SOAP("Soap", "🧼", "Apply bubbly soap and scrub"),
    RINSE("Rinse", "💧", "Rinse off all the soap"),
    DRY("Dry", "💨", "Blow dry fluffy"),
    BRUSH("Brush", "🪮", "Brush and style coat")
}

data class PetSpaUiState(
    val guessedLetters: Set<Char> = emptySet(),
    val wrongGuesses: Int = 0,
    val maxWrong: Int = 4,
    val maskedWord: String = "",
    val phase: PetSpaPhase = PetSpaPhase.SPELLING,
    val completedGroomingTools: Set<GroomingTool> = emptySet(),
    val activeGroomingTool: GroomingTool? = null,
    val petName: String = "Fluffy the Puppy",
    val petEmoji: String = "🐶",
    val overallDifficulty: Int = 1
)

@HiltViewModel
class PetSpaViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "pet_spa") {

    private val _petSpaState = MutableStateFlow(PetSpaUiState())
    val petSpaState: StateFlow<PetSpaUiState> = _petSpaState.asStateFlow()

    private val pets = listOf(
        "Fluffy the Puppy" to "🐶",
        "Whiskers the Kitten" to "🐱",
        "Pippin the Bunny" to "🐰",
        "Coco the Panda" to "🐼",
        "Sparky the Fox" to "🦊"
    )

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val newMaxWrong = when (state.overallDifficulty) {
                    1 -> 5 // Easy: 5
                    2 -> 4 // Medium: 4
                    3 -> 3 // Hard: 3
                    else -> 4
                }
                _petSpaState.update { it.copy(maxWrong = newMaxWrong, overallDifficulty = state.overallDifficulty) }

                val currentWordText = state.currentWord?.text
                if (!currentWordText.isNullOrBlank() && !state.showFeedback && !state.isFinished && _petSpaState.value.maskedWord.isBlank()) {
                    resetPetSpaForNewWord(currentWordText)
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
                        resetPetSpaForNewWord(retry.text)
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
                resetPetSpaForNewWord(word.text)
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

    private fun resetPetSpaForNewWord(word: String) {
        val initialMasked = word.map { char ->
            if (char == ' ') ' ' else '_'
        }.joinToString("")

        val maxWrong = when (uiState.value.overallDifficulty) {
            1 -> 5
            2 -> 4
            3 -> 3
            else -> 4
        }

        val randomPet = pets.random()

        _petSpaState.value = PetSpaUiState(
            guessedLetters = emptySet(),
            wrongGuesses = 0,
            maxWrong = maxWrong,
            maskedWord = initialMasked,
            phase = PetSpaPhase.SPELLING,
            completedGroomingTools = emptySet(),
            activeGroomingTool = null,
            petName = randomPet.first,
            petEmoji = randomPet.second,
            overallDifficulty = uiState.value.overallDifficulty
        )
    }

    fun guessLetter(letter: Char) {
        val state = _petSpaState.value
        if (uiState.value.showFeedback || uiState.value.isFinished || state.phase != PetSpaPhase.SPELLING) return

        val word = uiState.value.currentWord?.text ?: return
        val requireCaps = uiState.value.requireCapitalLetters

        val isAlreadyGuessed = if (requireCaps) {
            state.guessedLetters.contains(letter)
        } else {
            state.guessedLetters.contains(letter.lowercaseChar()) ||
            state.guessedLetters.contains(letter.uppercaseChar())
        }

        if (isAlreadyGuessed) return

        val isCorrect = if (requireCaps) {
            word.contains(letter)
        } else {
            word.contains(letter, ignoreCase = true)
        }

        _petSpaState.update { st ->
            val addedLetters = if (!requireCaps && letter.isLetter()) setOf(letter, letter.lowercaseChar(), letter.uppercaseChar()) else setOf(letter)
            val newGuessed = st.guessedLetters + addedLetters
            val newWrong = if (isCorrect) st.wrongGuesses else st.wrongGuesses + 1
            st.copy(
                guessedLetters = newGuessed,
                wrongGuesses = newWrong
            )
        }

        updateMaskedWord()
        checkSpellingStatus()
    }

    private fun updateMaskedWord() {
        val word = uiState.value.currentWord?.text ?: return
        val guessed = _petSpaState.value.guessedLetters
        val requireCaps = uiState.value.requireCapitalLetters

        val masked = word.map { char ->
            val isMatch = if (requireCaps) {
                char in guessed
            } else {
                char in guessed || char.lowercaseChar() in guessed || char.uppercaseChar() in guessed
            }
            if (isMatch || char == ' ') char else '_'
        }.joinToString("")

        _petSpaState.update { it.copy(maskedWord = masked) }
    }

    private fun checkSpellingStatus() {
        val state = _petSpaState.value
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
            // Spelling correct! Transition to GROOMING phase
            _petSpaState.update { it.copy(phase = PetSpaPhase.GROOMING) }
        } else if (state.wrongGuesses >= state.maxWrong) {
            // Too many wrong guesses -> Game Over
            submitAnswer(false)
        }
    }

    fun useGroomingTool(tool: GroomingTool) {
        val state = _petSpaState.value
        if (state.phase != PetSpaPhase.GROOMING) return

        val newCompleted = state.completedGroomingTools + tool
        _petSpaState.update {
            it.copy(
                completedGroomingTools = newCompleted,
                activeGroomingTool = tool
            )
        }

        if (newCompleted.size >= GroomingTool.entries.size) {
            // All grooming tools finished! Round complete!
            _petSpaState.update { it.copy(phase = PetSpaPhase.ROUND_COMPLETE) }
            submitAnswer(true)
        }
    }
}
