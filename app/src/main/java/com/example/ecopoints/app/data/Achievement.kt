package com.example.ecopoints.app.data

/** Datos del usuario con los que se calcula el progreso de los logros. */
data class AchievementStats(
    val completedChallenges: List<EcoChallenge>,
    val bestStreak: Int,
    val feedCount: Int,
    val playCount: Int,
    val historicalPoints: Int
) {
    val impact: EcoImpact get() = EcoImpact.total(completedChallenges)

    fun completedOfType(icon: String) = completedChallenges.count { it.icon == icon }
}

/**
 * Logro que se desbloquea al llegar a [target]. El progreso se calcula siempre a partir
 * de los datos guardados, así que no hace falta registrar cada acción por separado.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    private val progressOf: (AchievementStats) -> Int
) {
    fun progress(stats: AchievementStats): Int = progressOf(stats).coerceAtMost(target)

    fun isUnlocked(stats: AchievementStats): Boolean = progressOf(stats) >= target

    companion object {
        val ALL = listOf(
            Achievement("first_challenge", "Primer paso", "Cumple tu primer reto", 1) { it.completedChallenges.size },
            Achievement("five_challenges", "Constante", "Cumple 5 retos", 5) { it.completedChallenges.size },
            Achievement("ten_challenges", "Imparable", "Cumple 10 retos", 10) { it.completedChallenges.size },
            Achievement("recycler", "Reciclador", "Cumple 3 retos de reciclaje", 3) { it.completedOfType("recycle") },
            Achievement("cyclist", "Ciclista urbano", "Cumple 3 retos de movilidad", 3) { it.completedOfType("bike") },
            Achievement("streak_3", "En racha", "Entra 3 días seguidos", 3) { it.bestStreak },
            Achievement("streak_7", "Semana verde", "Entra 7 días seguidos", 7) { it.bestStreak },
            Achievement("feeder", "Buen cuidador", "Alimenta a tu mascota 10 veces", 10) { it.feedCount },
            Achievement("player", "Compañero de juegos", "Juega con tu mascota 10 veces", 10) { it.playCount },
            Achievement("points_200", "Ahorrador verde", "Gana 200 EcoPoints en total", 200) { it.historicalPoints },
            Achievement("level_3", "EcoHéroe", "Llega al nivel 3", 3) { EcoLevel.forPoints(it.historicalPoints).number },
            Achievement("co2_10", "Planeta agradecido", "Evita 10 kg de CO₂", 10) { it.impact.co2Kg.toInt() }
        )
    }
}
