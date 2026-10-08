package com.example.ecopoints.app.data.repository

import androidx.room.withTransaction
import com.example.ecopoints.app.data.Achievement
import com.example.ecopoints.app.data.AchievementStats
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.local.db.AchievementUnlockEntity
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.toDomain
import com.example.ecopoints.app.data.model.DailyVisit
import com.example.ecopoints.app.data.model.UserProgress
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

/** Puntos, racha diaria y logros. El nivel y el ranking dependen de los puntos históricos. */
class ProgressRepository(private val database: EcoDatabase) {
    private val progressDao = database.progressDao()
    private val challengeDao = database.challengeDao()
    private val achievementDao = database.achievementDao()

    fun observeProgress(userId: Long): Flow<UserProgress?> =
        progressDao.observe(userId).map { it?.toDomain() }

    suspend fun earn(userId: Long, amount: Int) = progressDao.earn(userId, amount)

    /**
     * Registra la visita de hoy. La primera visita del día suma un bono al azar y alarga la racha
     * si ayer también entró (si no, la racha vuelve a 1). Devuelve null si hoy ya había entrado.
     */
    suspend fun registerDailyVisit(
        userId: Long,
        today: String = DateUtils.dayKey(0),
        yesterday: String = DateUtils.dayKey(-1),
        bonus: Int = Random.nextInt(EcoRules.DAILY_BONUS_MIN, EcoRules.DAILY_BONUS_MAX + 1)
    ): DailyVisit? = database.withTransaction {
        val progress = progressDao.get(userId) ?: return@withTransaction null
        if (progress.lastVisitDay == today) return@withTransaction null
        val streak = if (progress.lastVisitDay == yesterday) progress.streak + 1 else 1
        progressDao.updateStreak(userId, streak, maxOf(streak, progress.bestStreak), today)
        progressDao.earn(userId, bonus)
        DailyVisit(streak, bonus)
    }

    /**
     * Revisa los logros y devuelve los que se acaban de desbloquear (para avisar al usuario).
     * Los ya avisados quedan guardados y no se repiten.
     */
    suspend fun checkNewAchievements(userId: Long, now: Long = System.currentTimeMillis()): List<Achievement> =
        database.withTransaction {
            val progress = progressDao.get(userId)?.toDomain() ?: return@withTransaction emptyList()
            val completed = challengeDao.getAll(userId).map { it.toDomain() }.filter { it.completed }
            val stats = statsOf(progress, completed)
            val already = achievementDao.unlockedIds(userId).toSet()
            val newlyUnlocked = Achievement.ALL.filter { it.id !in already && it.isUnlocked(stats) }
            if (newlyUnlocked.isNotEmpty()) {
                achievementDao.insertAll(newlyUnlocked.map { AchievementUnlockEntity(userId, it.id, now) })
            }
            newlyUnlocked
        }

    companion object {
        /** Datos con los que se calcula el progreso de los logros y el impacto ecológico. */
        fun statsOf(progress: UserProgress, completedChallenges: List<EcoChallenge>) =
            AchievementStats(
                completedChallenges = completedChallenges,
                bestStreak = maxOf(progress.bestStreak, progress.streak),
                feedCount = progress.feedCount,
                playCount = progress.playCount,
                historicalPoints = progress.historicalPoints
            )
    }
}
