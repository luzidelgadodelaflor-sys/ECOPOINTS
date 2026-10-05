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

        /** Nivel siguiente, o null si ya está en el máximo. */
        fun nextAfter(level: EcoLevel): EcoLevel? = LEVELS.firstOrNull { it.number == level.number + 1 }
    }
}

/**
 * Etapa de crecimiento de la mascota: crece con el nivel del usuario
 * (la imagen se agranda y en la última etapa gana un marco dorado).
 */
enum class PetStage(val label: String, val imageSizeDp: Int) {
    BABY("Bebé", 64),
    YOUNG("Joven", 80),
    ADULT("Adulta", 96),
    LEGENDARY("Legendaria", 100);

    companion object {
        fun forLevel(level: EcoLevel): PetStage = when (level.number) {
            1 -> BABY
            2 -> YOUNG
            3 -> ADULT
            else -> LEGENDARY
        }
    }
}
