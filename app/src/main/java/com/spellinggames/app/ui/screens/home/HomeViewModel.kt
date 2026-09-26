package com.spellinggames.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.local.SpellingListEntity
import com.spellinggames.app.data.repository.SessionRepository
import com.spellinggames.app.data.repository.SettingsRepository
import com.spellinggames.app.data.repository.SpellingRepository
import com.spellinggames.app.ui.screens.profile.Avatar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeListName: String? = null,
    val activeListId: Long? = null,
    val allLists: List<SpellingListEntity> = emptyList(),
    val wordCount: Int = 0,
    val masteredCount: Int = 0,
    val learningCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val overallProgress: Float = 0f,
    val avatar: Avatar = Avatar(),
    val isParentUnlocked: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SpellingRepository,
    private val settings: SettingsRepository,
    private val session: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val listFlow = combine(repository.getActiveList(), repository.getAllLists()) { active, all -> active to all }

            combine(
                listFlow, 
                settings.avatar, 
                session.isParentUnlocked
            ) { listData, avatar, isUnlocked ->
                val (list, all) = listData
                if (list == null) {
                    _uiState.update { it.copy(allLists = all, avatar = avatar, isParentUnlocked = isUnlocked) }
                } else {
                    val words = repository.getWordsForList(list.id).first()
                    val progress = repository.getProgressForList(list.id).first()
                    val progressMap = progress.associateBy { it.wordId }

                    var mastered = 0
                    var learning = 0
                    var needs = 0
                    words.forEach { w ->
                        when (progressMap[w.id]?.masteryLevel) {
                            "Mastered" -> mastered++
                            "Practicing", "Learning" -> learning++
                            else -> needs++
                        }
                    }
                    val overall = if (words.isEmpty()) 0f
                    else mastered.toFloat() / words.size

                    _uiState.update {
                        it.copy(
                            activeListName = list.name,
                            activeListId = list.id,
                            allLists = all,
                            wordCount = words.size,
                            masteredCount = mastered,
                            learningCount = learning,
                            needsPracticeCount = needs,
                            overallProgress = overall,
                            avatar = avatar,
                            isParentUnlocked = isUnlocked
                        )
                    }
                }
            }.collect { }
        }
    }

    fun setActiveList(id: Long) {
        viewModelScope.launch {
            repository.setActiveList(id)
        }
    }
}
