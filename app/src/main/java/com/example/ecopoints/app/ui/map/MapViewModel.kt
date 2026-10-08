package com.example.ecopoints.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecopoints.app.data.LocationProvider
import com.example.ecopoints.app.data.RecyclingPoint
import com.example.ecopoints.app.data.RecyclingPointsRepository
import com.example.ecopoints.app.data.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LatLon(val latitude: Double, val longitude: Double)

/** Estado de la búsqueda de puntos de reciclaje. */
sealed interface MapStatus {
    data object Loading : MapStatus
    data object NeedsPermission : MapStatus
    data object NoLocation : MapStatus
    data object NetworkError : MapStatus
    data class Loaded(val points: List<RecyclingPoint>) : MapStatus
}

data class MapUiState(
    /** Radio elegido en Ajustes; null mientras se lee de DataStore. */
    val radiusKm: Int? = null,
    val userLocation: LatLon? = null,
    val status: MapStatus = MapStatus.Loading,
    val selected: RecyclingPoint? = null
)

/**
 * EcoMapa (US-08): ubicación del usuario con GPS y puntos de reciclaje reales de OpenStreetMap
 * dentro del radio elegido. El permiso de ubicación lo pide la pantalla y se informa con [load].
 */
class MapViewModel(
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val search = MutableStateFlow(MapUiState())
    private var loadJob: Job? = null

    val uiState: StateFlow<MapUiState> = combine(
        search,
        settingsRepository.settings.map { it.mapRadiusKm }
    ) { state, radius -> state.copy(radiusKm = radius) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    /** Busca los puntos cercanos; se vuelve a llamar al conceder el permiso, al reintentar y al cambiar el radio. */
    fun load(hasLocationPermission: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (!hasLocationPermission) {
                search.update { it.copy(status = MapStatus.NeedsPermission) }
                return@launch
            }
            val radiusKm = settingsRepository.settings.first().mapRadiusKm
            search.update { it.copy(status = MapStatus.Loading, selected = null) }

            val location = locationProvider.currentLocation()
            if (location == null) {
                search.update { it.copy(status = MapStatus.NoLocation) }
                return@launch
            }
            search.update { it.copy(userLocation = LatLon(location.latitude, location.longitude)) }

            val status = try {
                MapStatus.Loaded(
                    RecyclingPointsRepository.findNearby(location.latitude, location.longitude, radiusKm)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MapStatus.NetworkError
            }
            search.update { it.copy(status = status) }
        }
    }

    fun setRadius(radiusKm: Int) {
        viewModelScope.launch { settingsRepository.setMapRadiusKm(radiusKm) }
    }

    fun select(point: RecyclingPoint?) {
        search.update { it.copy(selected = point) }
    }
}
