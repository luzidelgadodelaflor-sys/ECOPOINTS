package com.example.ecopoints.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Base64
import androidx.room.withTransaction
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.PetEntity
import com.example.ecopoints.app.data.local.db.ProgressEntity
import com.example.ecopoints.app.data.local.db.UserEntity
import com.example.ecopoints.app.data.local.db.toDomain
import com.example.ecopoints.app.data.model.LoginResult
import com.example.ecopoints.app.data.model.RegisterResult
import com.example.ecopoints.app.data.model.User
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Cuentas de usuario: registro, inicio de sesión, recuperación de contraseña y cierre de sesión. */
class UserRepository(
    private val database: EcoDatabase,
    private val settings: SettingsRepository
) {
    private val userDao = database.userDao()
    private val progressDao = database.progressDao()
    private val petDao = database.petDao()

    fun observeUser(userId: Long): Flow<User?> = userDao.observeById(userId).map { it?.toDomain() }

    suspend fun getUser(userId: Long): User? = userDao.findById(userId)?.toDomain()

    /**
     * Crea la cuenta con su progreso inicial (bono de bienvenida de 50 EcoPoints) y su mascota,
     * todo en una transacción, y deja la sesión iniciada.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        species: String,
        petName: String,
        now: Long = System.currentTimeMillis()
    ): RegisterResult {
        val normalizedEmail = normalize(email)
        if (userDao.findByEmail(normalizedEmail) != null) return RegisterResult.EmailTaken

        val salt = PasswordHasher.newSalt()
        val hash = hashPassword(password, salt)
        val userId = try {
            database.withTransaction {
                val id = userDao.insert(
                    UserEntity(
                        name = name.trim(),
                        email = normalizedEmail,
                        passwordHash = encode(hash),
                        passwordSalt = encode(salt),
                        createdAt = now
                    )
                )
                progressDao.upsert(
                    ProgressEntity(
                        userId = id,
                        balance = EcoRules.WELCOME_BONUS,
                        historicalPoints = EcoRules.WELCOME_BONUS
                    )
                )
                petDao.upsert(PetEntity(userId = id, species = species, name = petName.trim(), decayTimestamp = now))
                id
            }
        } catch (e: SQLiteConstraintException) {
            // Otro registro con el mismo correo ganó la carrera: el índice único lo impide.
            return RegisterResult.EmailTaken
        }
        settings.startSession(userId)
        return RegisterResult.Success(userId)
    }

    /** Solo entra la cuenta registrada con ese correo y su contraseña. */
    suspend fun login(email: String, password: String): LoginResult {
        val user = userDao.findByEmail(normalize(email)) ?: return LoginResult.NoAccount

        val hash = user.passwordHash
        val salt = user.passwordSalt
        if (hash != null && salt != null) {
            if (!passwordMatches(password, decode(salt), decode(hash))) return LoginResult.WrongPassword
        } else {
            // Cuentas creadas antes de guardar contraseñas: la primera que usen queda como suya.
            savePassword(user.id, password)
        }

        val isFirstTime = (progressDao.get(user.id)?.historicalPoints ?: 0) == 0
        if (isFirstTime) progressDao.earn(user.id, EcoRules.WELCOME_BONUS)

        settings.startSession(user.id)
        return LoginResult.Success(user.id, isFirstTime)
    }

    /**
     * Recuperar contraseña sin servidor de correo: se confirma la identidad con el nombre de
     * usuario y el correo registrados. Devuelve false si no coinciden.
     */
    suspend fun resetPassword(userName: String, email: String, newPassword: String): Boolean {
        val user = userDao.findByEmail(normalize(email)) ?: return false
        if (!user.name.trim().equals(userName.trim(), ignoreCase = true)) return false
        savePassword(user.id, newPassword)
        return true
    }

    suspend fun logout() = settings.endSession()

    private suspend fun savePassword(userId: Long, password: String) {
        val salt = PasswordHasher.newSalt()
        userDao.updatePassword(userId, encode(hashPassword(password, salt)), encode(salt))
    }

    // PBKDF2 es lento a propósito (10 000 iteraciones): se calcula fuera del hilo principal.
    private suspend fun hashPassword(password: String, salt: ByteArray): ByteArray =
        withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }

    private suspend fun passwordMatches(password: String, salt: ByteArray, expected: ByteArray): Boolean =
        withContext(Dispatchers.Default) { PasswordHasher.matches(password, salt, expected) }

    private fun normalize(email: String) = email.trim().lowercase()

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(text: String): ByteArray = Base64.decode(text, Base64.NO_WRAP)
}
