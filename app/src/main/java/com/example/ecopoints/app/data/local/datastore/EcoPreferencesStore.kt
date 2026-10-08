package com.example.ecopoints.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecopoints.app.data.model.AppSettings
import com.example.ecopoints.app.data.model.ThemeMode
import com.example.ecopoints.app.domain.EcoRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Única instancia de DataStore de la app (archivo ecopoints_settings.preferences_pb). */
private val Context.ecoDataStore: DataStore<Preferences> by preferencesDataStore(name = "ecopoints_settings")

/**
 * Fuente de datos de las preferencias con DataStore: la sesión activa (id del usuario)
 * y los ajustes de la pantalla de Configuración. Lo que no son preferencias vive en Room.
 */
class EcoPreferencesStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.ecoDataStore)

    private object Keys {
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
        val CHALLENGE_REMINDERS = booleanPreferencesKey("notifications_enabled")
        val PET_REMINDERS = booleanPreferencesKey("pet_reminders_enabled")
        val RANKING_VISIBLE = booleanPreferencesKey("ranking_visible")
        val CONFIRM_SPEND = booleanPreferencesKey("confirm_spend")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val MAP_RADIUS_KM = intPreferencesKey("eco_map_search_radius_km")
        val LEGACY_MIGRATED = booleanPreferencesKey("legacy_prefs_migrated")
    }

    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        // Si el archivo no se puede leer se usan los valores por defecto en lugar de cerrar la app.
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val settings: Flow<AppSettings> = preferences.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: defaults.themeMode,
            largeText = prefs[Keys.LARGE_TEXT] ?: defaults.largeText,
            challengeRemindersEnabled = prefs[Keys.CHALLENGE_REMINDERS] ?: defaults.challengeRemindersEnabled,
            petRemindersEnabled = prefs[Keys.PET_REMINDERS] ?: defaults.petRemindersEnabled,
            rankingVisible = prefs[Keys.RANKING_VISIBLE] ?: defaults.rankingVisible,
            confirmSpend = prefs[Keys.CONFIRM_SPEND] ?: defaults.confirmSpend,
            vibrationEnabled = prefs[Keys.VIBRATION] ?: defaults.vibrationEnabled,
            mapRadiusKm = (prefs[Keys.MAP_RADIUS_KM] ?: defaults.mapRadiusKm)
                .takeIf { it in EcoRules.MAP_RADIUS_OPTIONS_KM } ?: defaults.mapRadiusKm
        )
    }

    /** Id del usuario con la sesión iniciada, o null si no hay sesión. */
    val sessionUserId: Flow<Long?> = preferences.map { it[Keys.CURRENT_USER_ID] }

    val legacyMigrated: Flow<Boolean> = preferences.map { it[Keys.LEGACY_MIGRATED] ?: false }

    suspend fun setSession(userId: Long) {
        dataStore.edit { it[Keys.CURRENT_USER_ID] = userId }
    }

    suspend fun clearSession() {
        dataStore.edit { it.remove(Keys.CURRENT_USER_ID) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLargeText(enabled: Boolean) {
        dataStore.edit { it[Keys.LARGE_TEXT] = enabled }
    }

    suspend fun setChallengeReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.CHALLENGE_REMINDERS] = enabled }
    }

    suspend fun setPetReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.PET_REMINDERS] = enabled }
    }

    suspend fun setRankingVisible(visible: Boolean) {
        dataStore.edit { it[Keys.RANKING_VISIBLE] = visible }
    }

    suspend fun setConfirmSpend(enabled: Boolean) {
        dataStore.edit { it[Keys.CONFIRM_SPEND] = enabled }
    }

    suspend fun setVibration(enabled: Boolean) {
        dataStore.edit { it[Keys.VIBRATION] = enabled }
    }

    suspend fun setMapRadiusKm(radiusKm: Int) {
        dataStore.edit { it[Keys.MAP_RADIUS_KM] = radiusKm }
    }

    /** Copia los ajustes de la versión con SharedPreferences y marca la migración como hecha. */
    suspend fun importLegacy(settings: AppSettings, sessionUserId: Long?) {
        dataStore.edit {
            it[Keys.THEME_MODE] = settings.themeMode.name
            it[Keys.LARGE_TEXT] = settings.largeText
            it[Keys.CHALLENGE_REMINDERS] = settings.challengeRemindersEnabled
            it[Keys.PET_REMINDERS] = settings.petRemindersEnabled
            it[Keys.RANKING_VISIBLE] = settings.rankingVisible
            it[Keys.CONFIRM_SPEND] = settings.confirmSpend
            it[Keys.VIBRATION] = settings.vibrationEnabled
            it[Keys.MAP_RADIUS_KM] = settings.mapRadiusKm
            if (sessionUserId != null) it[Keys.CURRENT_USER_ID] = sessionUserId
        }
    }

    suspend fun markLegacyMigrated() {
        dataStore.edit { it[Keys.LEGACY_MIGRATED] = true }
    }
}
