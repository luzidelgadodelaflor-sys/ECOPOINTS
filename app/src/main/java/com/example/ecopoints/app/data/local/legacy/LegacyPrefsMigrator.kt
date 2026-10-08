package com.example.ecopoints.app.data.local.legacy

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.ecopoints.app.data.local.datastore.EcoPreferencesStore
import com.example.ecopoints.app.data.local.db.AchievementUnlockEntity
import com.example.ecopoints.app.data.local.db.ChallengeEntity
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.PetEntity
import com.example.ecopoints.app.data.local.db.ProgressEntity
import com.example.ecopoints.app.data.local.db.UserEntity
import com.example.ecopoints.app.data.model.AppSettings
import com.example.ecopoints.app.data.model.PetSpecies
import com.example.ecopoints.app.data.model.ThemeMode
import com.example.ecopoints.app.domain.EcoRules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONException

/**
 * Pasa a Room y DataStore los datos que la versión anterior guardaba en SharedPreferences
 * ("ecopoints_prefs"): cuenta, contraseña cifrada, puntos, mascota, retos, logros y ajustes.
 * Así, quien ya usaba la app no pierde su progreso al actualizar. Se ejecuta una sola vez.
 */
class LegacyPrefsMigrator(
    private val context: Context,
    private val database: EcoDatabase,
    private val store: EcoPreferencesStore
) {
    private val lock = Mutex()

    suspend fun migrateIfNeeded() = lock.withLock {
        if (store.legacyMigrated.first()) return@withLock

        val legacy = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val email = legacy.getString("user_email", "").orEmpty().trim().lowercase()
        if (email.isBlank()) {
            // Instalación nueva (o sin cuenta): no hay nada que migrar.
            store.markLegacyMigrated()
            return@withLock
        }

        val userId = importAccount(legacy, email)
        val wasLoggedIn = legacy.getBoolean("user_logged_in", false)
        store.importLegacy(readSettings(legacy), if (wasLoggedIn) userId else null)
        store.markLegacyMigrated()
        // Los datos ya viven en Room/DataStore: se borra la copia antigua (incluye el hash de la contraseña).
        legacy.edit().clear().apply()
    }

    private suspend fun importAccount(legacy: SharedPreferences, email: String): Long {
        val now = System.currentTimeMillis()
        val existing = database.userDao().findByEmail(email)
        if (existing != null) return existing.id // la migración se interrumpió antes: ya estaba importada

        return database.withTransaction {
            val userId = database.userDao().insert(
                UserEntity(
                    name = legacy.getString("user_name", "").orEmpty().ifBlank { "EcoAmigo" },
                    email = email,
                    passwordHash = legacy.getString("password_hash", null),
                    passwordSalt = legacy.getString("password_salt", null),
                    createdAt = now
                )
            )

            val streak = legacy.getInt("streak_count", 0)
            database.progressDao().upsert(
                ProgressEntity(
                    userId = userId,
                    balance = legacy.getInt("ecopoints_balance", 0),
                    historicalPoints = legacy.getInt("ecopoints_historical", 0),
                    streak = streak,
                    bestStreak = maxOf(legacy.getInt("best_streak", 0), streak),
                    lastVisitDay = legacy.getString("last_visit_day", "").orEmpty(),
                    feedCount = legacy.getInt("pet_feed_count", 0),
                    playCount = legacy.getInt("pet_play_count", 0)
                )
            )

            // La mascota se guardaba como "Koala (Nombre)" o solo "Koala".
            val petText = legacy.getString("pet_level", "").orEmpty()
            database.petDao().upsert(
                PetEntity(
                    userId = userId,
                    species = PetSpecies.fromText(petText),
                    name = if (petText.contains("(")) petText.substringAfter("(").substringBefore(")") else "",
                    hunger = legacy.getInt("pet_hunger", EcoRules.STAT_MAX).coerceIn(0, EcoRules.STAT_MAX),
                    happiness = legacy.getInt("pet_happiness", EcoRules.STAT_MAX).coerceIn(0, EcoRules.STAT_MAX),
                    decayTimestamp = legacy.getLong("pet_decay_timestamp", 0L)
                )
            )

            val reminded = legacy.getStringSet("reminded_challenges", emptySet()).orEmpty()
            readChallenges(legacy, userId, reminded).forEach { database.challengeDao().insert(it) }

            val unlocked = legacy.getStringSet("unlocked_achievements", emptySet()).orEmpty()
            database.achievementDao().insertAll(unlocked.map { AchievementUnlockEntity(userId, it, now) })

            userId
        }
    }

    /** Los retos se guardaban como un arreglo JSON dentro de una sola preferencia. */
    private fun readChallenges(legacy: SharedPreferences, userId: Long, reminded: Set<String>): List<ChallengeEntity> {
        val raw = legacy.getString("user_challenges", null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                val id = o.getLong("id")
                val startMillis = o.getLong("startMillis")
                val durationDays = o.getInt("durationDays")
                val deadline = startMillis + durationDays * DAY_MILLIS
                ChallengeEntity(
                    id = id,
                    userId = userId,
                    icon = o.getString("icon"),
                    title = o.getString("title"),
                    description = o.getString("description"),
                    durationDays = durationDays,
                    startMillis = startMillis,
                    completed = o.optBoolean("completed", false),
                    completedMillis = o.optLong("completedMillis", 0L),
                    evidencePath = o.optString("evidencePath", ""),
                    remindedDeadline = if ("$id:$deadline" in reminded) deadline else 0L
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    private fun readSettings(legacy: SharedPreferences): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == legacy.getString("theme_mode", null) }
                ?: defaults.themeMode,
            largeText = legacy.getBoolean("large_text", defaults.largeText),
            challengeRemindersEnabled = legacy.getBoolean("notifications_enabled", defaults.challengeRemindersEnabled),
            petRemindersEnabled = legacy.getBoolean("pet_reminders_enabled", defaults.petRemindersEnabled),
            rankingVisible = legacy.getBoolean("ranking_visible", defaults.rankingVisible),
            confirmSpend = legacy.getBoolean("confirm_spend", defaults.confirmSpend),
            vibrationEnabled = legacy.getBoolean("vibration_enabled", defaults.vibrationEnabled),
            mapRadiusKm = legacy.getInt("eco_map_search_radius_km", defaults.mapRadiusKm)
                .takeIf { it in EcoRules.MAP_RADIUS_OPTIONS_KM } ?: defaults.mapRadiusKm
        )
    }

    private companion object {
        const val LEGACY_PREFS_NAME = "ecopoints_prefs"
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
