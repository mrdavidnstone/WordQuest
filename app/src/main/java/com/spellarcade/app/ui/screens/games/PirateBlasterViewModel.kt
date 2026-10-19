package com.spellarcade.app.ui.screens.games

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.hypot

enum class PirateTargetState { IDLE, AIMED, HIT_CORRECT, HIT_INCORRECT, DESTROYED }

data class SpawnItem(
    val letter: Char,
    val isCorrect: Boolean,
    val requiredIndex: Int?,
    val retriesLeft: Int
)

data class PirateTarget(
    val id: String,
    val letter: Char,
    val x: Float,
    val y: Float,
    val scale: Float,
    val speed: Float,
    val isCorrect: Boolean,
    val requiredIndex: Int?,
    val retriesLeft: Int,
    val state: PirateTargetState = PirateTargetState.IDLE
)

data class PirateCannonShot(
    val startRatio: Offset,
    val targetRatio: Offset,
    val target: PirateTarget?,
    val progress: Float
)

data class PirateBlasterUiState(
    val maskedWord: List<Char> = emptyList(),
    val missingIndices: List<Int> = emptyList(),
    val currentPointer: Int = 0,
    val targets: List<PirateTarget> = emptyList(),
    val spawnQueue: List<SpawnItem> = emptyList(),
    val activeShot: PirateCannonShot? = null,
    val cannonAngle: Float = -90f,
    val missesCount: Int = 0,
    val coins: Int = 0,
    val cannonHorizontalOffset: Float = 0f,
    val cannonBallVerticalOffset: Float = -0.06f,
    val isFiring: Boolean = false,
    val isResolving: Boolean = false,
    val pirateSpeech: String? = null,
    val healthUnits: Int = 10,
    val maxHealthUnits: Int = 10,
    val damageCount: Int = 0,
    val isGameOver: Boolean = false
)

@HiltViewModel
class PirateBlasterViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "pirate_blaster") {

    private val _blasterState = MutableStateFlow(PirateBlasterUiState())
    val blasterState: StateFlow<PirateBlasterUiState> = _blasterState.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val level = state.overallDifficulty
                val maxUnits = when (level) {
                    3 -> 6  // Hard: 3 hearts (6 half-heart units)
                    2 -> 8  // Medium: 4 hearts (8 half-heart units)
                    1 -> 5  // Easy: 5 hearts (10 half-heart units)
                    else -> 10
                }
                _blasterState.update { cur ->
                    if (cur.healthUnits <= 0 && !cur.isGameOver) {
                        cur.copy(healthUnits = maxUnits, maxHealthUnits = maxUnits)
                    } else {
                        cur.copy(maxHealthUnits = maxUnits)
                    }
                }

                if (state.isReady && !state.isFinished && gameLoopJob == null) {
                    startGameLoop()
                } else if (!state.isReady || state.isFinished) {
                    gameLoopJob?.cancel()
                    gameLoopJob = null
                }
            }
        }
    }

    override fun startGame() {
        super.startGame()
        val level = uiState.value.overallDifficulty
        val maxUnits = when (level) {
            3 -> 6  // Hard: 3 hearts (6 half-heart units)
            2 -> 8  // Medium: 4 hearts (8 half-heart units)
            1 -> 5  // Easy: 5 hearts (10 half-heart units)
            else -> 10
        }
        _blasterState.update {
            it.copy(
                healthUnits = maxUnits,
                maxHealthUnits = maxUnits,
                isGameOver = false,
                damageCount = 0,
                missesCount = 0
            )
        }
    }

    private fun inflictDamage() {
        val state = _blasterState.value
        if (state.isGameOver || uiState.value.isFinished) return

        Log.d("PirateBlaster", "DAMAGE_EVENT: confirmed miss received by the health system")
        Log.d("PirateBlaster", "HEALTH_BEFORE: ${state.healthUnits}")

        val newHealth = (state.healthUnits - 1).coerceAtLeast(0)
        val newDamageCount = state.damageCount + 1

        Log.d("PirateBlaster", "HEALTH_AFTER: $newHealth")

        if (newHealth <= 0) {
            Log.d("PirateBlaster", "GAME_OVER: triggered only when health reaches zero.")
            _blasterState.update {
                it.copy(
                    healthUnits = 0,
                    damageCount = newDamageCount,
                    isGameOver = true
                )
            }
            showSpeech("ARRR! Ship wrecked! Game Over!")
            viewModelScope.launch {
                delay(600)
                finishGame()
            }
        } else {
            _blasterState.update {
                it.copy(
                    healthUnits = newHealth,
                    damageCount = newDamageCount
                )
            }
        }
    }

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

        val retriesAllowed = when (level) {
            1 -> 3 // Easy: 3 retries
            2 -> 2 // Medium: 2 retries
            3 -> 1 // Hard: 1 retry
            else -> 3
        }

        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val requiredItems = missingIndices.mapIndexed { reqIdx, wordIdx ->
            val char = word[wordIdx]
            SpawnItem(
                letter = if (requireCaps) char else char.lowercaseChar(),
                isCorrect = true,
                requiredIndex = reqIdx,
                retriesLeft = retriesAllowed
            )
        }

        val correctChars = requiredItems.map { it.letter }.toSet()
        val alphabet = if (requireCaps) ('A'..'Z').toList() else ('a'..'z').toList()
        val distractors = alphabet.filter { it !in correctChars }.shuffled().take(6).map { ch ->
            SpawnItem(
                letter = ch,
                isCorrect = false,
                requiredIndex = null,
                retriesLeft = 0
            )
        }

        val spawnQueue = (requiredItems + distractors).shuffled()
        val maxUnits = when (level) {
            3 -> 6  // Hard: 3 hearts (6 half-heart units)
            2 -> 8  // Medium: 4 hearts (8 half-heart units)
            1 -> 10 // Easy: 5 hearts (10 half-heart units)
            else -> 10
        }

        _blasterState.update { cur ->
            cur.copy(
                maskedWord = maskedWord,
                missingIndices = missingIndices,
                currentPointer = 0,
                targets = emptyList(),
                spawnQueue = spawnQueue,
                missesCount = 0,
                isFiring = false,
                isResolving = false,
                pirateSpeech = null,
                healthUnits = if (cur.healthUnits <= 0 || cur.maxHealthUnits != maxUnits) maxUnits else cur.healthUnits,
                maxHealthUnits = maxUnits
            )
        }
        showSpeech("Arrr! New word! Barrels ahoy!")
    }

    private fun showSpeech(speech: String) {
        _blasterState.update { it.copy(pirateSpeech = speech) }
        viewModelScope.launch {
            delay(1000L) // Hide after 1 second
            _blasterState.update { cur ->
                if (cur.pirateSpeech == speech) {
                    cur.copy(pirateSpeech = null)
                } else cur
            }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                delay(16) // ~60fps
                val currentTime = System.currentTimeMillis()
                val delta = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                if (delta in 0.001f..0.1f) {
                    updateGameFrame(delta)
                }
            }
        }
    }

    fun setCannonSettings(horizontalOffset: Float, verticalOffset: Float) {
        _blasterState.update {
            it.copy(
                cannonHorizontalOffset = horizontalOffset,
                cannonBallVerticalOffset = verticalOffset
            )
        }
    }

    private fun updateGameFrame(delta: Float) {
        val state = _blasterState.value
        if (state.isResolving || uiState.value.showFeedback || uiState.value.isFinished) return

        val difficulty = uiState.value.overallDifficulty
        val speedRatioPerSec = when (difficulty) {
            1 -> 0.12f // Easy: slowest
            2 -> 0.18f // Medium: in between
            3 -> 0.28f // Hard: quickest
            else -> 0.18f
        }
        val moveDelta = speedRatioPerSec * delta

        val updatedTargets = mutableListOf<PirateTarget>()
        val returnedItems = mutableListOf<SpawnItem>()

        for (target in state.targets) {
            if (target.state == PirateTargetState.HIT_CORRECT || target.state == PirateTargetState.HIT_INCORRECT || target.state == PirateTargetState.DESTROYED) {
                updatedTargets.add(target)
                continue
            }

            val newX = target.x - (moveDelta * (target.scale * 0.85f))
            if (newX < -0.15f) {
                if (target.isCorrect && target.retriesLeft > 0) {
                    returnedItems.add(
                        SpawnItem(
                            letter = target.letter,
                            isCorrect = true,
                            requiredIndex = target.requiredIndex,
                            retriesLeft = target.retriesLeft - 1
                        )
                    )
                } else if (target.isCorrect && target.retriesLeft <= 0) {
                    inflictDamage()

                    val word = uiState.value.currentWord?.text ?: ""
                    val reqIdx = target.requiredIndex ?: state.currentPointer
                    val targetIdx = state.missingIndices.getOrNull(reqIdx)
                    if (targetIdx != null) {
                        val correctChar = word[targetIdx]
                        val newMasked = state.maskedWord.toMutableList()
                        newMasked[targetIdx] = correctChar
                        val nextPointer = reqIdx + 1

                        _blasterState.update { cur ->
                            cur.copy(
                                maskedWord = newMasked,
                                currentPointer = nextPointer.coerceAtMost(state.missingIndices.size),
                                missesCount = cur.missesCount + 1
                            )
                        }
                        showSpeech("Arrr! Missed letter! Taking damage!")

                        if (nextPointer >= state.missingIndices.size) {
                            viewModelScope.launch {
                                delay(500)
                                submitAnswer(true)
                            }
                        }
                    }
                }
            } else {
                updatedTargets.add(target.copy(x = newX))
            }
        }

        val activeBarrels = updatedTargets.filter { it.state == PirateTargetState.IDLE || it.state == PirateTargetState.AIMED }
        var currentSpawnQueue = state.spawnQueue + returnedItems
        if (currentSpawnQueue.isEmpty()) {
            currentSpawnQueue = ensureSpawnQueue(state)
        }
        val maxActiveBarrels = 3

        if (activeBarrels.size < maxActiveBarrels && currentSpawnQueue.isNotEmpty()) {
            val nextItem = currentSpawnQueue.first()
            currentSpawnQueue = currentSpawnQueue.drop(1)
            val newBarrel = spawnBarrel(nextItem, activeBarrels)
            updatedTargets.add(newBarrel)
        }

        _blasterState.update {
            it.copy(
                targets = updatedTargets.filter { t -> t.state != PirateTargetState.DESTROYED || t.x >= -0.15f },
                spawnQueue = currentSpawnQueue
            )
        }
    }

    private fun ensureSpawnQueue(state: PirateBlasterUiState): List<SpawnItem> {
        val word = uiState.value.currentWord?.text ?: return emptyList()
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }

        val remainingIndices = state.missingIndices.drop(state.currentPointer)
        val requiredItems = remainingIndices.map { wordIdx ->
            val char = word[wordIdx]
            SpawnItem(
                letter = if (requireCaps) char else char.lowercaseChar(),
                isCorrect = true,
                requiredIndex = state.missingIndices.indexOf(wordIdx),
                retriesLeft = when (uiState.value.overallDifficulty) {
                    1 -> 3
                    2 -> 2
                    3 -> 1
                    else -> 3
                }
            )
        }

        val correctChars = requiredItems.map { it.letter }.toSet()
        val alphabet = if (requireCaps) ('A'..'Z').toList() else ('a'..'z').toList()
        val distractors = alphabet.filter { it !in correctChars }.shuffled().take(6).map { ch ->
            SpawnItem(
                letter = ch,
                isCorrect = false,
                requiredIndex = null,
                retriesLeft = 0
            )
        }

        return (requiredItems + distractors).shuffled()
    }

    private fun spawnBarrel(item: SpawnItem, activeBarrels: List<PirateTarget>): PirateTarget {
        val occupiedTiers = activeBarrels.map { t ->
            when {
                t.y < 0.33f -> 0
                t.y < 0.48f -> 1
                else -> 2
            }
        }.toSet()

        val availableTiers = (0..2).filter { it !in occupiedTiers }.ifEmpty { listOf((0..2).random()) }
        val tier = availableTiers.random()

        val (scale, yPos) = when (tier) {
            0 -> 0.75f to (0.22f + (0..4).random() * 0.02f)
            1 -> 1.05f to (0.38f + (0..4).random() * 0.02f)
            else -> 1.35f to (0.52f + (0..4).random() * 0.02f)
        }

        val spawnX = 1.15f + (0..5).random() * 0.04f

        val id = "barrel_${System.currentTimeMillis()}_${(0..999).random()}"
        return PirateTarget(
            id = id,
            letter = item.letter,
            x = spawnX,
            y = yPos,
            scale = scale,
            speed = 0.20f,
            isCorrect = item.isCorrect,
            requiredIndex = item.requiredIndex,
            retriesLeft = item.retriesLeft,
            state = PirateTargetState.IDLE
        )
    }

    fun fireAtPoint(ratioX: Float, ratioY: Float) {
        val state = _blasterState.value
        if (state.isFiring || state.isResolving || uiState.value.showFeedback || uiState.value.isFinished) return

        val startX = 0.50f + state.cannonHorizontalOffset
        val startY = 0.82f + state.cannonBallVerticalOffset
        val cannonPos = Offset(startX, startY)
        val clickedTarget = state.targets.filter { it.state == PirateTargetState.IDLE }.minByOrNull { t ->
            hypot((t.x - ratioX).toDouble(), (t.y - ratioY).toDouble())
        }

        if (clickedTarget != null) {
            val dist = hypot((clickedTarget.x - ratioX).toDouble(), (clickedTarget.y - ratioY).toDouble())
            if (dist < 0.20) {
                fireAtTarget(clickedTarget)
                return
            }
        }

        val targetPos = Offset(ratioX, ratioY)
        val dx = targetPos.x - cannonPos.x
        val dy = targetPos.y - cannonPos.y
        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        _blasterState.update {
            it.copy(
                isFiring = true,
                cannonAngle = angleDeg,
                activeShot = PirateCannonShot(startRatio = cannonPos, targetRatio = targetPos, target = null, progress = 0f)
            )
        }

        viewModelScope.launch {
            val durationMs = 300L
            val steps = 12
            val stepDelay = durationMs / steps

            for (i in 1..steps) {
                delay(stepDelay)
                val p = i.toFloat() / steps
                _blasterState.update { cur ->
                    val shot = cur.activeShot ?: return@update cur
                    cur.copy(activeShot = shot.copy(progress = p))
                }
            }

            _blasterState.update {
                it.copy(
                    isFiring = false,
                    activeShot = null
                )
            }
        }
    }

    fun fireAtTarget(target: PirateTarget) {
        val state = _blasterState.value
        if (state.isFiring || state.isResolving || uiState.value.showFeedback || uiState.value.isFinished) return
        if (target.state != PirateTargetState.IDLE) return

        val startX = 0.50f + state.cannonHorizontalOffset
        val startY = 0.82f + state.cannonBallVerticalOffset
        val cannonPos = Offset(startX, startY)
        val dx = target.x - cannonPos.x
        val dy = target.y - cannonPos.y
        val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        _blasterState.update {
            it.copy(
                isFiring = true,
                cannonAngle = angleDeg,
                activeShot = PirateCannonShot(startRatio = cannonPos, targetRatio = Offset(target.x, target.y), target = target, progress = 0f)
            )
        }

        viewModelScope.launch {
            val durationMs = 300L
            val steps = 12
            val stepDelay = durationMs / steps

            for (i in 1..steps) {
                delay(stepDelay)
                val p = i.toFloat() / steps
                _blasterState.update { cur ->
                    val shot = cur.activeShot ?: return@update cur
                    cur.copy(activeShot = shot.copy(progress = p))
                }
            }

            resolveImpact(target)
        }
    }

    private fun resolveImpact(target: PirateTarget) {
        val word = uiState.value.currentWord?.text ?: return
        val state = _blasterState.value
        val targetIdx = state.missingIndices.getOrNull(state.currentPointer) ?: return
        val correctLetter = word[targetIdx]

        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val isMatch = if (requireCaps) target.letter == correctLetter else target.letter.equals(correctLetter, ignoreCase = true)

        if (isMatch) {
            val newMasked = state.maskedWord.toMutableList()
            newMasked[targetIdx] = correctLetter
            val nextPointer = state.currentPointer + 1
            val allRequiredHit = nextPointer >= state.missingIndices.size

            if (allRequiredHit) {
                // Word Completion Sequence: Trigger explosion animations for all active barrels currently on screen
                _blasterState.update { cur ->
                    val updatedTargets = cur.targets.map { t ->
                        t.copy(state = PirateTargetState.HIT_CORRECT)
                    }
                    cur.copy(
                        maskedWord = newMasked,
                        targets = updatedTargets,
                        coins = cur.coins + 50, // Bonus coins for completing a word
                        isFiring = false,
                        isResolving = true,
                        activeShot = null
                    )
                }
                showSpeech("ARRR! Word Complete! Magnificent shooting! 💥")

                viewModelScope.launch {
                    delay(750) // Wait for 15-frame explosion animation @ 20fps
                    submitAnswer(true)
                }
            } else {
                _blasterState.update { cur ->
                    val updatedTargets = cur.targets.map { t ->
                        if (t.id == target.id) t.copy(state = PirateTargetState.HIT_CORRECT) else t
                    }
                    cur.copy(
                        maskedWord = newMasked,
                        currentPointer = nextPointer,
                        targets = updatedTargets,
                        coins = cur.coins + 25,
                        isFiring = false,
                        isResolving = false,
                        activeShot = null
                    )
                }
                showSpeech("Direct hit! Target the next missing letter!")
                viewModelScope.launch {
                    delay(750)
                    _blasterState.update { cur ->
                        val cleaned = cur.targets.filter { it.id != target.id }
                        cur.copy(targets = cleaned)
                    }
                }
            }
        } else {
            inflictDamage()
            _blasterState.update { cur ->
                val updatedTargets = cur.targets.map { t ->
                    if (t.id == target.id) t.copy(state = PirateTargetState.HIT_INCORRECT) else t
                }
                cur.copy(
                    targets = updatedTargets,
                    missesCount = cur.missesCount + 1,
                    isFiring = false,
                    isResolving = false,
                    activeShot = null
                )
            }
            showSpeech("ARRR! Wrong barrel! Try again!")

            viewModelScope.launch {
                delay(750)
                _blasterState.update { cur ->
                    val cleaned = cur.targets.filter { it.id != target.id }
                    cur.copy(targets = cleaned)
                }
            }
        }
    }
}
