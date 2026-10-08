package com.example.ecopoints.app.domain

import com.example.ecopoints.app.domain.EcoRules.DECAY_PERIOD_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Reglas de la mascota: desgaste con el tiempo (aunque la app esté cerrada) y aviso de hambre. */
class EcoRulesTest {

    private val start = 1_000_000L

    @Test
    fun decayed_restaSinBajarDelMinimo() {
        assertEquals(90, EcoRules.decayed(100, 10))
        assertEquals(EcoRules.PET_MIN_STAT, EcoRules.decayed(15, 10))
    }

    @Test
    fun decayed_unValorYaPorDebajoDelMinimoNoSube() {
        assertEquals(5, EcoRules.decayed(5, 10))
    }

    @Test
    fun applyDecay_primeraRevisionSoloIniciaElReloj() {
        val result = EcoRules.applyDecay(100, 100, lastTimestamp = 0L, now = start)
        assertEquals(EcoRules.DecayResult(100, 100, start), result)
    }

    @Test
    fun applyDecay_menosDeUnPeriodoNoCambiaNada() {
        val result = EcoRules.applyDecay(100, 100, start, start + DECAY_PERIOD_MILLIS - 1)
        assertEquals(EcoRules.DecayResult(100, 100, start), result)
    }

    @Test
    fun applyDecay_cuentaSoloPeriodosCompletos() {
        val result = EcoRules.applyDecay(100, 100, start, start + 3 * DECAY_PERIOD_MILLIS + 5_000)
        assertEquals(70, result.hunger)
        assertEquals(70, result.happiness)
        assertEquals(start + 3 * DECAY_PERIOD_MILLIS, result.timestamp)
    }

    @Test
    fun applyDecay_nuncaBajaDelMinimo() {
        val result = EcoRules.applyDecay(100, 100, start, start + 100 * DECAY_PERIOD_MILLIS)
        assertEquals(EcoRules.PET_MIN_STAT, result.hunger)
        assertEquals(EcoRules.PET_MIN_STAT, result.happiness)
    }

    @Test
    fun applyDecay_relojAtrasadoReiniciaElTiempo() {
        val result = EcoRules.applyDecay(80, 60, lastTimestamp = start, now = start - 10)
        assertEquals(EcoRules.DecayResult(80, 60, start - 10), result)
    }

    @Test
    fun nextLowHunger_yaEstaBajaNoProgramaAviso() {
        assertNull(EcoRules.nextLowHungerMillis(hunger = 29, decayTimestamp = start, now = start))
    }

    @Test
    fun nextLowHunger_justoEnElUmbralAvisaEnElSiguientePeriodo() {
        assertEquals(
            start + DECAY_PERIOD_MILLIS,
            EcoRules.nextLowHungerMillis(hunger = 30, decayTimestamp = start, now = start)
        )
    }

    @Test
    fun nextLowHunger_conEnergiaLlenaFaltan8Periodos() {
        // 100 -> 90 -> 80 -> 70 -> 60 -> 50 -> 40 -> 30 -> 20 (baja del umbral al 8.º periodo)
        assertEquals(
            start + 8 * DECAY_PERIOD_MILLIS,
            EcoRules.nextLowHungerMillis(hunger = 100, decayTimestamp = start, now = start + 1)
        )
    }

    @Test
    fun nextLowHunger_sinRelojUsaLaHoraActual() {
        val now = 5_000_000L
        assertEquals(now + DECAY_PERIOD_MILLIS, EcoRules.nextLowHungerMillis(30, decayTimestamp = 0L, now = now))
    }
}
