package com.example.ecopoints.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.Achievement
import com.example.ecopoints.app.data.AchievementStats
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.EcoLevel
import com.example.ecopoints.app.data.PetStage
import com.example.ecopoints.app.data.model.DailyVisit
import com.example.ecopoints.app.data.model.PetSpecies
import com.example.ecopoints.app.data.repository.ChallengeRepository
import com.example.ecopoints.app.data.repository.PetRepository
import com.example.ecopoints.app.data.repository.ProgressRepository
import com.example.ecopoints.app.data.repository.SettingsRepository
import com.example.ecopoints.app.data.repository.UserRepository
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.domain.PetAction
import com.example.ecopoints.app.domain.PetActionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que dibuja el panel principal. Los valores derivados (nivel, etapa, ánimo) se calculan aquí. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val petSpecies: String = PetSpecies.DEFAULT,
    val petName: String = "",
    val balance: Int = 0,
    val historicalPoints: Int = 0,
    val streak: Int = 0,
    val hunger: Int = EcoRules.STAT_MAX,
    val happiness: Int = EcoRules.STAT_MAX,
    val challenges: List<EcoChallenge> = emptyList(),
    val achievementStats: AchievementStats = AchievementStats(emptyList(), 0, 0, 0, 0),
    val dailyVisit: DailyVisit? = null,
    val newAchievements: List<Achievement> = emptyList(),
    /** Acción pendiente de confirmar ("¿Gastar 20 EcoPoints?") si está activado en Ajustes. */
    val pendingAction: PetAction? = null,
    val rankingVisible: Boolean = true,
    val mapRadiusKm: Int = 1,
    val challengeRemindersEnabled: Boolean = true,
    val confirmSpend: Boolean = false
) {
    val petDisplayName: String get() = petName.ifBlank { petSpecies }
    val level: EcoLevel get() = EcoLevel.forPoints(historicalPoints)
    val nextLevel: EcoLevel? get() = EcoLevel.nextAfter(level)
    val petStage: PetStage get() = PetStage.forLevel(level)
    val isHungry: Boolean get() = hunger < EcoRules.PET_LOW_STAT
    val isSad: Boolean get() = happiness < EcoRules.PET_LOW_STAT
}

/** Avisos de una sola vez (mensaje breve o vibración) que la pantalla muestra sin guardarlos en el estado. */
sealed interface HomeEvent {
    data class Message(val text: String) : HomeEvent
    data object Vibrate : HomeEvent
}

/**
 * Panel principal: saldo, mascota, retos, impacto, logros, ranking y EcoMapa.
 * Combina los flujos de Room y DataStore en un único estado y concentra las acciones del usuario.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val petRepository: PetRepository,
    private val challengeRepository: ChallengeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** Diálogos y acciones en curso: no vienen de la base de datos, viven solo mientras se usa la pantalla. */
    private data class Overlay(
        val dailyVisit: DailyVisit? = null,
        val newAchievements: List<Achievement> = emptyList(),
        val pendingAction: PetAction? = null
    )

    private val overlay = MutableStateFlow(Overlay())

    private val data: Flow<HomeUiState> = settingsRepository.sessionUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                userRepository.observeUser(userId),
                progressRepository.observeProgress(userId),
                petRepository.observePet(userId),
                challengeRepository.observeChallenges(userId),
                settingsRepository.settings
            ) { user, progress, pet, challenges, settings ->
                if (user == null || progress == null || pet == null) {
                    HomeUiState()
                } else {
                    HomeUiState(
                        isLoading = false,
                        userName = user.name,
                        petSpecies = pet.species,
                        petName = pet.name,
                        balance = progress.balance,
                        historicalPoints = progress.historicalPoints,
                        streak = progress.streak,
                        hunger = pet.hunger,
                        happiness = pet.happiness,
                        challenges = challenges,
                        achievementStats = ProgressRepository.statsOf(progress, challenges.filter { it.completed }),
                        rankingVisible = settings.rankingVisible,
                        mapRadiusKm = settings.mapRadiusKm,
                        challengeRemindersEnabled = settings.challengeRemindersEnabled,
                        confirmSpend = settings.confirmSpend
                    )
                }
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(data, overlay) { state, extra ->
        state.copy(
            dailyVisit = extra.dailyVisit,
            newAchievements = extra.newAchievements,
            pendingAction = extra.pendingAction
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    init {
        // Al abrir el panel: se descuenta el desgaste acumulado, se registra la visita del día
        // (bono y racha) y se revisan los logros.
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.filterNotNull().first()
            petRepository.applyDecay(userId)
            val visit = progressRepository.registerDailyVisit(userId)
            val unlocked = progressRepository.checkNewAchievements(userId)
            overlay.update { it.copy(dailyVisit = visit, newAchievements = unlocked) }
        }
    }

    /** Con la pantalla abierta las barras bajan en vivo (la pantalla llama a esto cada 5 s). */
    fun tickDecay() {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            petRepository.applyDecay(userId)
        }
    }

    // ---------- Mascota ----------

    /** Revisa si la acción tiene sentido y, si está activado, pide confirmación antes de gastar. */
    fun onPetAction(action: PetAction) {
        val state = uiState.value
        val name = state.petDisplayName
        when {
            action == PetAction.FEED && state.hunger >= EcoRules.STAT_MAX ->
                message("¡$name ya está lleno! No necesita comer ahora")
            action == PetAction.PLAY && state.happiness >= EcoRules.STAT_MAX ->
                message("¡$name ya está súper feliz! Déjalo descansar un rato")
            state.balance < action.cost -> message(INSUFFICIENT_POINTS)
            state.confirmSpend -> overlay.update { it.copy(pendingAction = action) }
            else -> perform(action)
        }
    }

    fun confirmPendingAction() {
        val action = overlay.value.pendingAction ?: return
        overlay.update { it.copy(pendingAction = null) }
        perform(action)
    }

    fun dismissPendingAction() {
        overlay.update { it.copy(pendingAction = null) }
    }

    private fun perform(action: PetAction) {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            val name = uiState.value.petDisplayName
            when (petRepository.perform(userId, action)) {
                PetActionResult.Done -> {
                    message(
                        when (action) {
                            PetAction.FEED -> "¡$name comió rico! (-${action.cost} pts, +Energía)"
                            PetAction.PLAY -> "¡$name está jugando alegremente! (-${action.cost} pts, -10 energía)"
                        }
                    )
                    vibrate()
                    checkAchievements(userId)
                }
                PetActionResult.NotEnoughPoints -> message(INSUFFICIENT_POINTS)
                PetActionResult.AlreadyFull -> message("¡$name no necesita eso ahora!")
                PetActionResult.NotFound -> Unit
            }
        }
    }

    // ---------- Retos ----------

    fun addChallenge(icon: String, title: String, description: String, days: Int) {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            challengeRepository.add(userId, icon, title, description, days)
            message("Reto agregado: tienes ${EcoChallenge.durationLabel(days)} para cumplirlo")
        }
    }

    fun updateChallenge(id: Long, icon: String, title: String, description: String, days: Int) {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            challengeRepository.update(userId, id, icon, title, description, days)
            message("Reto actualizado")
        }
    }

    fun deleteChallenge(id: Long) {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            challengeRepository.delete(userId, id)
        }
    }

    /** El usuario envió la foto de evidencia: el reto se cumple y gana sus EcoPoints. */
    fun completeChallenge(id: Long, evidencePath: String) {
        viewModelScope.launch {
            val userId = settingsRepository.sessionUserId.first() ?: return@launch
            val points = challengeRepository.complete(userId, id, evidencePath)
            if (points != null) {
                vibrate()
                message("¡Reto superado! +$points EcoPoints")
                checkAchievements(userId)
            }
        }
    }

    // ---------- Diálogos ----------

    fun dismissDailyVisit() {
        overlay.update { it.copy(dailyVisit = null) }
    }

    fun dismissAchievements() {
        overlay.update { it.copy(newAchievements = emptyList()) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.endSession()
            onDone()
        }
    }

    // ---------- Auxiliares ----------

    private suspend fun checkAchievements(userId: Long) {
        val unlocked = progressRepository.checkNewAchievements(userId)
        if (unlocked.isNotEmpty()) overlay.update { it.copy(newAchievements = it.newAchievements + unlocked) }
    }

    private fun message(text: String) {
        _events.trySend(HomeEvent.Message(text))
    }

    private suspend fun vibrate() {
        if (settingsRepository.settings.first().vibrationEnabled) _events.send(HomeEvent.Vibrate)
    }

    private companion object {
        const val INSUFFICIENT_POINTS = "¡Puntos insuficientes! Completa retos para ganar más."
    }
}
