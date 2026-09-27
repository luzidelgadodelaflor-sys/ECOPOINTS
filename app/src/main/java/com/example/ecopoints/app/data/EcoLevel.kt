package com.example.ecopoints.app.data

/**
 * Nivel del usuario según sus EcoPoints históricos (los que ha ganado en total;
 * gastar puntos en la mascota nunca lo baja).
 */
data class EcoLevel(val number: Int, val badge: String, val minPoints: Int) {
    companion object {
        private val LEVELS = listOf(
            EcoLevel(1, "Amigo de la Naturaleza", 0),
            EcoLevel(2, "Guardián Verde", 150),
            EcoLevel(3, "EcoHéroe", 400),
            EcoLevel(4, "EcoLeyenda", 800)
        )

        fun forPoints(historicalPoints: Int): EcoLevel =
            LEVELS.last { historicalPoints >= it.minPoints }
    }
}
