package com.example.ecopoints.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.ecopoints.app.ui.EcoViewModelFactory
import com.example.ecopoints.app.ui.navigation.EcoNavHost
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoPointsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Única Activity de la app. Solo prepara el tema y entrega el control a [EcoNavHost];
 * cada pantalla es una función composable con su propio ViewModel.
 */
class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as EcoPointsApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = container

        // Antes de dibujar: se pasan a Room/DataStore los datos de la versión anterior (solo la
        // primera vez) y se lee el tema, para que la primera pantalla ya salga con el tema elegido.
        val initialSettings = runBlocking {
            container.legacyMigrator.migrateIfNeeded()
            container.settingsRepository.settings.first()
        }

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = initialSettings)
            val factory = remember { EcoViewModelFactory(container) }
            val navController = rememberNavController()

            EcoPointsTheme(themeMode = settings.themeMode, largeText = settings.largeText) {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    EcoNavHost(navController = navController, viewModelFactory = factory)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Con la app abierta el usuario ve a su mascota: no hace falta el aviso de hambre
        container.reminderScheduler.cancelPetReminder()
    }

    override fun onStop() {
        super.onStop()
        // Al salir se programan los avisos (mascota con hambre y retos por vencer)
        val container = container
        container.appScope.launch { container.reminderScheduler.scheduleAll() }
    }
}
