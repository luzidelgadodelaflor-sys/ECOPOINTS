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
    val completed: Boolean = false
) {
    val points: Int get() = pointsForDuration(durationDays)
    val deadlineMillis: Long get() = startMillis + durationDays * DAY_MILLIS

    /** Un reto pendiente cuyo plazo terminó ya no se puede marcar (hay que editarlo para renovarlo). */
    fun isExpired(now: Long): Boolean = !completed && now > deadlineMillis

    companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000

        val DURATION_OPTIONS = listOf(1, 3, 7, 14)

        fun pointsForDuration(days: Int): Int = when {
            days <= 1 -> 10
            days <= 3 -> 25
            days <= 7 -> 50
            else -> 90
        }
    }
}
