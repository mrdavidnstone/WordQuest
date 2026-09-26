package com.spellinggames.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.local.SpellingListEntity
import com.spellinggames.app.data.repository.SpellingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordStat(
    val word: String,
    val mastery: String,
    val correct: Int,
    val total: Int,
    val accuracy: Int
)

data class ProgressUiState(
    val listName: String? = null,
    val mastered: Int = 0,
    val learning: Int = 0,
    val needsPractice: Int = 0,
    val wordStats: List<WordStat> = emptyList(),
    val allLists: List<SpellingListEntity> = emptyList(),
    val selectedListId: Long? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: SpellingRepository
) : ViewModel() {

    private val _selectedListId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Initialize with active list first
            repository.getActiveList().firstOrNull()?.let { active ->
                _selectedListId.value = active.id
            }

            combine(
                _selectedListId.flatMapLatest { id ->
                    if (id == null) flowOf(null)
                    else repository.getAllLists().map { lists -> lists.find { it.id == id } }
                },
                repository.getAllLists(),
                _selectedListId.flatMapLatest { id ->
                    if (id == null) flowOf(emptyList())
                    else repository.getWordsForList(id)
                },
                _selectedListId.flatMapLatest { id ->
                    if (id == null) flowOf(emptyList())
                    else repository.getProgressForList(id)
                }
            ) { list, allLists, words, progressList ->
                if (list == null) {
                    ProgressUiState(allLists = allLists)
                } else {
                    val progressMap = progressList.associateBy { it.wordId }
                    val stats = words.map { w ->
                        val p = progressMap[w.id]
                        val correct = p?.correctAttempts ?: 0
                        val total = p?.totalAttempts ?: 0
                        val accuracy = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
                        WordStat(
                            word = w.text,
                            mastery = p?.masteryLevel ?: "New",
                            correct = correct,
                            total = total,
                            accuracy = accuracy
                        )
                    }.sortedBy { it.accuracy }

                    var mastered = 0
                    var learning = 0
                    var needs = 0
                    stats.forEach {
                        when (it.mastery) {
                            "Mastered" -> mastered++
                            "Practicing", "Learning" -> learning++
                            else -> needs++
                        }
                    }

                    ProgressUiState(
                        listName = list.name,
                        mastered = mastered,
                        learning = learning,
                        needsPractice = needs,
                        wordStats = stats,
                        allLists = allLists,
                        selectedListId = list.id
                    )
                }
            }.collect { 
                _uiState.value = it
            }
        }
    }

    fun selectList(id: Long) {
        _selectedListId.value = id
    }
}
