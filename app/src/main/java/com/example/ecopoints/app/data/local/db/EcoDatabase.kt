package com.example.ecopoints.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos SQLite de EcoPoints (Room). Reemplaza al almacenamiento en SharedPreferences:
 * cuentas, progreso, mascota, retos y logros viven aquí; los ajustes viven en DataStore.
 */
@Database(
    entities = [
        UserEntity::class,
        ProgressEntity::class,
        PetEntity::class,
        ChallengeEntity::class,
        AchievementUnlockEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class EcoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun progressDao(): ProgressDao
    abstract fun petDao(): PetDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val NAME = "ecopoints.db"

        fun create(context: Context): EcoDatabase =
            Room.databaseBuilder(context.applicationContext, EcoDatabase::class.java, NAME).build()
    }
}
