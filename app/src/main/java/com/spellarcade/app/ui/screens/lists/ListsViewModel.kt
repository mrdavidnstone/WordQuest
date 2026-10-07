package com.spellarcade.app.ui.screens.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.local.SpellingListEntity
import com.spellarcade.app.data.local.WordEntity
import com.spellarcade.app.data.repository.SessionRepository
import com.spellarcade.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: SpellingRepository,
    private val session: SessionRepository
) : ViewModel() {

    val isParentUnlocked: StateFlow<Boolean> = session.isParentUnlocked

    val lists: StateFlow<List<SpellingListEntity>> = repository.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _wordCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val wordCounts: StateFlow<Map<Long, Int>> = _wordCounts.asStateFlow()

    val activeListId: StateFlow<Long?> = repository.getActiveList()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _viewingListId = MutableStateFlow<Long?>(null)
    val viewingListId: StateFlow<Long?> = _viewingListId.asStateFlow()

    val viewingWords: StateFlow<List<WordEntity>> = _viewingListId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getWordsForList(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setViewingList(id: Long?) {
        _viewingListId.value = id
    }

    init {
        viewModelScope.launch {
            lists.collect { listItems ->
                val counts = mutableMapOf<Long, Int>()
                listItems.forEach { list ->
                    counts[list.id] = repository.getWordCount(list.id)
                }
                _wordCounts.value = counts
            }
        }
    }

    fun createList(name: String, wordsText: String) {
        viewModelScope.launch {
            val id = repository.createList(name)
            if (wordsText.isNotBlank()) {
                val words = wordsText
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                repository.addWords(id, words)
            }
        }
    }

    fun renameList(list: SpellingListEntity) {
        viewModelScope.launch {
            repository.updateList(list)
        }
    }

    fun updateList(id: Long, name: String, wordsText: String) {
        viewModelScope.launch {
            val existing = repository.getListById(id)
            if (existing != null) {
                repository.updateList(existing.copy(name = name))
            }
            if (wordsText.isNotBlank()) {
                val words = wordsText
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                repository.updateListWords(id, words)
            }
        }
    }

    suspend fun getWordsText(listId: Long): String {
        return repository.getWordsForListOnce(listId).joinToString("\n") { 
            if (it.definition.isNullOrBlank()) it.text else "${it.text} - ${it.definition}"
        }
    }

    fun setActive(id: Long) {
        viewModelScope.launch {
            repository.setActiveList(id)
        }
    }

    fun duplicate(id: Long) {
        viewModelScope.launch {
            repository.duplicateList(id)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteList(id)
        }
    }
}
