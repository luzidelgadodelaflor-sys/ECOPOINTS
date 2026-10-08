package com.example.ecopoints.app.data.repository

import androidx.room.withTransaction
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.local.db.ChallengeEntity
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Retos del usuario: crear, editar, borrar, cumplir con evidencia y avisar cuando se acaba el plazo. */
class ChallengeRepository(private val database: EcoDatabase) {
    private val challengeDao = database.challengeDao()
    private val progressDao = database.progressDao()

    fun observeChallenges(userId: Long): Flow<List<EcoChallenge>> =
        challengeDao.observeAll(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getChallenges(userId: Long): List<EcoChallenge> =
        challengeDao.getAll(userId).map { it.toDomain() }

    suspend fun add(
        userId: Long,
        icon: String,
        title: String,
        description: String,
        days: Int,
        now: Long = System.currentTimeMillis()
    ): Long = challengeDao.insert(
        ChallengeEntity(
            userId = userId,
            icon = icon,
            title = title,
            description = description,
            durationDays = days,
            startMillis = now
        )
    )

    /** El plazo vuelve a contar desde hoy si lo cambian o si ya había vencido. */
    suspend fun update(
        userId: Long,
        id: Long,
        icon: String,
        title: String,
        description: String,
        days: Int,
        now: Long = System.currentTimeMillis()
    ) {
        database.withTransaction {
            val current = challengeDao.get(id, userId)?.toDomain() ?: return@withTransaction
            val restart = days != current.durationDays || current.isExpired(now)
            challengeDao.update(
                ChallengeEntity(
                    id = current.id,
                    userId = userId,
                    icon = icon,
                    title = title,
                    description = description,
                    durationDays = days,
                    startMillis = if (restart) now else current.startMillis,
                    completed = current.completed,
                    completedMillis = current.completedMillis,
                    evidencePath = current.evidencePath,
                    remindedDeadline = current.remindedDeadline
                )
            )
        }
    }

    suspend fun delete(userId: Long, id: Long) = challengeDao.delete(id, userId)

    /**
     * Da el reto por cumplido con su foto de evidencia y suma los EcoPoints (saldo e histórico).
     * Devuelve los puntos ganados, o null si el reto no existe, ya estaba cumplido o venció.
     */
    suspend fun complete(
        userId: Long,
        id: Long,
        evidencePath: String,
        now: Long = System.currentTimeMillis()
    ): Int? = database.withTransaction {
        val challenge = challengeDao.get(id, userId)?.toDomain() ?: return@withTransaction null
        if (challenge.completed || challenge.isExpired(now)) return@withTransaction null
        challengeDao.update(
            ChallengeEntity(
                id = challenge.id,
                userId = userId,
                icon = challenge.icon,
                title = challenge.title,
                description = challenge.description,
                durationDays = challenge.durationDays,
                startMillis = challenge.startMillis,
                completed = true,
                completedMillis = now,
                evidencePath = evidencePath,
                remindedDeadline = challenge.remindedDeadline
            )
        )
        progressDao.earn(userId, challenge.points)
        challenge.points
    }

    /** Retos pendientes que ya entraron en su ventana de aviso y aún no se avisaron. */
    suspend fun dueForReminder(userId: Long, now: Long): List<EcoChallenge> =
        pendingNotReminded(userId, now)
            .filter { it.reminderMillis <= now }
            .sortedBy { it.deadlineMillis }

    /** Marca el aviso como hecho para el plazo actual de cada reto (si se edita el plazo, vuelve a avisar). */
    suspend fun markReminded(userId: Long, challenges: List<EcoChallenge>) {
        database.withTransaction {
            challenges.forEach { due ->
                val entity = challengeDao.get(due.id, userId) ?: return@forEach
                challengeDao.update(entity.copy(remindedDeadline = due.deadlineMillis))
            }
        }
    }

    /** Momento del próximo aviso pendiente, o null si no hay ninguno. */
    suspend fun nextReminderMillis(userId: Long, now: Long = System.currentTimeMillis()): Long? =
        pendingNotReminded(userId, now).minOfOrNull { it.reminderMillis }

    private suspend fun pendingNotReminded(userId: Long, now: Long): List<EcoChallenge> =
        getChallenges(userId).filter { !it.completed && now < it.deadlineMillis && !it.alreadyReminded }
}
