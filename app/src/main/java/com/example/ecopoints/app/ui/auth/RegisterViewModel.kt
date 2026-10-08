package com.example.ecopoints.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.model.RegisterResult
import com.example.ecopoints.app.data.repository.UserRepository
import com.example.ecopoints.app.util.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface RegisterEvent {
    /** La cuenta se creó y la sesión quedó iniciada. */
    data object Registered : RegisterEvent
}

/** Registro de cuenta (HU-01): valida los datos y crea usuario, progreso y mascota. */
class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onInputChanged() {
        if (_uiState.value.error != null) _uiState.update { it.copy(error = null) }
    }

    fun register(
        name: String,
        petName: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean,
        species: String
    ) {
        if (_uiState.value.isLoading) return
        val validation = Validators.registerError(name, petName, email, password, confirmPassword, termsAccepted)
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (userRepository.register(name, email, password, species, petName)) {
                is RegisterResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.Registered)
                }
                RegisterResult.EmailTaken -> _uiState.update {
                    it.copy(isLoading = false, error = "Ya existe una cuenta con este correo. Inicia sesión.")
                }
            }
        }
    }
}
