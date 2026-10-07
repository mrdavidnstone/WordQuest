package com.spellarcade.app.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.local.SpellingListEntity
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesMenuViewModel @Inject constructor(
    private val repository: SpellingRepository
) : ViewModel() {

    val activeListName: StateFlow<String?> = repository.getActiveList()
        .map { it?.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLists: StateFlow<List<SpellingListEntity>> = repository.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasWords = MutableStateFlow(false)
    val hasWords: StateFlow<Boolean> = _hasWords.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveList().collect { list ->
                if (list == null) {
                    _hasWords.value = false
                } else {
                    val count = repository.getWordCount(list.id)
                    _hasWords.value = count > 0
                }
            }
        }
    }

    fun switchList(id: Long) {
        viewModelScope.launch {
            repository.setActiveList(id)
        }
    }
}
