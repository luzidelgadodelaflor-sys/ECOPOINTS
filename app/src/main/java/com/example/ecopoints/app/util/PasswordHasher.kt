package com.example.ecopoints.app.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * La contraseña nunca se guarda en texto plano: solo su hash PBKDF2 con una sal aleatoria.
 * Los parámetros son los mismos de la versión con SharedPreferences, así que las cuentas
 * ya creadas siguen entrando después de migrar a Room.
 */
object PasswordHasher {
    const val ITERATIONS = 10_000
    const val KEY_BITS = 256
    const val SALT_BYTES = 16

    fun newSalt(): ByteArray = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }

    fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
    }

    /** Comparación en tiempo constante. */
    fun matches(password: String, salt: ByteArray, expected: ByteArray): Boolean =
        MessageDigest.isEqual(expected, hash(password, salt))
}
