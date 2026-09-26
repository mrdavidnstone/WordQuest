package com.spellinggames.app.ui.screens.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.local.SpellingListEntity
import com.spellinggames.app.data.local.WordEntity
import com.spellinggames.app.data.repository.SessionRepository
import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val repository: SpellingRepository,
    private val settings: SettingsRepository,
    private val session: SessionRepository
) : ViewModel() {

    val isUnlocked: StateFlow<Boolean> = session.isParentUnlocked

    val hasPin: StateFlow<Boolean> = settings.parentPin
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var storedPin: String? = null

    val lists: StateFlow<List<SpellingListEntity>> = repository.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedListId = MutableStateFlow<Long?>(null)
    val selectedListId: StateFlow<Long?> = _selectedListId.asStateFlow()

    val words: StateFlow<List<WordEntity>> = _selectedListId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getWordsForList(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    enum class ParentPage { Overview, LessonDetail }
    private val _currentPage = MutableStateFlow(ParentPage.Overview)
    val currentPage: StateFlow<ParentPage> = _currentPage.asStateFlow()

    fun navigateToOverview() {
        _currentPage.value = ParentPage.Overview
    }

    fun navigateToLessonDetail(id: Long) {
        _selectedListId.value = id
        _currentPage.value = ParentPage.LessonDetail
    }

    fun createAndNavigateToNewLesson(name: String) {
        viewModelScope.launch {
            val id = repository.createList(name)
            navigateToLessonDetail(id)
        }
    }

    val stats = combine(
        repository.getAllWords(),
        repository.getProgressForAll(),
        repository.getAllLists()
    ) { allWords, allProgress, allLists ->
        val masteredIds = allProgress.filter { it.masteryLevel == "Mastered" }.map { it.wordId }.toSet()
        
        val totalSpelling = allWords.count { it.definition.isNullOrBlank() }
        val masteredSpelling = allWords.count { it.definition.isNullOrBlank() && it.id in masteredIds }
        
        val totalVocab = allWords.count { !it.definition.isNullOrBlank() }
        val masteredVocab = allWords.count { !it.definition.isNullOrBlank() && it.id in masteredIds }
        
        val totalLessons = allLists.size
        val totalWords = allWords.size
        val progressPercent = if (totalWords == 0) 0 
        else (masteredIds.size * 100 / totalWords)
        
        ParentStats(
            wordsLearned = masteredSpelling,
            totalWords = totalSpelling,
            vocabLearned = masteredVocab,
            totalVocab = totalVocab,
            totalLessons = totalLessons,
            progressPercent = progressPercent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ParentStats())

    val activeLessonId: StateFlow<Long?> = repository.getActiveList()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun activateLesson(id: Long) {
        viewModelScope.launch {
            repository.setActiveList(id)
        }
    }

    fun updateLessonName(id: Long, name: String) {
        viewModelScope.launch {
            val list = repository.getListById(id)
            if (list != null) {
                repository.updateList(list.copy(name = name))
            }
        }
    }

    fun deleteLesson(id: Long) {
        viewModelScope.launch {
            val allLessons = lists.value
            val lessonToDelete = allLessons.find { it.id == id }
            val wasActive = lessonToDelete?.isActive == true
            
            repository.deleteList(id)
            
            if (wasActive) {
                // Find next available lesson to activate
                val remaining = allLessons.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    repository.setActiveList(remaining.first().id)
                }
            }

            if (_selectedListId.value == id) {
                _selectedListId.value = null
                _currentPage.value = ParentPage.Overview
            }
        }
    }

    private val _isReorderMode = MutableStateFlow(false)
    val isReorderMode: StateFlow<Boolean> = _isReorderMode.asStateFlow()

    fun toggleReorderMode() {
        _isReorderMode.value = !_isReorderMode.value
    }

    fun moveLesson(id: Long, direction: Int) { // -1 up, 1 down
        viewModelScope.launch {
            val currentLists = lists.value.toMutableList()
            val index = currentLists.indexOfFirst { it.id == id }
            if (index == -1) return@launch
            
            val newIndex = index + direction
            if (newIndex !in currentLists.indices) return@launch
            
            // Swap
            val item = currentLists.removeAt(index)
            currentLists.add(newIndex, item)
            
            // Update orders
            val updated = currentLists.mapIndexed { i, lesson ->
                lesson.copy(displayOrder = i)
            }
            repository.updateLists(updated)
        }
    }

    data class ParentStats(
        val wordsLearned: Int = 0,
        val totalWords: Int = 0,
        val vocabLearned: Int = 0,
        val totalVocab: Int = 0,
        val totalLessons: Int = 0,
        val progressPercent: Int = 0
    )

    init {
        viewModelScope.launch {
            settings.parentPin.collect { storedPin = it }
        }
    }

    fun unlock(pin: String) {
        if (!storedPin.isNullOrBlank() && storedPin == pin) {
            session.setParentUnlocked(true)
        }
    }

    fun lock() {
        session.setParentUnlocked(false)
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            settings.setParentPin(pin)
        }
    }

    fun selectList(id: Long) {
        _selectedListId.value = id
    }

    fun addWords(text: String) {
        val listId = _selectedListId.value ?: return
        viewModelScope.launch {
            val words = text.split(Regex("[,\\n]+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            repository.addWords(listId, words)
        }
    }

    fun addVocabulary(word: String, definition: String) {
        val listId = _selectedListId.value ?: return
        viewModelScope.launch {
            repository.addVocabulary(listId, word, definition)
        }
    }

    fun updateWord(id: Long, text: String, definition: String?) {
        viewModelScope.launch {
            val word = words.value.find { it.id == id }
            if (word != null) {
                repository.updateWord(word.copy(text = text.trim().lowercase(), definition = definition?.trim()?.takeIf { it.isNotBlank() }))
            }
        }
    }

    fun deleteWord(id: Long) {
        viewModelScope.launch {
            repository.deleteWord(id)
        }
    }

    fun resetProgress() {
        val listId = _selectedListId.value ?: return
        viewModelScope.launch {
            repository.resetListProgress(listId)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    val overallDifficulty: StateFlow<Int> = settings.overallDifficulty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    fun setOverallDifficulty(difficulty: Int) {
        viewModelScope.launch {
            settings.setOverallDifficulty(difficulty)
        }
    }
}
