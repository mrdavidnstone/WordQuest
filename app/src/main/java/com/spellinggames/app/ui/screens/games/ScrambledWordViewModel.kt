package com.spellinggames.app.ui.screens.games

import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
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
        _tiles.value = word.toList().shuffled()
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
        val word = uiState.value.currentWord?.text ?: return
        val attempt = _selected.value.joinToString("")
        submitAnswer(attempt.equals(word, ignoreCase = true))
    }
}
