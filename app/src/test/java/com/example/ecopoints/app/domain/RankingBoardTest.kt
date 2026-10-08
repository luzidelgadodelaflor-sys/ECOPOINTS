package com.example.ecopoints.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingBoardTest {

    @Test
    fun elRankingIncluyeALaComunidadMasAlUsuario() {
        val ranking = RankingBoard.build("Ana", 100)
        assertEquals(RankingBoard.sampleCommunity.size + 1, ranking.size)
        assertEquals(1, ranking.count { it.isUser })
    }

    @Test
    fun estaOrdenadoDeMayorAMenor() {
        val points = RankingBoard.build("Ana", 300).map { it.points }
        assertEquals(points.sortedDescending(), points)
    }

    @Test
    fun usuarioConMuchosPuntosQuedaPrimero() {
        val ranking = RankingBoard.build("Ana", 1_000)
        assertEquals(1, RankingBoard.positionOf(ranking))
        assertTrue(ranking.first().isUser)
    }

    @Test
    fun usuarioNuevoQuedaUltimo() {
        val ranking = RankingBoard.build("Ana", 0)
        assertEquals(ranking.size, RankingBoard.positionOf(ranking))
    }
}
