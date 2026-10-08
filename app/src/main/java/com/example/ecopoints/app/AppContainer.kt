package com.example.ecopoints.app

import android.content.Context
import com.example.ecopoints.app.data.LocationProvider
import com.example.ecopoints.app.data.local.datastore.EcoPreferencesStore
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.legacy.LegacyPrefsMigrator
import com.example.ecopoints.app.data.repository.ChallengeRepository
import com.example.ecopoints.app.data.repository.PetRepository
import com.example.ecopoints.app.data.repository.ProgressRepository
import com.example.ecopoints.app.data.repository.SettingsRepository
import com.example.ecopoints.app.data.repository.UserRepository
import com.example.ecopoints.app.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Contenedor de dependencias de la app (inyección manual). Crea una sola vez la base de datos,
 * DataStore y los repositorios; los ViewModels los reciben a través de [com.example.ecopoints.app.ui.EcoViewModelFactory].
 * Hilt reemplazará esta clase en un sprint posterior.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Alcance de trabajos que deben terminar aunque la pantalla se cierre (alarmas, receivers). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: EcoDatabase by lazy { EcoDatabase.create(appContext) }
    private val preferencesStore: EcoPreferencesStore by lazy { EcoPreferencesStore(appContext) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(preferencesStore) }
    val userRepository: UserRepository by lazy { UserRepository(database, settingsRepository) }
    val petRepository: PetRepository by lazy { PetRepository(database) }
    val progressRepository: ProgressRepository by lazy { ProgressRepository(database) }
    val challengeRepository: ChallengeRepository by lazy { ChallengeRepository(database) }

    val locationProvider: LocationProvider by lazy { LocationProvider(appContext) }

    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(appContext, settingsRepository, petRepository, challengeRepository)
    }

    /** Pasa a Room y DataStore los datos guardados por la versión anterior (SharedPreferences). */
    val legacyMigrator: LegacyPrefsMigrator by lazy { LegacyPrefsMigrator(appContext, database, preferencesStore) }
}
