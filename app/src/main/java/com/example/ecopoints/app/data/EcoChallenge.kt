package com.example.ecopoints.app.data

/**
 * Reto ecológico elegido por el usuario. El plazo (en días) lo define el usuario y
 * la recompensa depende de él: a más días de compromiso, más EcoPoints.
 */
data class EcoChallenge(
    val id: Long,
    val icon: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val startMillis: Long,
    val completed: Boolean = false,
    /** Momento en que se envió la evidencia (0 si aún no se cumple). */
    val completedMillis: Long = 0L,
    /** Ruta de la foto de evidencia dentro del almacenamiento privado de la app. */
    val evidencePath: String = "",
    /**
     * Vencimiento para el que ya se mostró el aviso "se acaba el tiempo" (0 si aún no se avisó).
     * Si el usuario edita el reto y cambia el plazo, el vencimiento cambia y vuelve a avisarse.
     */
    val remindedDeadline: Long = 0L
) {
    val points: Int get() = pointsForDuration(durationDays)
    val deadlineMillis: Long get() = startMillis + durationDays * DAY_MILLIS

    /** Un reto pendiente cuyo plazo terminó ya no se puede marcar (hay que editarlo para renovarlo). */
    fun isExpired(now: Long): Boolean = !completed && now > deadlineMillis

    /**
     * Momento del aviso "se acaba el tiempo": 3 horas antes en los retos de 1 día
     * y 24 horas antes en los más largos.
     */
    val reminderMillis: Long
        get() = deadlineMillis - if (durationDays <= 1) 3 * HOUR_MILLIS else DAY_MILLIS

    /** Ya se avisó de este plazo concreto. */
    val alreadyReminded: Boolean get() = remindedDeadline == deadlineMillis

    companion object {
        const val HOUR_MILLIS = 60L * 60 * 1000
        const val DAY_MILLIS = 24 * HOUR_MILLIS

        val DURATION_OPTIONS = listOf(1, 3, 7, 14)

        fun pointsForDuration(days: Int): Int = when {
            days <= 1 -> 10
            days <= 3 -> 25
            days <= 7 -> 50
            else -> 90
        }

        fun durationLabel(days: Int): String = if (days == 1) "1 día" else "$days días"
    }
}
