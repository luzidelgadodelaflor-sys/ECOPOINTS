package com.example.ecopoints.app.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** La contraseña nunca se guarda en texto plano: solo su hash PBKDF2 con sal. */
class PasswordHasherTest {

    @Test
    fun mismaContrasenaYSalGeneranElMismoHash() {
        val salt = PasswordHasher.newSalt()
        assertArrayEquals(PasswordHasher.hash("clave-segura", salt), PasswordHasher.hash("clave-segura", salt))
    }

    @Test
    fun elHashTiene256Bits() {
        assertEquals(PasswordHasher.KEY_BITS / 8, PasswordHasher.hash("clave-segura", PasswordHasher.newSalt()).size)
    }

    @Test
    fun saltsDistintasGeneranHashesDistintos() {
        val a = PasswordHasher.hash("clave-segura", PasswordHasher.newSalt())
        val b = PasswordHasher.hash("clave-segura", PasswordHasher.newSalt())
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun matchesAceptaLaCorrectaYRechazaLaIncorrecta() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("clave-segura", salt)
        assertTrue(PasswordHasher.matches("clave-segura", salt, hash))
        assertFalse(PasswordHasher.matches("otra-clave", salt, hash))
    }

    @Test
    fun laSalTieneElLargoEsperado() {
        assertEquals(PasswordHasher.SALT_BYTES, PasswordHasher.newSalt().size)
    }
}
