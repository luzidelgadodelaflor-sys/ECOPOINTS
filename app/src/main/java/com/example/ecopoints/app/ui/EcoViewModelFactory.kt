package com.example.ecopoints.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecopoints.app.AppContainer
import com.example.ecopoints.app.ui.auth.LoginViewModel
import com.example.ecopoints.app.ui.auth.RegisterViewModel
import com.example.ecopoints.app.ui.home.HomeViewModel
import com.example.ecopoints.app.ui.map.MapViewModel
import com.example.ecopoints.app.ui.settings.SettingsViewModel
import com.example.ecopoints.app.ui.welcome.WelcomeViewModel

/** Crea cada ViewModel con los repositorios del [AppContainer] (inyección manual). */
class EcoViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(container.userRepository, container.settingsRepository)
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(container.userRepository)
            modelClass.isAssignableFrom(WelcomeViewModel::class.java) ->
                WelcomeViewModel(
                    container.userRepository,
                    container.progressRepository,
                    container.petRepository,
                    container.settingsRepository
                )
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(
                    container.userRepository,
                    container.progressRepository,
                    container.petRepository,
                    container.challengeRepository,
                    container.settingsRepository
                )
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.settingsRepository, container.userRepository)
            modelClass.isAssignableFrom(MapViewModel::class.java) ->
                MapViewModel(container.settingsRepository, container.locationProvider)
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
        return viewModel as T
    }
}
