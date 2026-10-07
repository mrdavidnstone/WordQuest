package com.spellarcade.app.ui.screens.games

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2

enum class TargetState { IDLE, AIMED, HIT_CORRECT, HIT_INCORRECT }

data class GalleryTarget(
    val id: String,
    val letter: Char,
    val positionRatio: Offset, // Normalized (x, y) relative position in landscape canvas
    val isCorrect: Boolean,
    val state: TargetState = TargetState.IDLE
)

data class CannonShot(
    val startRatio: Offset,
    val targetRatio: Offset,
    val target: GalleryTarget,
    val progress: Float // 0f to 1f
)

data class WordGalleryUiState(
    val maskedWord: List<Char> = emptyList(),
    val missingIndices: List<Int> = emptyList(),
    val currentPointer: Int = 0,
    val targets: List<GalleryTarget> = emptyList(),
    val activeShot: CannonShot? = null,
    val cannonAngle: Float = 0f,
    val missesCount: Int = 0,
    val isFiring: Boolean = false,
    val isResolving: Boolean = false,
    val pirateSpeech: String? = null
)

@HiltViewModel
class WordGalleryViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "word_gallery") {

    private val _galleryState = MutableStateFlow(WordGalleryUiState())
    val galleryState: StateFlow<WordGalleryUiState> = _galleryState.asStateFlow()

    fun buildPuzzleForCurrentWord() {
        val word = uiState.value.currentWord?.text ?: return
        if (word.length < 2) return

        val level = uiState.value.overallDifficulty
        val numToHide = when (level) {
            1 -> 1
            2 -> (word.length * 0.5).toInt().coerceAtLeast(1)
            3 -> (word.length * 0.9).toInt().coerceAtLeast(1)
            else -> 1
        }.coerceAtMost(word.length - 1)

        val missingIndices = (word.indices).shuffled().take(numToHide).sorted()
        val maskedWord = word.mapIndexed { i, c -> if (i in missingIndices) '_' else c }

        val firstMissingIdx = missingIndices[0]
        val targetLetters = generateChoices(word[firstMissingIdx])

        val targets = createTargetsForChoices(targetLetters, word[firstMissingIdx])

        _galleryState.value = WordGalleryUiState(
            maskedWord = maskedWord,
            missingIndices = missingIndices,
            currentPointer = 0,
            targets = targets,
            missesCount = 0,
            pirateSpeech = "Arrr! Load the cannon and aim for the missing letter!"
        )
    }

    private fun generateChoices(correctChar: Char): List<Char> {
        val word = uiState.value.currentWord?.text ?: ""
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val targetChar = if (requireCaps) correctChar else correctChar.lowercaseChar()
        val alphabet = if (requireCaps && correctChar.isUpperCase()) ('A'..'Z').toList() else ('a'..'z').toList()
        val distractors = alphabet.filter { it != targetChar }.shuffled().take(3)
        return (distractors + targetChar).shuffled()
    }

    private fun createTargetsForChoices(choices: List<Char>, correctChar: Char): List<GalleryTarget> {
        val word = uiState.value.currentWord?.text ?: ""
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }

        // Preset 4 pirate barrel positions in 16:9 landscape cove (right side)
        val positions = listOf(
            Offset(0.52f, 0.28f),
            Offset(0.72f, 0.22f),
            Offset(0.58f, 0.52f),
            Offset(0.78f, 0.48f)
        ).shuffled()

        return choices.mapIndexed { idx, ch ->
            val isMatch = if (requireCaps) ch == correctChar else ch.equals(correctChar, ignoreCase = true)
            GalleryTarget(
                id = "target_$idx",
                letter = ch,
                positionRatio = positions[idx],
                isCorrect = isMatch
            )
        }
    }

    fun fireAtTarget(target: GalleryTarget) {
        val state = _galleryState.value
        if (state.isFiring || state.isResolving || uiState.value.showFeedback || uiState.value.isFinished) return

        val cannonPos = Offset(0.18f, 0.72f) // Lower-left cannon position
        val dx = target.positionRatio.x - cannonPos.x
        val dy = target.positionRatio.y - cannonPos.y
        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        _galleryState.update {
            it.copy(
                isFiring = true,
                cannonAngle = angleDeg,
                activeShot = CannonShot(startRatio = cannonPos, targetRatio = target.positionRatio, target = target, progress = 0f)
            )
        }

        // Animate cannonball flight over 350ms
        viewModelScope.launch {
            val durationMs = 350L
            val steps = 15
            val stepDelay = durationMs / steps

            for (i in 1..steps) {
                delay(stepDelay)
                val p = i.toFloat() / steps
                _galleryState.update { cur ->
                    val shot = cur.activeShot ?: return@update cur
                    cur.copy(activeShot = shot.copy(progress = p))
                }
            }

            // Impact! Resolve hit
            resolveImpact(target)
        }
    }

    private fun resolveImpact(target: GalleryTarget) {
        val word = uiState.value.currentWord?.text ?: return
        val state = _galleryState.value
        val targetIdx = state.missingIndices.getOrNull(state.currentPointer) ?: return
        val correctLetter = word[targetIdx]

        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val isMatch = if (requireCaps) target.letter == correctLetter else target.letter.equals(correctLetter, ignoreCase = true)

        if (isMatch) {
            // Correct barrel hit!
            val newMasked = state.maskedWord.toMutableList()
            newMasked[targetIdx] = correctLetter
            val nextPointer = state.currentPointer + 1

            _galleryState.update { cur ->
                val updatedTargets = cur.targets.map { t ->
                    if (t.id == target.id) t.copy(state = TargetState.HIT_CORRECT) else t
                }
                cur.copy(
                    maskedWord = newMasked,
                    targets = updatedTargets,
                    isFiring = false,
                    isResolving = true,
                    activeShot = null,
                    pirateSpeech = "BLAST ME TIMBERS! Perfect Shot! 💥"
                )
            }

            viewModelScope.launch {
                delay(800)
                if (nextPointer >= state.missingIndices.size) {
                    // Entire word completed!
                    submitAnswer(true)
                } else {
                    // Next missing letter in multi-blank word
                    val nextMissingIdx = state.missingIndices[nextPointer]
                    val nextChoices = generateChoices(word[nextMissingIdx])
                    val newTargets = createTargetsForChoices(nextChoices, word[nextMissingIdx])

                    _galleryState.update { cur ->
                        cur.copy(
                            currentPointer = nextPointer,
                            targets = newTargets,
                            isResolving = false,
                            pirateSpeech = "Aim for the next missing letter, matey!"
                        )
                    }
                }
            }
        } else {
            // Incorrect barrel hit - Forgiving miss!
            _galleryState.update { cur ->
                val updatedTargets = cur.targets.map { t ->
                    if (t.id == target.id) t.copy(state = TargetState.HIT_INCORRECT) else t
                }
                cur.copy(
                    targets = updatedTargets,
                    missesCount = cur.missesCount + 1,
                    isFiring = false,
                    isResolving = true,
                    activeShot = null,
                    pirateSpeech = "ARRR! Missed that one! Try another barrel!"
                )
            }

            viewModelScope.launch {
                delay(600)
                _galleryState.update { cur ->
                    val resetTargets = cur.targets.map { t ->
                        if (t.id == target.id) t.copy(state = TargetState.IDLE) else t
                    }
                    cur.copy(
                        targets = resetTargets,
                        isResolving = false
                    )
                }
            }
        }
    }
}
