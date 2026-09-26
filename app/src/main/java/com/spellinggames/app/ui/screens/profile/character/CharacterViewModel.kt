package com.spellinggames.app.ui.screens.profile.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellinggames.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    private val _characterState = MutableStateFlow(CharacterState.POC_DEFAULT)
    val characterState: StateFlow<CharacterState> = _characterState.asStateFlow()

    private val _debugMode = MutableStateFlow(false)
    val debugMode: StateFlow<Boolean> = _debugMode.asStateFlow()

    private val _layerVisibility = MutableStateFlow(
        CharacterLayerType.entries.associateWith { true }
    )
    val layerVisibility: StateFlow<Map<CharacterLayerType, Boolean>> = _layerVisibility.asStateFlow()

    init {
        viewModelScope.launch {
            // Load saved configuration from DataStore on screen launch
            settings.avatar.firstOrNull()?.let { avatar ->
                _characterState.value = CharacterState.fromAvatar(avatar)
            }
        }
    }

    fun updateCharacter(update: (CharacterState) -> CharacterState) {
        _characterState.update { current ->
            val updated = update(current)
            updated
        }
        save()
    }

    fun toggleDebugMode() {
        _debugMode.update { !it }
    }

    fun toggleLayer(type: CharacterLayerType) {
        _layerVisibility.update { current ->
            current.toMutableMap().apply {
                this[type] = !(this[type] ?: true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val updatedAvatar = _characterState.value.toHeroConfiguration().toAvatar()
            settings.updateFullAvatar(updatedAvatar)
        }
    }
}
