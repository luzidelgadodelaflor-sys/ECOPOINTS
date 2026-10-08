package com.example.ecopoints.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Impacto ecológico estimado y logros: se calculan siempre a partir de los datos guardados. */
class EcoImpactAndAchievementTest {

    private fun done(icon: String, id: Long = 1) = EcoChallenge(
        id = id, icon = icon, title = "Reto", description = "", durationDays = 1,
        startMillis = 0L, completed = true, completedMillis = 1L
    )

    private fun pending(icon: String) = done(icon).copy(completed = false, completedMillis = 0L)

    private fun stats(
        challenges: List<EcoChallenge> = emptyList(),
        bestStreak: Int = 0,
        feed: Int = 0,
        play: Int = 0,
        points: Int = 0
    ) = AchievementStats(challenges, bestStreak, feed, play, points)

    private fun achievement(id: String) = Achievement.ALL.first { it.id == id }

    @Test
    fun soloCuentanLosRetosCumplidos() {
        val total = EcoImpact.total(listOf(done("bike"), pending("recycle")))
        assertEquals(2.0, total.co2Kg, 0.0001)
        assertEquals(0, total.plasticItems)
    }

    @Test
    fun elImpactoSeSuma() {
        val total = EcoImpact.total(listOf(done("recycle", 1), done("recycle", 2), done("water", 3)))
        assertEquals(0.6, total.co2Kg, 0.0001)
        assertEquals(6, total.plasticItems)
        assertEquals(20.0, total.waterLiters, 0.0001)
    }

    @Test
    fun sinRetosNoHayImpacto() {
        assertTrue(EcoImpact.total(emptyList()).isEmpty)
    }

    @Test
    fun retoPersonalizadoTieneImpactoSimbolico() {
        assertEquals(0.2, EcoImpact.forChallengeIcon("otro").co2Kg, 0.0001)
    }

    @Test
    fun primerPasoSeDesbloqueaConElPrimerReto() {
        assertFalse(achievement("first_challenge").isUnlocked(stats()))
        assertTrue(achievement("first_challenge").isUnlocked(stats(listOf(done("bike")))))
    }

    @Test
    fun elProgresoNoSuperaLaMeta() {
        val muchos = (1L..8L).map { done("bike", it) }
        assertEquals(5, achievement("five_challenges").progress(stats(muchos)))
    }

    @Test
    fun logrosDeRachaYCuidado() {
        assertTrue(achievement("streak_3").isUnlocked(stats(bestStreak = 3)))
        assertFalse(achievement("streak_7").isUnlocked(stats(bestStreak = 6)))
        assertTrue(achievement("feeder").isUnlocked(stats(feed = 10)))
        assertFalse(achievement("player").isUnlocked(stats(play = 9)))
    }

    @Test
    fun logroDeNivelUsaLosPuntosHistoricos() {
        assertFalse(achievement("level_3").isUnlocked(stats(points = 399)))
        assertTrue(achievement("level_3").isUnlocked(stats(points = 400)))
    }

    @Test
    fun logroPorTipoDeReto() {
        val tres = (1L..3L).map { done("recycle", it) }
        assertTrue(achievement("recycler").isUnlocked(stats(tres)))
        assertFalse(achievement("cyclist").isUnlocked(stats(tres)))
    }

    @Test
    fun logroDeCo2() {
        val cinco = (1L..5L).map { done("bike", it) } // 5 x 2 kg = 10 kg
        assertTrue(achievement("co2_10").isUnlocked(stats(cinco)))
    }
}
