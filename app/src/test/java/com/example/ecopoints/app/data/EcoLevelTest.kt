package com.example.ecopoints.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** El nivel depende de los EcoPoints históricos; gastar puntos en la mascota nunca lo baja. */
class EcoLevelTest {

    @Test
    fun nivelesPorPuntosHistoricos() {
        assertEquals(1, EcoLevel.forPoints(0).number)
        assertEquals(1, EcoLevel.forPoints(149).number)
        assertEquals(2, EcoLevel.forPoints(150).number)
        assertEquals(2, EcoLevel.forPoints(399).number)
        assertEquals(3, EcoLevel.forPoints(400).number)
        assertEquals(4, EcoLevel.forPoints(800).number)
        assertEquals(4, EcoLevel.forPoints(50_000).number)
    }

    @Test
    fun siguienteNivel() {
        assertEquals(2, EcoLevel.nextAfter(EcoLevel.forPoints(0))?.number)
        assertNull(EcoLevel.nextAfter(EcoLevel.forPoints(800)))
    }

    @Test
    fun etapaDeLaMascotaCreceConElNivel() {
        assertEquals(PetStage.BABY, PetStage.forLevel(EcoLevel.forPoints(0)))
        assertEquals(PetStage.YOUNG, PetStage.forLevel(EcoLevel.forPoints(150)))
        assertEquals(PetStage.ADULT, PetStage.forLevel(EcoLevel.forPoints(400)))
        assertEquals(PetStage.LEGENDARY, PetStage.forLevel(EcoLevel.forPoints(800)))
    }
}
