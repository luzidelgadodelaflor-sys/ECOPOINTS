package com.example.ecopoints.app.util

/**
 * Validaciones de los formularios (RF-01, RF-02). Cada función devuelve el mensaje de error
 * para mostrar al usuario, o null si los datos son válidos. Son puras y se prueban con tests unitarios.
 */
object Validators {

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 40
    const val MAX_PET_NAME_LENGTH = 20
    const val MAX_CHALLENGE_TITLE_LENGTH = 40

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** Validación básica del formato de un correo electrónico. */
    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun loginError(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Completa correo y contraseña"
        !isValidEmail(email) -> "Ingresa un correo electrónico válido"
        password.length < MIN_PASSWORD_LENGTH -> "Mínimo 8 caracteres en contraseña"
        else -> null
    }

    fun registerError(
        name: String,
        petName: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): String? = when {
        name.isBlank() || email.isBlank() || password.isBlank() ->
            "Completa todos los campos principales"
        name.trim().length < MIN_NAME_LENGTH -> "El nombre debe tener al menos 2 caracteres"
        name.trim().length > MAX_NAME_LENGTH -> "El nombre es demasiado largo (máximo 40)"
        petName.trim().length > MAX_PET_NAME_LENGTH -> "El nombre de la mascota es demasiado largo (máximo 20)"
        !isValidEmail(email) -> "Ingresa un correo electrónico válido"
        password.length < MIN_PASSWORD_LENGTH -> "Mínimo 8 caracteres en contraseña"
        password != confirmPassword -> "Las contraseñas no coinciden"
        !termsAccepted -> "Acepta los términos y condiciones"
        else -> null
    }

    fun resetPasswordError(
        userName: String,
        email: String,
        newPassword: String,
        confirmPassword: String
    ): String? = when {
        userName.isBlank() || email.isBlank() || newPassword.isBlank() -> "Completa todos los campos"
        !isValidEmail(email) -> "Ingresa un correo electrónico válido"
        newPassword.length < MIN_PASSWORD_LENGTH -> "Mínimo 8 caracteres en contraseña"
        newPassword != confirmPassword -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Nombre del reto: obligatorio y con largo razonable. */
    fun challengeTitleError(title: String): String? = when {
        title.isBlank() -> "Escribe un nombre para tu reto"
        title.trim().length > MAX_CHALLENGE_TITLE_LENGTH -> "El nombre es demasiado largo (máximo 40)"
        else -> null
    }
}
