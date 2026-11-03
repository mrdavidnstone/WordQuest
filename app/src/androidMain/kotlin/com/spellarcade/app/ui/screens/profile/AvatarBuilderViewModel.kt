package com.spellarcade.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellarcade.app.data.model.Gender
import com.spellarcade.app.data.model.HeroConfiguration
import com.spellarcade.app.data.model.HeroConfigurationValidator
import com.spellarcade.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AvatarBuilderViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    private val _heroConfig = MutableStateFlow(HeroConfiguration.BOY_PROTOTYPE)
    val heroConfig: StateFlow<HeroConfiguration> = _heroConfig.asStateFlow()

    init {
        viewModelScope.launch {
            // Load saved configuration once on screen launch
            settings.avatar.firstOrNull()?.let { avatar ->
                val loaded = HeroConfiguration.fromAvatar(avatar)
                _heroConfig.value = HeroConfigurationValidator.validate(loaded)
            }
        }
    }

    fun updateConfig(update: (HeroConfiguration) -> HeroConfiguration) {
        _heroConfig.update { current ->
            val updated = update(current)
            HeroConfigurationValidator.validate(updated)
        }
        saveHero()
    }

    fun randomize() {
        val gender = _heroConfig.value.gender
        val randomConfig = HeroConfigurationValidator.getRandomConfiguration(gender)
        _heroConfig.value = HeroConfigurationValidator.validate(randomConfig)
        saveHero()
    }

    fun reset() {
        val defaultPrototype = if (_heroConfig.value.gender == Gender.GIRL) {
            HeroConfiguration.GIRL_PROTOTYPE
        } else {
            HeroConfiguration.BOY_PROTOTYPE
        }
        _heroConfig.value = defaultPrototype
        saveHero()
    }

    fun saveHero() {
        viewModelScope.launch {
            val validated = HeroConfigurationValidator.validate(_heroConfig.value)
            settings.updateFullAvatar(validated.toAvatar())
        }
    }
}
