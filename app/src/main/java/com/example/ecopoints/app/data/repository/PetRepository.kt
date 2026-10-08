package com.example.ecopoints.app.data.repository

import androidx.room.withTransaction
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.toDomain
import com.example.ecopoints.app.data.model.PetState
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.domain.PetAction
import com.example.ecopoints.app.domain.PetActionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** La mascota: indicadores vitales, desgaste con el tiempo y acciones que cuestan EcoPoints. */
class PetRepository(private val database: EcoDatabase) {
    private val petDao = database.petDao()
    private val progressDao = database.progressDao()

    fun observePet(userId: Long): Flow<PetState?> = petDao.observe(userId).map { it?.toDomain() }

    suspend fun getPet(userId: Long): PetState? = petDao.get(userId)?.toDomain()

    /**
     * Descuenta el desgaste acumulado desde la última revisión (aunque la app haya estado cerrada).
     * Solo cuenta periodos completos de 30 minutos; el resto se conserva para la próxima revisión.
     */
    suspend fun applyDecay(userId: Long, now: Long = System.currentTimeMillis()) {
        database.withTransaction {
            val pet = petDao.get(userId) ?: return@withTransaction
            val result = EcoRules.applyDecay(pet.hunger, pet.happiness, pet.decayTimestamp, now)
            val changed = result.hunger != pet.hunger ||
                result.happiness != pet.happiness ||
                result.timestamp != pet.decayTimestamp
            if (changed) petDao.updateStats(userId, result.hunger, result.happiness, result.timestamp)
        }
    }

    /** Alimentar o jugar: gasta EcoPoints del saldo (el histórico nunca baja) y mejora a la mascota. */
    suspend fun perform(userId: Long, action: PetAction): PetActionResult =
        database.withTransaction {
            val pet = petDao.get(userId) ?: return@withTransaction PetActionResult.NotFound
            val full = when (action) {
                PetAction.FEED -> pet.hunger >= EcoRules.STAT_MAX
                PetAction.PLAY -> pet.happiness >= EcoRules.STAT_MAX
            }
            if (full) return@withTransaction PetActionResult.AlreadyFull
            if (progressDao.spend(userId, action.cost) == 0) return@withTransaction PetActionResult.NotEnoughPoints

            when (action) {
                PetAction.FEED -> {
                    // Comer solo recupera energía; la felicidad sube jugando.
                    val hunger = (pet.hunger + EcoRules.FEED_HUNGER_GAIN).coerceAtMost(EcoRules.STAT_MAX)
                    petDao.updateStats(userId, hunger, pet.happiness, pet.decayTimestamp)
                    progressDao.incrementFeedCount(userId)
                }
                PetAction.PLAY -> {
                    val happiness = (pet.happiness + EcoRules.PLAY_HAPPINESS_GAIN).coerceAtMost(EcoRules.STAT_MAX)
                    val hunger = (pet.hunger - EcoRules.PLAY_HUNGER_LOSS).coerceAtLeast(0)
                    petDao.updateStats(userId, hunger, happiness, pet.decayTimestamp)
                    progressDao.incrementPlayCount(userId)
                }
            }
            PetActionResult.Done
        }

    /**
     * Momento en que la energía bajará del umbral si nadie alimenta a la mascota,
     * o null si ya está baja o no hay mascota.
     */
    suspend fun nextLowHungerMillis(userId: Long, now: Long = System.currentTimeMillis()): Long? {
        val pet = petDao.get(userId) ?: return null
        return EcoRules.nextLowHungerMillis(pet.hunger, pet.decayTimestamp, now)
    }
}
