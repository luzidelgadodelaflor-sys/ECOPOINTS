package com.example.ecopoints.app.data

import com.example.ecopoints.app.data.EcoChallenge.Companion.DAY_MILLIS
import com.example.ecopoints.app.data.EcoChallenge.Companion.HOUR_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Retos ecológicos: recompensa según el plazo, vencimiento y aviso "se acaba el tiempo". */
class EcoChallengeTest {

    private val start = 10_000_000L

    private fun challenge(days: Int, completed: Boolean = false, reminded: Long = 0L) = EcoChallenge(
        id = 1,
        icon = "bike",
        title = "Ir en bici",
        description = "",
        durationDays = days,
        startMillis = start,
        completed = completed,
        remindedDeadline = reminded
    )

    @Test
    fun masDiasDeCompromisoDanMasPuntos() {
        assertEquals(10, EcoChallenge.pointsForDuration(1))
        assertEquals(25, EcoChallenge.pointsForDuration(3))
        assertEquals(50, EcoChallenge.pointsForDuration(7))
        assertEquals(90, EcoChallenge.pointsForDuration(14))
    }

    @Test
    fun elPlazoTerminaDespuesDeLosDiasElegidos() {
        assertEquals(start + 3 * DAY_MILLIS, challenge(3).deadlineMillis)
    }

    @Test
    fun vencimiento() {
        val reto = challenge(1)
        assertFalse(reto.isExpired(start + DAY_MILLIS))
        assertTrue(reto.isExpired(start + DAY_MILLIS + 1))
    }

    @Test
    fun unRetoCumplidoNuncaVence() {
        assertFalse(challenge(1, completed = true).isExpired(start + 100 * DAY_MILLIS))
    }

    @Test
    fun avisoDeRetosDeUnDiaEs3HorasAntes() {
        val reto = challenge(1)
        assertEquals(reto.deadlineMillis - 3 * HOUR_MILLIS, reto.reminderMillis)
    }

    @Test
    fun avisoDeRetosLargosEs24HorasAntes() {
        val reto = challenge(7)
        assertEquals(reto.deadlineMillis - DAY_MILLIS, reto.reminderMillis)
    }

    @Test
    fun yaAvisadoSoloParaElPlazoActual() {
        val reto = challenge(3)
        assertFalse(reto.alreadyReminded)
        assertTrue(challenge(3, reminded = reto.deadlineMillis).alreadyReminded)
        // Si se edita el plazo cambia el vencimiento y vuelve a avisar
        assertFalse(challenge(7, reminded = reto.deadlineMillis).alreadyReminded)
    }

    @Test
    fun etiquetaDeDuracion() {
        assertEquals("1 día", EcoChallenge.durationLabel(1))
        assertEquals("7 días", EcoChallenge.durationLabel(7))
    }
}
