package com.example.ecopoints.app.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.EcoLevel
import com.example.ecopoints.app.data.model.PetSpecies
import com.example.ecopoints.app.data.repository.PetRepository
import com.example.ecopoints.app.data.repository.ProgressRepository
import com.example.ecopoints.app.data.repository.SettingsRepository
import com.example.ecopoints.app.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WelcomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val petSpecies: String = PetSpecies.DEFAULT,
    val petDisplayName: String = "",
    val balance: Int = 0,
    val level: EcoLevel = EcoLevel.forPoints(0)
)

/** Bienvenida: saludo, mascota elegida, bono inicial e insignia del nivel actual. */
@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModel(
    userRepository: UserRepository,
    progressRepository: ProgressRepository,
    petRepository: PetRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<WelcomeUiState> = settingsRepository.sessionUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                userRepository.observeUser(userId),
                progressRepository.observeProgress(userId),
                petRepository.observePet(userId)
            ) { user, progress, pet ->
                if (user == null || progress == null || pet == null) {
                    WelcomeUiState()
                } else {
                    WelcomeUiState(
                        isLoading = false,
                        userName = user.name,
                        petSpecies = pet.species,
                        petDisplayName = pet.displayName,
                        balance = progress.balance,
                        level = EcoLevel.forPoints(progress.historicalPoints)
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WelcomeUiState())

    /** Cierra la sesión y avisa cuando terminó para que la pantalla vuelva al login. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.endSession()
            onDone()
        }
    }
}
