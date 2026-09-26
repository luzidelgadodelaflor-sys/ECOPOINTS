package com.example.ecopoints.app.data
// Ajusta el nombre del paquete de arriba para que coincida con el de tu proyecto
// (el que definiste al crear el "Empty Activity" en Android Studio).

import android.content.Context
import android.content.SharedPreferences

/**
 * Encapsula el acceso a SharedPreferences para EcoPoints.
 * Uso típico:
 *   val prefs = PreferencesManager(context)
 *   prefs.setUserLoggedIn(true)
 *   val saldo = prefs.getEcoPointsBalance()
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ecopoints_prefs"

        // Claves (keys)
        private const val KEY_LOGGED_IN = "user_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ECOPOINTS_BALANCE = "ecopoints_balance"
        private const val KEY_ECOPOINTS_HISTORICAL = "ecopoints_historical"
        private const val KEY_PET_LEVEL = "pet_level"
        private const val KEY_PET_HUNGER = "pet_hunger"
        private const val KEY_PET_HAPPINESS = "pet_happiness"
        private const val KEY_LAST_FED_TIMESTAMP = "last_fed_timestamp"
        private const val KEY_DAILY_CHALLENGE_PROGRESS = "daily_challenge_progress"
        private const val KEY_DAILY_CHALLENGE_DATE = "daily_challenge_date"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        // Ajustes de la aplicación (orientados a EcoPoints)
        private const val KEY_RANKING_VISIBLE = "ranking_visible"
        private const val KEY_PET_SOUND_ENABLED = "pet_sound_enabled"
        private const val KEY_MAP_DISTANCE_UNIT = "map_distance_unit"
        private const val KEY_MAP_SEARCH_RADIUS_KM = "eco_map_search_radius_km"
    }

    // 1. Sesión activa
    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }
    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    // 2. Nombre del usuario
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    // 3. Saldo disponible de EcoPoints
    fun setEcoPointsBalance(value: Int) {
        prefs.edit().putInt(KEY_ECOPOINTS_BALANCE, value).apply()
    }
    fun getEcoPointsBalance(): Int = prefs.getInt(KEY_ECOPOINTS_BALANCE, 0)
    /** Suma (o resta, con valor negativo) al saldo disponible. */
    fun addEcoPointsBalance(delta: Int) {
        setEcoPointsBalance(getEcoPointsBalance() + delta)
    }

    // 4. EcoPoints históricos acumulados (define nivel y ranking)
    fun setEcoPointsHistorical(value: Int) {
        prefs.edit().putInt(KEY_ECOPOINTS_HISTORICAL, value).apply()
    }
    fun getEcoPointsHistorical(): Int = prefs.getInt(KEY_ECOPOINTS_HISTORICAL, 0)
    fun addEcoPointsHistorical(delta: Int) {
        setEcoPointsHistorical(getEcoPointsHistorical() + delta)
    }

    /**
     * Registra EcoPoints ganados por una actividad: suma tanto al saldo
     * disponible como al histórico (regla de negocio: el nivel depende
     * del histórico, no del saldo).
     */
    fun earnEcoPoints(amount: Int) {
        addEcoPointsBalance(amount)
        addEcoPointsHistorical(amount)
    }

    /**
     * Descuenta EcoPoints al alimentar o personalizar (solo afecta el saldo
     * disponible, el histórico nunca baja).
     */
    fun spendEcoPoints(amount: Int): Boolean {
        val current = getEcoPointsBalance()
        if (current < amount) return false
        setEcoPointsBalance(current - amount)
        return true
    }

    // 5. Nivel de la mascota (Semilla, Brote, Guardián, EcoHéroe, EcoLeyenda)
    fun setPetLevel(level: String) {
        prefs.edit().putString(KEY_PET_LEVEL, level).apply()
    }
    fun getPetLevel(): String = prefs.getString(KEY_PET_LEVEL, "Semilla") ?: "Semilla"

    // 6. Indicadores vitales de la mascota (0-100)
    fun setPetHunger(value: Int) {
        prefs.edit().putInt(KEY_PET_HUNGER, value.coerceIn(0, 100)).apply()
    }
    fun getPetHunger(): Int = prefs.getInt(KEY_PET_HUNGER, 100)

    fun setPetHappiness(value: Int) {
        prefs.edit().putInt(KEY_PET_HAPPINESS, value.coerceIn(0, 100)).apply()
    }
    fun getPetHappiness(): Int = prefs.getInt(KEY_PET_HAPPINESS, 100)

    // 7. Última vez alimentada (timestamp en millis)
    fun setLastFedTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_FED_TIMESTAMP, timestamp).apply()
    }
    fun getLastFedTimestamp(): Long = prefs.getLong(KEY_LAST_FED_TIMESTAMP, 0L)

    // 8. Progreso del reto del día
    fun setDailyChallengeProgress(progress: Int) {
        prefs.edit().putInt(KEY_DAILY_CHALLENGE_PROGRESS, progress).apply()
    }
    fun getDailyChallengeProgress(): Int = prefs.getInt(KEY_DAILY_CHALLENGE_PROGRESS, 0)

    // 9. Fecha del reto (para reiniciarlo automáticamente al cambiar de día)
    fun setDailyChallengeDate(date: String) {
        prefs.edit().putString(KEY_DAILY_CHALLENGE_DATE, date).apply()
    }
    fun getDailyChallengeDate(): String = prefs.getString(KEY_DAILY_CHALLENGE_DATE, "") ?: ""

    /**
     * Compara la fecha guardada con la fecha actual (formato "yyyy-MM-dd")
     * y reinicia el progreso del reto si cambió el día.
     */
    fun resetDailyChallengeIfNewDay(today: String) {
        if (getDailyChallengeDate() != today) {
            setDailyChallengeProgress(0)
            setDailyChallengeDate(today)
        }
    }

    // 10. Notificaciones / recordatorios de retos diarios
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    fun areNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    // ---------- Ajustes de EcoPoints (pantalla de Configuración) ----------

    /** Visibilidad del usuario en el Ranking público de usuarios. */
    fun setRankingVisible(visible: Boolean) {
        prefs.edit().putBoolean(KEY_RANKING_VISIBLE, visible).apply()
    }
    fun isRankingVisible(): Boolean = prefs.getBoolean(KEY_RANKING_VISIBLE, true)

    /** Sonido al interactuar con la mascota (alimentar, jugar, etc.). */
    fun setPetSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PET_SOUND_ENABLED, enabled).apply()
    }
    fun isPetSoundEnabled(): Boolean = prefs.getBoolean(KEY_PET_SOUND_ENABLED, true)

    /** Unidad de distancia usada en EcoMapa: "km" o "millas". */
    fun setMapDistanceUnit(unit: String) {
        prefs.edit().putString(KEY_MAP_DISTANCE_UNIT, unit).apply()
    }
    fun getMapDistanceUnit(): String = prefs.getString(KEY_MAP_DISTANCE_UNIT, "km") ?: "km"

    /** Radio (en km) en el que EcoMapa busca puntos ecológicos cercanos. */
    fun setMapSearchRadiusKm(radiusKm: Int) {
        prefs.edit().putInt(KEY_MAP_SEARCH_RADIUS_KM, radiusKm).apply()
    }
    fun getMapSearchRadiusKm(): Int = prefs.getInt(KEY_MAP_SEARCH_RADIUS_KM, 1)

    /** Limpia todas las preferencias, por ejemplo al cerrar sesión. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}