package com.spellarcade.app.ui.screens.games

import com.spellarcade.app.data.repository.SettingsRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScrambledWordViewModel @Inject constructor(
    repository: SpellingRepository,
    settings: SettingsRepository
) : BaseGameViewModel(repository, settings, "scrambled") {

    private val _tiles = MutableStateFlow<List<Char>>(emptyList())
    val tiles: StateFlow<List<Char>> = _tiles.asStateFlow()

    private val _selected = MutableStateFlow<List<Char>>(emptyList())
    val selected: StateFlow<List<Char>> = _selected.asStateFlow()

    private val usedIndices = mutableSetOf<Int>()

    fun prepareTiles() {
        val word = uiState.value.currentWord?.text ?: return
        val requireCaps = uiState.value.requireCapitalLetters || word.any { it.isUpperCase() }
        val charList = if (requireCaps) word.toList() else word.lowercase().toList()
        _tiles.value = charList.shuffled()
        _selected.value = emptyList()
        usedIndices.clear()
    }

    fun tapTile(index: Int) {
        if (index in usedIndices) return
        usedIndices.add(index)
        _selected.value = _selected.value + _tiles.value[index]
    }

    fun isTileUsed(index: Int) = index in usedIndices

    fun clearSelection() {
        _selected.value = emptyList()
        usedIndices.clear()
    }

    fun checkAnswer() {
        val attempt = _selected.value.joinToString("")
        submitAnswer(isSpellingCorrect(attempt))
    }
}
