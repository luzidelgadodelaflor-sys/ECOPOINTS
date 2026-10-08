package com.example.ecopoints.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.model.AppSettings
import com.example.ecopoints.app.data.model.ThemeMode
import com.example.ecopoints.app.data.repository.SettingsRepository
import com.example.ecopoints.app.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "EcoAmigo",
    val settings: AppSettings = AppSettings()
)

/** Ajustes de EcoPoints: cada cambio se guarda al instante en DataStore y se aplica en toda la app. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val userName: Flow<String> = settingsRepository.sessionUserId.flatMapLatest { userId ->
        if (userId == null) flowOf("EcoAmigo") else userRepository.observeUser(userId).map { it?.name ?: "EcoAmigo" }
    }

    val uiState: StateFlow<SettingsUiState> = combine(settingsRepository.settings, userName) { settings, name ->
        SettingsUiState(userName = name, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) = save { settingsRepository.setThemeMode(mode) }

    fun setLargeText(enabled: Boolean) = save { settingsRepository.setLargeText(enabled) }

    fun setChallengeReminders(enabled: Boolean) = save { settingsRepository.setChallengeReminders(enabled) }

    fun setPetReminders(enabled: Boolean) = save { settingsRepository.setPetReminders(enabled) }

    fun setRankingVisible(visible: Boolean) = save { settingsRepository.setRankingVisible(visible) }

    fun setConfirmSpend(enabled: Boolean) = save { settingsRepository.setConfirmSpend(enabled) }

    fun setVibration(enabled: Boolean) = save { settingsRepository.setVibration(enabled) }

    fun setMapRadiusKm(radiusKm: Int) = save { settingsRepository.setMapRadiusKm(radiusKm) }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
