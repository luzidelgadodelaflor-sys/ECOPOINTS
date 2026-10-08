package com.example.ecopoints.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    /** Falla con SQLiteConstraintException si el correo ya existe (índice único). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("UPDATE users SET password_hash = :hash, password_salt = :salt WHERE id = :id")
    suspend fun updatePassword(id: Long, hash: String, salt: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    fun observe(userId: Long): Flow<ProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    suspend fun get(userId: Long): ProgressEntity?

    /** Ganar puntos suma al saldo y al histórico (el nivel depende del histórico). */
    @Query(
        "UPDATE user_progress SET balance = balance + :amount, " +
            "historical_points = historical_points + :amount WHERE user_id = :userId"
    )
    suspend fun earn(userId: Long, amount: Int)

    /** Gastar solo baja el saldo; devuelve 0 filas si no alcanza (el histórico nunca baja). */
    @Query("UPDATE user_progress SET balance = balance - :amount WHERE user_id = :userId AND balance >= :amount")
    suspend fun spend(userId: Long, amount: Int): Int

    @Query("UPDATE user_progress SET feed_count = feed_count + 1 WHERE user_id = :userId")
    suspend fun incrementFeedCount(userId: Long)

    @Query("UPDATE user_progress SET play_count = play_count + 1 WHERE user_id = :userId")
    suspend fun incrementPlayCount(userId: Long)

    @Query(
        "UPDATE user_progress SET streak = :streak, best_streak = :bestStreak, " +
            "last_visit_day = :day WHERE user_id = :userId"
    )
    suspend fun updateStreak(userId: Long, streak: Int, bestStreak: Int, day: String)
}

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE user_id = :userId")
    fun observe(userId: Long): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE user_id = :userId")
    suspend fun get(userId: Long): PetEntity?

    @Query(
        "UPDATE pets SET hunger = :hunger, happiness = :happiness, " +
            "decay_timestamp = :decayTimestamp WHERE user_id = :userId"
    )
    suspend fun updateStats(userId: Long, hunger: Int, happiness: Int, decayTimestamp: Long)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE user_id = :userId ORDER BY id")
    fun observeAll(userId: Long): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE user_id = :userId ORDER BY id")
    suspend fun getAll(userId: Long): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id AND user_id = :userId")
    suspend fun get(id: Long, userId: Long): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity): Long

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE id = :id AND user_id = :userId")
    suspend fun delete(id: Long, userId: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT achievement_id FROM achievement_unlocks WHERE user_id = :userId")
    suspend fun unlockedIds(userId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(unlocks: List<AchievementUnlockEntity>)
}
