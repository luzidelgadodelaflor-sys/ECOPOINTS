package com.example.ecopoints.app.data.model

/** Tema elegido en Ajustes. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Preferencias de la app guardadas en DataStore. Los valores por defecto son los de la primera ejecución. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val largeText: Boolean = false,
    /** Avisos de retos por vencer. */
    val challengeRemindersEnabled: Boolean = true,
    /** Aviso "tu mascota tiene hambre". */
    val petRemindersEnabled: Boolean = true,
    /** Visibilidad del usuario en el ranking de la comunidad. */
    val rankingVisible: Boolean = true,
    /** Pedir confirmación antes de gastar EcoPoints en la mascota. */
    val confirmSpend: Boolean = false,
    val vibrationEnabled: Boolean = true,
    /** Radio (en km) en el que EcoMapa busca puntos de reciclaje. */
    val mapRadiusKm: Int = 1
)

data class User(val id: Long, val name: String, val email: String)

/** Saldo, puntos históricos (definen nivel y ranking), racha y contadores de cuidado. */
data class UserProgress(
    val balance: Int = 0,
    val historicalPoints: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val feedCount: Int = 0,
    val playCount: Int = 0
)

/** Resultado de la primera visita del día: racha actual y bono ganado. */
data class DailyVisit(val streak: Int, val bonus: Int)

/** Mascota del usuario: especie elegida, nombre opcional e indicadores vitales (0-100). */
data class PetState(
    val species: String,
    val name: String,
    val hunger: Int,
    val happiness: Int
) {
    /** Nombre que el usuario le puso, o la especie si no le puso nombre. */
    val displayName: String get() = name.ifBlank { species }
}

object PetSpecies {
    const val DEFAULT = "Koala"
    val ALL = listOf("Zorro", "Panda", "Koala", "Gato", "Búho")

    /** Reconoce la especie en textos guardados por versiones anteriores ("Koala (Pepe)"). */
    fun fromText(text: String): String = when {
        text.contains("Panda", ignoreCase = true) -> "Panda"
        text.contains("Zorro", ignoreCase = true) -> "Zorro"
        text.contains("Gato", ignoreCase = true) -> "Gato"
        text.contains("Búho", ignoreCase = true) || text.contains("Buho", ignoreCase = true) -> "Búho"
        else -> DEFAULT
    }
}

sealed interface LoginResult {
    /** [isFirstTime] es true si la cuenta no tenía puntos y recibió el bono de bienvenida. */
    data class Success(val userId: Long, val isFirstTime: Boolean) : LoginResult
    data object NoAccount : LoginResult
    data object WrongPassword : LoginResult
}

sealed interface RegisterResult {
    data class Success(val userId: Long) : RegisterResult
    data object EmailTaken : RegisterResult
}
