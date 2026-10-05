package com.example.ecopoints.app.data

/**
 * Impacto ambiental aproximado de los retos cumplidos. Son estimaciones de referencia
 * por reto cumplido (no mediciones), pensadas para que el usuario vea el efecto de sus acciones.
 */
data class EcoImpact(
    val co2Kg: Double = 0.0,
    val waterLiters: Double = 0.0,
    val plasticItems: Int = 0
) {
    operator fun plus(other: EcoImpact) = EcoImpact(
        co2Kg + other.co2Kg,
        waterLiters + other.waterLiters,
        plasticItems + other.plasticItems
    )

    val isEmpty: Boolean get() = co2Kg == 0.0 && waterLiters == 0.0 && plasticItems == 0

    companion object {
        /** Emisión media de un auto: ~0,2 kg de CO₂ por km (sirve para la equivalencia "km sin auto"). */
        const val CAR_CO2_KG_PER_KM = 0.2

        /** Impacto estimado de un reto según su ícono (tipo de reto). */
        fun forChallengeIcon(icon: String): EcoImpact = when (icon) {
            // ~10 km en bicicleta o a pie en lugar de auto
            "bike" -> EcoImpact(co2Kg = 2.0)
            // 3 envases reciclados
            "recycle" -> EcoImpact(co2Kg = 0.3, plasticItems = 3)
            // bolsas plásticas evitadas en compras
            "bag" -> EcoImpact(co2Kg = 0.1, plasticItems = 5)
            // ~1 kWh ahorrado
            "light" -> EcoImpact(co2Kg = 0.4)
            // llave cerrada al cepillarse: ~6 L por vez, varias veces
            "water" -> EcoImpact(waterLiters = 20.0)
            // una planta cuidada o sembrada
            "tree" -> EcoImpact(co2Kg = 1.0)
            // retos personalizados: impacto simbólico
            else -> EcoImpact(co2Kg = 0.2)
        }

        fun total(challenges: List<EcoChallenge>): EcoImpact =
            challenges.filter { it.completed }
                .fold(EcoImpact()) { acc, challenge -> acc + forChallengeIcon(challenge.icon) }
    }
}
