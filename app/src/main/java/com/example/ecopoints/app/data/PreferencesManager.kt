package com.example.ecopoints.app.data
// Ajusta el nombre del paquete de arriba para que coincida con el de tu proyecto
// (el que definiste al crear el "Empty Activity" en Android Studio).

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONArray
import org.json.JSONException
import com.example.ecopoints.app.ui.theme.ThemeMode
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.random.Random

/** Resultado de la primera visita del día: racha actual y bono ganado. */
data class DailyVisit(val streak: Int, val bonus: Int)

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
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PASSWORD_SALT = "password_salt"
        private const val PASSWORD_ITERATIONS = 10_000
        private const val PASSWORD_KEY_BITS = 256
        private const val KEY_CHALLENGES = "user_challenges"
        private const val KEY_ECOPOINTS_BALANCE = "ecopoints_balance"
        private const val KEY_ECOPOINTS_HISTORICAL = "ecopoints_historical"
        private const val KEY_PET_LEVEL = "pet_level"
        private const val KEY_PET_HUNGER = "pet_hunger"
        private const val KEY_PET_HAPPINESS = "pet_happiness"
        private const val KEY_LAST_FED_TIMESTAMP = "last_fed_timestamp"
        private const val KEY_PET_DECAY_TIMESTAMP = "pet_decay_timestamp"

        private const val KEY_REMINDED_CHALLENGES = "reminded_challenges"
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_FEED_COUNT = "pet_feed_count"
        private const val KEY_PLAY_COUNT = "pet_play_count"
        private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
        private const val KEY_LAST_VISIT_DAY = "last_visit_day"

        // Desgaste: cada 30 minutos la mascota pierde 10% de energía y 10% de felicidad,
        // pero el desgaste nunca la deja por debajo del mínimo (se ve triste, no "muerta").
        private const val DECAY_PERIOD_MILLIS = 30L * 60 * 1000
        private const val HUNGER_LOSS_PER_PERIOD = 10
        private const val HAPPINESS_LOSS_PER_PERIOD = 10
        const val PET_MIN_STAT = 10

        /** Por debajo de este valor la mascota se ve triste y se avisa con una notificación. */
        const val PET_LOW_STAT = 30

        // Bono diario al azar por volver a la app
        private const val DAILY_BONUS_MIN = 5
        private const val DAILY_BONUS_MAX = 20
        private const val KEY_DAILY_CHALLENGE_PROGRESS = "daily_challenge_progress"
        private const val KEY_DAILY_CHALLENGE_DATE = "daily_challenge_date"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        // Ajustes de la aplicación (orientados a EcoPoints)
        private const val KEY_RANKING_VISIBLE = "ranking_visible"
        private const val KEY_MAP_SEARCH_RADIUS_KM = "eco_map_search_radius_km"
        private const val KEY_PET_REMINDERS_ENABLED = "pet_reminders_enabled"
        private const val KEY_CONFIRM_SPEND = "confirm_spend"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LARGE_TEXT = "large_text"

        /** Radios de búsqueda que se pueden elegir en Ajustes. */
        val MAP_RADIUS_OPTIONS_KM = listOf(1, 3, 5)
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

    // 2b. Correo con el que se registró el usuario (para reconocerlo al iniciar sesión)
    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email.trim().lowercase()).apply()
    }
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    /** Hay una cuenta registrada en este dispositivo. */
    fun hasAccount(): Boolean = getUserEmail().isNotBlank()

    fun isAccountEmail(email: String): Boolean = hasAccount() && getUserEmail() == email.trim().lowercase()

    // 2c. Contraseña: nunca se guarda en texto plano, solo su hash PBKDF2 con una sal aleatoria
    fun savePassword(password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PASSWORD_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PASSWORD_HASH, Base64.encodeToString(hashPassword(password, salt), Base64.NO_WRAP))
            .apply()
    }

    /** Las cuentas creadas antes de guardar contraseñas no tienen una todavía. */
    fun hasPassword(): Boolean = prefs.getString(KEY_PASSWORD_HASH, null) != null

    fun checkPassword(password: String): Boolean {
        val salt = prefs.getString(KEY_PASSWORD_SALT, null) ?: return false
        val hash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        val expected = Base64.decode(hash, Base64.NO_WRAP)
        val actual = hashPassword(password, Base64.decode(salt, Base64.NO_WRAP))
        // Comparación en tiempo constante
        return MessageDigest.isEqual(expected, actual)
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PASSWORD_ITERATIONS, PASSWORD_KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
    }

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

    /**
     * Desgaste de la mascota: por cada periodo de 30 minutos transcurrido desde la última
     * revisión pierde energía y felicidad, aunque la app o la sesión estén cerradas.
     * Solo descuenta periodos completos; el resto se conserva para la siguiente revisión.
     */
    fun applyPetDecay(now: Long = System.currentTimeMillis()) {
        val last = prefs.getLong(KEY_PET_DECAY_TIMESTAMP, 0L)
        if (last == 0L || now < last) {
            prefs.edit().putLong(KEY_PET_DECAY_TIMESTAMP, now).apply()
            return
        }
        val periods = ((now - last) / DECAY_PERIOD_MILLIS).toInt()
        if (periods == 0) return
        setPetHunger(decayed(getPetHunger(), periods * HUNGER_LOSS_PER_PERIOD))
        setPetHappiness(decayed(getPetHappiness(), periods * HAPPINESS_LOSS_PER_PERIOD))
        prefs.edit().putLong(KEY_PET_DECAY_TIMESTAMP, last + periods * DECAY_PERIOD_MILLIS).apply()
    }

    /** Resta el desgaste sin bajar del mínimo (y sin subir un valor que ya estaba por debajo). */
    private fun decayed(value: Int, loss: Int): Int =
        minOf(value, (value - loss).coerceAtLeast(PET_MIN_STAT))

    /**
     * Momento en que la energía bajará de [PET_LOW_STAT] si nadie alimenta a la mascota,
     * o null si ya está baja (no hace falta programar otro aviso).
     */
    fun nextLowHungerMillis(): Long? {
        val hunger = getPetHunger()
        if (hunger < PET_LOW_STAT) return null
        val periods = (hunger - PET_LOW_STAT) / HUNGER_LOSS_PER_PERIOD + 1
        val last = prefs.getLong(KEY_PET_DECAY_TIMESTAMP, System.currentTimeMillis())
        return last + periods * DECAY_PERIOD_MILLIS
    }

    // 6b. Racha de días seguidos entrando a la app y bono diario
    fun getStreak(): Int = prefs.getInt(KEY_STREAK_COUNT, 0)

    /**
     * Registra la visita de hoy. La primera visita del día suma un bono al azar y
     * alarga la racha si ayer también entró (si no, la racha vuelve a 1).
     * Devuelve null si hoy ya había entrado.
     */
    /** Racha más larga alcanzada (para los logros; no baja al perder la racha). */
    fun getBestStreak(): Int = maxOf(prefs.getInt(KEY_BEST_STREAK, 0), getStreak())

    // 6c. Contadores de cuidado de la mascota y logros desbloqueados
    fun getFeedCount(): Int = prefs.getInt(KEY_FEED_COUNT, 0)
    fun incrementFeedCount() {
        prefs.edit().putInt(KEY_FEED_COUNT, getFeedCount() + 1).apply()
    }

    fun getPlayCount(): Int = prefs.getInt(KEY_PLAY_COUNT, 0)
    fun incrementPlayCount() {
        prefs.edit().putInt(KEY_PLAY_COUNT, getPlayCount() + 1).apply()
    }

    fun getAchievementStats() = AchievementStats(
        completedChallenges = getChallenges().filter { it.completed },
        bestStreak = getBestStreak(),
        feedCount = getFeedCount(),
        playCount = getPlayCount(),
        historicalPoints = getEcoPointsHistorical()
    )

    /**
     * Revisa los logros y devuelve los que se acaban de desbloquear (para avisar al usuario).
     * Los ya avisados quedan guardados y no se repiten.
     */
    fun checkNewAchievements(): List<Achievement> {
        val stats = getAchievementStats()
        val alreadyUnlocked = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
        val newlyUnlocked = Achievement.ALL.filter { it.id !in alreadyUnlocked && it.isUnlocked(stats) }
        if (newlyUnlocked.isNotEmpty()) {
            prefs.edit()
                .putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, alreadyUnlocked + newlyUnlocked.map { it.id })
                .apply()
        }
        return newlyUnlocked
    }

    fun registerDailyVisit(): DailyVisit? {
        val today = dayKey(0)
        val lastVisit = prefs.getString(KEY_LAST_VISIT_DAY, "") ?: ""
        if (lastVisit == today) return null
        val streak = if (lastVisit == dayKey(-1)) getStreak() + 1 else 1
        val bonus = Random.nextInt(DAILY_BONUS_MIN, DAILY_BONUS_MAX + 1)
        prefs.edit()
            .putString(KEY_LAST_VISIT_DAY, today)
            .putInt(KEY_STREAK_COUNT, streak)
            .putInt(KEY_BEST_STREAK, maxOf(streak, getBestStreak()))
            .apply()
        earnEcoPoints(bonus)
        return DailyVisit(streak, bonus)
    }

    /** Fecha "yyyy-MM-dd" de hoy desplazada [offsetDays] días. */
    private fun dayKey(offsetDays: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDays) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

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

    /** Retos elegidos por el usuario (con su plazo y estado), guardados como JSON. */
    fun getChallenges(): List<EcoChallenge> {
        val raw = prefs.getString(KEY_CHALLENGES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                EcoChallenge(
                    id = o.getLong("id"),
                    icon = o.getString("icon"),
                    title = o.getString("title"),
                    description = o.getString("description"),
                    durationDays = o.getInt("durationDays"),
                    startMillis = o.getLong("startMillis"),
                    completed = o.optBoolean("completed", false),
                    completedMillis = o.optLong("completedMillis", 0L),
                    evidencePath = o.optString("evidencePath", "")
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun saveChallenges(challenges: List<EcoChallenge>) {
        val array = JSONArray()
        challenges.forEach { c ->
            array.put(
                JSONObject()
                    .put("id", c.id)
                    .put("icon", c.icon)
                    .put("title", c.title)
                    .put("description", c.description)
                    .put("durationDays", c.durationDays)
                    .put("startMillis", c.startMillis)
                    .put("completed", c.completed)
                    .put("completedMillis", c.completedMillis)
                    .put("evidencePath", c.evidencePath)
            )
        }
        prefs.edit().putString(KEY_CHALLENGES, array.toString()).apply()
        setDailyChallengeProgress(challenges.count { it.completed })
    }

    /**
     * Retos que ya recibieron el aviso de "se acaba el tiempo". Se guarda "id:vencimiento",
     * así que si el usuario edita el reto y cambia el plazo, vuelve a avisarse.
     */
    fun getRemindedChallengeKeys(): Set<String> =
        prefs.getStringSet(KEY_REMINDED_CHALLENGES, emptySet()) ?: emptySet()

    fun markChallengesReminded(keys: Collection<String>) {
        // Solo se conservan las marcas de retos que siguen existiendo.
        val existing = getChallenges().map { it.reminderKey }.toSet()
        val updated = (getRemindedChallengeKeys() + keys).filter { it in existing }.toSet()
        prefs.edit().putStringSet(KEY_REMINDED_CHALLENGES, updated).apply()
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

    /** Aviso "tu mascota tiene hambre" (independiente de los avisos de retos). */
    fun setPetRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PET_REMINDERS_ENABLED, enabled).apply()
    }
    fun arePetRemindersEnabled(): Boolean = prefs.getBoolean(KEY_PET_REMINDERS_ENABLED, true)

    /** Pedir confirmación antes de gastar EcoPoints en la mascota. */
    fun setConfirmSpendEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_SPEND, enabled).apply()
    }
    fun isConfirmSpendEnabled(): Boolean = prefs.getBoolean(KEY_CONFIRM_SPEND, false)

    /** Vibración al alimentar, jugar y cumplir retos. */
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)

    /** Tema de la app: sistema, claro u oscuro. */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
    fun getThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME_MODE, null) } ?: ThemeMode.SYSTEM

    /** Texto más grande en toda la app. */
    fun setLargeTextEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
    }
    fun isLargeTextEnabled(): Boolean = prefs.getBoolean(KEY_LARGE_TEXT, false)

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