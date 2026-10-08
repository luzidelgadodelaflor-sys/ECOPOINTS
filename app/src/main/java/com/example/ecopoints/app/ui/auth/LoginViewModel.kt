package com.example.ecopoints.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.model.LoginResult
import com.example.ecopoints.app.data.repository.SettingsRepository
import com.example.ecopoints.app.data.repository.UserRepository
import com.example.ecopoints.app.util.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Nombre de la sesión guardada, para ofrecer "Continuar como...". */
    val savedUserName: String? = null,
    val resetLoading: Boolean = false,
    val resetError: String? = null
)

/** Eventos de una sola vez que la pantalla convierte en navegación o avisos. */
sealed interface LoginEvent {
    data class LoggedIn(val isNewUser: Boolean) : LoginEvent
    data class PasswordUpdated(val email: String) : LoginEvent
}

/** Inicio de sesión (HU-02) y recuperación de contraseña. La pantalla solo dibuja este estado. */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModel(
    private val userRepository: UserRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val form = MutableStateFlow(LoginUiState())

    private val savedUserName: Flow<String?> = settingsRepository.sessionUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else userRepository.observeUser(userId).map { it?.name?.ifBlank { null } }
    }

    val uiState: StateFlow<LoginUiState> = combine(form, savedUserName) { state, name ->
        state.copy(savedUserName = name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoginUiState())

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    /** Al escribir se borra el error anterior. */
    fun onInputChanged() {
        if (form.value.error != null) form.update { it.copy(error = null) }
    }

    fun login(email: String, password: String) {
        if (form.value.isLoading) return
        val validation = Validators.loginError(email, password)
        if (validation != null) {
            form.update { it.copy(error = validation) }
            return
        }
        viewModelScope.launch {
            form.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.login(email, password)) {
                is LoginResult.Success -> {
                    form.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoggedIn(result.isFirstTime))
                }
                LoginResult.NoAccount -> form.update {
                    it.copy(isLoading = false, error = "No hay una cuenta con ese correo. Regístrate primero.")
                }
                LoginResult.WrongPassword -> form.update {
                    it.copy(isLoading = false, error = "Contraseña incorrecta")
                }
            }
        }
    }

    fun onResetInputChanged() {
        if (form.value.resetError != null) form.update { it.copy(resetError = null) }
    }

    fun clearResetState() {
        form.update { it.copy(resetLoading = false, resetError = null) }
    }

    fun resetPassword(userName: String, email: String, newPassword: String, confirmPassword: String) {
        if (form.value.resetLoading) return
        val validation = Validators.resetPasswordError(userName, email, newPassword, confirmPassword)
        if (validation != null) {
            form.update { it.copy(resetError = validation) }
            return
        }
        viewModelScope.launch {
            form.update { it.copy(resetLoading = true, resetError = null) }
            if (userRepository.resetPassword(userName, email, newPassword)) {
                form.update { it.copy(resetLoading = false) }
                _events.send(LoginEvent.PasswordUpdated(email.trim()))
            } else {
                form.update {
                    it.copy(
                        resetLoading = false,
                        resetError = "El nombre de usuario y el correo no coinciden con la cuenta registrada"
                    )
                }
            }
        }
    }
}
