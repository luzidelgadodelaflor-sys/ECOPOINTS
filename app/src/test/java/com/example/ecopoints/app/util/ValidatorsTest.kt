package com.example.ecopoints.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validaciones de los formularios (RF-01, RF-02): reglas puras, sin Android. */
class ValidatorsTest {

    @Test
    fun emailValido() {
        assertTrue(Validators.isValidEmail("ana@correo.com"))
        assertTrue(Validators.isValidEmail("  ana.perez+eco@mail.upn.edu.pe  "))
    }

    @Test
    fun emailInvalido() {
        assertFalse(Validators.isValidEmail("ana"))
        assertFalse(Validators.isValidEmail("ana@"))
        assertFalse(Validators.isValidEmail("ana@correo"))
        assertFalse(Validators.isValidEmail("@correo.com"))
    }

    @Test
    fun login_camposVacios() {
        assertEquals("Completa correo y contraseña", Validators.loginError("", "12345678"))
        assertEquals("Completa correo y contraseña", Validators.loginError("ana@correo.com", ""))
    }

    @Test
    fun login_emailYContrasena() {
        assertEquals("Ingresa un correo electrónico válido", Validators.loginError("ana", "12345678"))
        assertEquals("Mínimo 8 caracteres en contraseña", Validators.loginError("ana@correo.com", "1234567"))
        assertNull(Validators.loginError("ana@correo.com", "12345678"))
    }

    @Test
    fun registro_datosCorrectos() {
        assertNull(Validators.registerError("Ana", "Pepe", "ana@correo.com", "12345678", "12345678", true))
    }

    @Test
    fun registro_errores() {
        assertEquals(
            "Completa todos los campos principales",
            Validators.registerError("", "", "ana@correo.com", "12345678", "12345678", true)
        )
        assertEquals(
            "El nombre debe tener al menos 2 caracteres",
            Validators.registerError("A", "", "ana@correo.com", "12345678", "12345678", true)
        )
        assertEquals(
            "Las contraseñas no coinciden",
            Validators.registerError("Ana", "", "ana@correo.com", "12345678", "87654321", true)
        )
        assertEquals(
            "Acepta los términos y condiciones",
            Validators.registerError("Ana", "", "ana@correo.com", "12345678", "12345678", false)
        )
    }

    @Test
    fun registro_nombreDeMascotaMuyLargo() {
        val largo = "x".repeat(Validators.MAX_PET_NAME_LENGTH + 1)
        assertEquals(
            "El nombre de la mascota es demasiado largo (máximo 20)",
            Validators.registerError("Ana", largo, "ana@correo.com", "12345678", "12345678", true)
        )
    }

    @Test
    fun recuperarContrasena() {
        assertEquals("Completa todos los campos", Validators.resetPasswordError("", "ana@correo.com", "12345678", "12345678"))
        assertEquals("Las contraseñas no coinciden", Validators.resetPasswordError("Ana", "ana@correo.com", "12345678", "1"))
        assertNull(Validators.resetPasswordError("Ana", "ana@correo.com", "12345678", "12345678"))
    }

    @Test
    fun tituloDeReto() {
        assertEquals("Escribe un nombre para tu reto", Validators.challengeTitleError("   "))
        assertEquals(
            "El nombre es demasiado largo (máximo 40)",
            Validators.challengeTitleError("x".repeat(Validators.MAX_CHALLENGE_TITLE_LENGTH + 1))
        )
        assertNull(Validators.challengeTitleError("Ir en bici al trabajo"))
    }
}
