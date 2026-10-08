package com.example.ecopoints.app.domain

/** Acciones con la mascota y su costo en EcoPoints. */
enum class PetAction(val cost: Int, val label: String) {
    FEED(cost = EcoRules.FEED_COST, label = "Alimentar"),
    PLAY(cost = EcoRules.PLAY_COST, label = "Jugar")
}

/** Resultado de intentar alimentar o jugar con la mascota. */
sealed interface PetActionResult {
    /** La acción se hizo y se descontaron los puntos. */
    data object Done : PetActionResult

    /** El indicador ya estaba al máximo (no tiene sentido gastar puntos). */
    data object AlreadyFull : PetActionResult

    data object NotEnoughPoints : PetActionResult

    /** No hay mascota para ese usuario (sesión inválida). */
    data object NotFound : PetActionResult
}
