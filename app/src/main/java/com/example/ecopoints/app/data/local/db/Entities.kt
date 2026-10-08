package com.example.ecopoints.app.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Cuenta del usuario. El correo es único; la contraseña se guarda como hash PBKDF2 + sal (Base64). */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String?,
    @ColumnInfo(name = "password_salt") val passwordSalt: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

/** Saldo disponible, puntos históricos (definen nivel y ranking), racha y contadores de cuidado. */
@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProgressEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Long,
    val balance: Int = 0,
    @ColumnInfo(name = "historical_points") val historicalPoints: Int = 0,
    val streak: Int = 0,
    @ColumnInfo(name = "best_streak") val bestStreak: Int = 0,
    @ColumnInfo(name = "last_visit_day") val lastVisitDay: String = "",
    @ColumnInfo(name = "feed_count") val feedCount: Int = 0,
    @ColumnInfo(name = "play_count") val playCount: Int = 0
)

/** Mascota del usuario con sus indicadores vitales (0-100) y el reloj del desgaste. */
@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PetEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Long,
    val species: String,
    val name: String,
    val hunger: Int = 100,
    val happiness: Int = 100,
    @ColumnInfo(name = "decay_timestamp") val decayTimestamp: Long = 0L
)

/** Reto elegido por el usuario, con su plazo, su evidencia (foto) y el aviso de vencimiento. */
@Entity(
    tableName = "challenges",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val icon: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "duration_days") val durationDays: Int,
    @ColumnInfo(name = "start_millis") val startMillis: Long,
    val completed: Boolean = false,
    @ColumnInfo(name = "completed_millis") val completedMillis: Long = 0L,
    @ColumnInfo(name = "evidence_path") val evidencePath: String = "",
    @ColumnInfo(name = "reminded_deadline") val remindedDeadline: Long = 0L
)

/** Logros que el usuario ya desbloqueó (para avisar una sola vez). */
@Entity(
    tableName = "achievement_unlocks",
    primaryKeys = ["user_id", "achievement_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class AchievementUnlockEntity(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "achievement_id") val achievementId: String,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long
)
