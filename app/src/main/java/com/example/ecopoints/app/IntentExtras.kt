package com.example.ecopoints.app

import android.util.Patterns

/** Claves de los extras que viajan en los Intents entre las actividades. */
object IntentExtras {
    const val USER_NAME = "EXTRA_USER_NAME"
    const val PET_LEVEL = "EXTRA_PET_LEVEL"
    const val SELECTED_PET = "EXTRA_SELECTED_PET"
    const val PET_NAME = "EXTRA_PET_NAME"
    const val IS_NEW_USER = "EXTRA_IS_NEW_USER"
    const val EMAIL = "EXTRA_EMAIL"
    const val LOGGED_OUT = "EXTRA_LOGGED_OUT"

    // Resultado que SettingsActivity devuelve a quien la abrió
    const val MAP_RADIUS_KM = "EXTRA_MAP_RADIUS_KM"
    const val RANKING_VISIBLE = "EXTRA_RANKING_VISIBLE"
    const val NOTIFICATIONS_ENABLED = "EXTRA_NOTIFICATIONS_ENABLED"
}

/** Validación básica del formato de un correo electrónico (RF-01). */
fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
