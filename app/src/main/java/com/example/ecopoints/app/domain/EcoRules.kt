package com.example.ecopoints.app.domain

/**
 * Reglas de negocio de EcoPoints en un solo lugar (funciones puras, sin Android),
 * para poder probarlas con tests unitarios y reutilizarlas en repositorios y receivers.
 */
object EcoRules {

    /** Bono de bienvenida al crear la cuenta. */
    const val WELCOME_BONUS = 50

    const val STAT_MAX = 100

    // Costos y efectos de cuidar a la mascota
    const val FEED_COST = 20
    const val PLAY_COST = 25
    const val FEED_HUNGER_GAIN = 20
    const val PLAY_HAPPINESS_GAIN = 15
    const val PLAY_HUNGER_LOSS = 10

    // Desgaste: cada 30 minutos la mascota pierde 10% de energía y 10% de felicidad,
    // pero nunca baja del mínimo (se ve triste, no "muerta").
    const val DECAY_PERIOD_MILLIS = 30L * 60 * 1000
    const val HUNGER_LOSS_PER_PERIOD = 10
    const val HAPPINESS_LOSS_PER_PERIOD = 10
    const val PET_MIN_STAT = 10

    /** Por debajo de este valor la mascota se ve triste y se avisa con una notificación. */
    const val PET_LOW_STAT = 30

    // Bono diario al azar por volver a la app
    const val DAILY_BONUS_MIN = 5
    const val DAILY_BONUS_MAX = 20

    /** Radios de búsqueda del EcoMapa que se pueden elegir en Ajustes. */
    val MAP_RADIUS_OPTIONS_KM = listOf(1, 3, 5)

    /** Valores de la mascota después de aplicar el desgaste. */
    data class DecayResult(val hunger: Int, val happiness: Int, val timestamp: Long)

    /** Resta el desgaste sin bajar del mínimo (y sin subir un valor que ya estaba por debajo). */
    fun decayed(value: Int, loss: Int): Int = minOf(value, (value - loss).coerceAtLeast(PET_MIN_STAT))

    /**
     * Desgaste de la mascota: por cada periodo de 30 minutos completo desde la última revisión
     * pierde energía y felicidad, aunque la app esté cerrada. El resto del tiempo se conserva
     * para la siguiente revisión. Si [lastTimestamp] es 0 (primera revisión) solo se inicia el reloj.
     */
    fun applyDecay(hunger: Int, happiness: Int, lastTimestamp: Long, now: Long): DecayResult {
        if (lastTimestamp == 0L || now < lastTimestamp) return DecayResult(hunger, happiness, now)
        val periods = ((now - lastTimestamp) / DECAY_PERIOD_MILLIS).toInt()
        if (periods == 0) return DecayResult(hunger, happiness, lastTimestamp)
        return DecayResult(
            hunger = decayed(hunger, periods * HUNGER_LOSS_PER_PERIOD),
            happiness = decayed(happiness, periods * HAPPINESS_LOSS_PER_PERIOD),
            timestamp = lastTimestamp + periods * DECAY_PERIOD_MILLIS
        )
    }

    /**
     * Momento en que la energía bajará de [PET_LOW_STAT] si nadie alimenta a la mascota,
     * o null si ya está baja (no hace falta programar otro aviso).
     */
    fun nextLowHungerMillis(hunger: Int, decayTimestamp: Long, now: Long): Long? {
        if (hunger < PET_LOW_STAT) return null
        val periods = (hunger - PET_LOW_STAT) / HUNGER_LOSS_PER_PERIOD + 1
        val last = if (decayTimestamp == 0L) now else decayTimestamp
        return last + periods * DECAY_PERIOD_MILLIS
    }
}
