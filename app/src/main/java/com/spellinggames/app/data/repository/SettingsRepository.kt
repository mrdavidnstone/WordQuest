package com.spellinggames.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spellinggames.app.ui.screens.profile.Avatar
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val parentPinKey = stringPreferencesKey("parent_pin")
    private val totalPointsKey = stringPreferencesKey("total_points")
    private val streakKey = stringPreferencesKey("current_streak")
    
    // Avatar Keys
    private val avatarGenderKey = stringPreferencesKey("avatar_gender")
    private val avatarSkinKey = stringPreferencesKey("avatar_skin")
    private val avatarFaceShapeKey = stringPreferencesKey("avatar_face_shape")
    private val avatarEyeStyleKey = stringPreferencesKey("avatar_eye_style")
    private val avatarEyeColorKey = stringPreferencesKey("avatar_eye_color")
    private val avatarEyebrowsKey = stringPreferencesKey("avatar_eyebrows")
    private val avatarNoseKey = stringPreferencesKey("avatar_nose")
    private val avatarMouthKey = stringPreferencesKey("avatar_mouth")
    private val avatarHairStyleKey = stringPreferencesKey("avatar_hair_style")
    private val avatarHairColorKey = stringPreferencesKey("avatar_hair_color")
    private val avatarOutfitKey = stringPreferencesKey("avatar_outfit")
    private val avatarOutfitColorKey = stringPreferencesKey("avatar_outfit_color")
    private val avatarBottomStyleKey = stringPreferencesKey("avatar_bottom_style")
    private val avatarBottomColorKey = stringPreferencesKey("avatar_bottom_color")
    private val avatarHatStyleKey = stringPreferencesKey("avatar_hat_style")
    private val avatarShoeStyleKey = stringPreferencesKey("avatar_shoe_style")
    private val avatarShoeColorKey = stringPreferencesKey("avatar_shoe_color")
    private val avatarAccessoryKey = stringPreferencesKey("avatar_accessory")

    private val overallDifficultyKey = stringPreferencesKey("overall_difficulty")

    val parentPin: Flow<String?> = context.dataStore.data.map { it[parentPinKey] }
    val totalPoints: Flow<Int> = context.dataStore.data.map {
        it[totalPointsKey]?.toIntOrNull() ?: 0
    }
    val currentStreak: Flow<Int> = context.dataStore.data.map {
        it[streakKey]?.toIntOrNull() ?: 0
    }

    val avatar: Flow<Avatar> = context.dataStore.data.map { prefs ->
        Avatar(
            gender = prefs[avatarGenderKey] ?: "Boy",
            skinColor = prefs[avatarSkinKey] ?: "body_01",
            faceShape = prefs[avatarFaceShapeKey] ?: "Round",
            eyeStyle = prefs[avatarEyeStyleKey] ?: "Round",
            eyeColor = prefs[avatarEyeColorKey] ?: "#6D4C41",
            eyebrowsStyle = prefs[avatarEyebrowsKey] ?: "Natural",
            noseStyle = prefs[avatarNoseKey] ?: "Small",
            mouthStyle = prefs[avatarMouthKey] ?: "Smile",
            hairStyle = prefs[avatarHairStyleKey] ?: "hat_hair_spiky_brown",
            hairColor = prefs[avatarHairColorKey] ?: "brown",
            outfit = prefs[avatarOutfitKey] ?: "hoodie",
            outfitColor = prefs[avatarOutfitColorKey] ?: "gray",
            bottomStyle = prefs[avatarBottomStyleKey] ?: "cargo_pants",
            bottomColor = prefs[avatarBottomColorKey] ?: "khaki",
            hatStyle = prefs[avatarHatStyleKey] ?: "cap",
            shoeStyle = prefs[avatarShoeStyleKey] ?: "shoes_blue",
            shoeColor = prefs[avatarShoeColorKey] ?: "#3E2723",
            accessory = prefs[avatarAccessoryKey] ?: "None"
        )
    }

    val overallDifficulty: Flow<Int> = context.dataStore.data.map {
        it[overallDifficultyKey]?.toIntOrNull() ?: 1
    }

    suspend fun setParentPin(pin: String) {
        context.dataStore.edit { it[parentPinKey] = pin }
    }

    suspend fun clearParentPin() {
        context.dataStore.edit { it.remove(parentPinKey) }
    }

    suspend fun addPoints(points: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[totalPointsKey]?.toIntOrNull() ?: 0
            prefs[totalPointsKey] = (current + points).toString()
        }
    }

    suspend fun setOverallDifficulty(difficulty: Int) {
        context.dataStore.edit { it[overallDifficultyKey] = difficulty.toString() }
    }

    suspend fun updateFullAvatar(a: Avatar) {
        context.dataStore.edit { prefs ->
            prefs[avatarGenderKey] = a.gender
            prefs[avatarSkinKey] = a.skinColor
            prefs[avatarFaceShapeKey] = a.faceShape
            prefs[avatarEyeStyleKey] = a.eyeStyle
            prefs[avatarEyeColorKey] = a.eyeColor
            prefs[avatarEyebrowsKey] = a.eyebrowsStyle
            prefs[avatarNoseKey] = a.noseStyle
            prefs[avatarMouthKey] = a.mouthStyle
            prefs[avatarHairStyleKey] = a.hairStyle
            prefs[avatarHairColorKey] = a.hairColor
            prefs[avatarOutfitKey] = a.outfit
            prefs[avatarOutfitColorKey] = a.outfitColor
            prefs[avatarBottomStyleKey] = a.bottomStyle
            prefs[avatarBottomColorKey] = a.bottomColor
            prefs[avatarHatStyleKey] = a.hatStyle
            prefs[avatarShoeStyleKey] = a.shoeStyle
            prefs[avatarShoeColorKey] = a.shoeColor
            prefs[avatarAccessoryKey] = a.accessory
        }
    }
}
