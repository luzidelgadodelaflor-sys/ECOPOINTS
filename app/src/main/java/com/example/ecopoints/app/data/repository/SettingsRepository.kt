package com.example.ecopoints.app.data.repository

import com.example.ecopoints.app.data.local.datastore.EcoPreferencesStore
import com.example.ecopoints.app.data.model.AppSettings
import com.example.ecopoints.app.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Ajustes de la app y sesión activa (DataStore). Los ViewModels solo conocen esta clase. */
class SettingsRepository(private val store: EcoPreferencesStore) {

    val settings: Flow<AppSettings> = store.settings

    /** Id del usuario con la sesión iniciada, o null si no hay sesión. */
    val sessionUserId: Flow<Long?> = store.sessionUserId

    suspend fun startSession(userId: Long) = store.setSession(userId)

    suspend fun endSession() = store.clearSession()

    suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    suspend fun setLargeText(enabled: Boolean) = store.setLargeText(enabled)

    suspend fun setChallengeReminders(enabled: Boolean) = store.setChallengeReminders(enabled)

    suspend fun setPetReminders(enabled: Boolean) = store.setPetReminders(enabled)

    suspend fun setRankingVisible(visible: Boolean) = store.setRankingVisible(visible)

    suspend fun setConfirmSpend(enabled: Boolean) = store.setConfirmSpend(enabled)

    suspend fun setVibration(enabled: Boolean) = store.setVibration(enabled)

    suspend fun setMapRadiusKm(radiusKm: Int) = store.setMapRadiusKm(radiusKm)
}
