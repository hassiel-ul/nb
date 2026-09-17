package com.hassiel.rideadvisor.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hassiel.rideadvisor.data.UserPreferences
import com.hassiel.rideadvisor.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_DATASTORE_NAME)

/**
 * Persiste y expone las preferencias del usuario (sección 10 del documento
 * de requisitos) usando Jetpack DataStore, tal como se solicita.
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val MIN_PRICE = doublePreferencesKey("minimum_price")
        val MAX_DISTANCE = doublePreferencesKey("maximum_distance")
        val ALLOWED_APPS = stringSetPreferencesKey("allowed_apps")
        val RADAR_ENABLED = booleanPreferencesKey("radar_enabled")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_MODE_ENABLED = booleanPreferencesKey("auto_mode_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val defaults = UserPreferences()
        UserPreferences(
            minimumPrice = prefs[Keys.MIN_PRICE] ?: defaults.minimumPrice,
            maximumDistance = prefs[Keys.MAX_DISTANCE] ?: defaults.maximumDistance,
            allowedApps = prefs[Keys.ALLOWED_APPS] ?: defaults.allowedApps,
            radarEnabled = prefs[Keys.RADAR_ENABLED] ?: defaults.radarEnabled,
            overlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: defaults.overlayEnabled,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: defaults.vibrationEnabled,
            autoModeEnabled = prefs[Keys.AUTO_MODE_ENABLED] ?: defaults.autoModeEnabled,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted
        )
    }

    suspend fun updateMinimumPrice(value: Double) {
        context.dataStore.edit { it[Keys.MIN_PRICE] = value }
    }

    suspend fun updateMaximumDistance(value: Double) {
        context.dataStore.edit { it[Keys.MAX_DISTANCE] = value }
    }

    suspend fun updateAllowedApps(apps: Set<String>) {
        context.dataStore.edit { it[Keys.ALLOWED_APPS] = apps }
    }

    suspend fun updateRadarEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RADAR_ENABLED] = enabled }
    }

    suspend fun updateOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun updateAutoModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_MODE_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }
}
