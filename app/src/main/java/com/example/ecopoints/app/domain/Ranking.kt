package com.example.ecopoints.app.domain

/** Una fila del ranking de la comunidad. */
data class RankingEntry(val name: String, val points: Int, val isUser: Boolean = false)

/**
 * Ranking por EcoPoints históricos. La app todavía no tiene servidor, así que el usuario se
 * compara con una comunidad de muestra. Al conectar un backend se reemplaza esta lista.
 */
object RankingBoard {

    const val TOP_SIZE = 5

    val sampleCommunity = listOf(
        RankingEntry("EcoLuna", 420),
        RankingEntry("Verde_Mateo", 355),
        RankingEntry("RecicladorPro", 290),
        RankingEntry("SolarSofi", 240),
        RankingEntry("Hoja_Andina", 160),
        RankingEntry("BiciCarlos", 120),
        RankingEntry("AguaClara", 75)
    )

    /** Comunidad + usuario, de mayor a menor puntaje. */
    fun build(userName: String, historicalPoints: Int): List<RankingEntry> =
        (sampleCommunity + RankingEntry(userName, historicalPoints, isUser = true))
            .sortedByDescending { it.points }

    /** Puesto del usuario (1 = primero). */
    fun positionOf(ranking: List<RankingEntry>): Int = ranking.indexOfFirst { it.isUser } + 1
}
