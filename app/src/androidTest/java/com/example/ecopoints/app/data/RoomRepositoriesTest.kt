package com.example.ecopoints.app.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ecopoints.app.data.local.db.EcoDatabase
import com.example.ecopoints.app.data.local.db.PetEntity
import com.example.ecopoints.app.data.local.db.ProgressEntity
import com.example.ecopoints.app.data.local.db.UserEntity
import com.example.ecopoints.app.data.repository.ChallengeRepository
import com.example.ecopoints.app.data.repository.PetRepository
import com.example.ecopoints.app.data.repository.ProgressRepository
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.domain.PetAction
import com.example.ecopoints.app.domain.PetActionResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de la capa de datos (Sprint de persistencia local): Room en memoria, sin tocar la base
 * real de la app. Cubren las reglas que viven en los repositorios: puntos, mascota, retos y racha.
 */
@RunWith(AndroidJUnit4::class)
class RoomRepositoriesTest {

    private lateinit var db: EcoDatabase
    private lateinit var challenges: ChallengeRepository
    private lateinit var pets: PetRepository
    private lateinit var progress: ProgressRepository
    private var userId = 0L

    private val t0 = 1_700_000_000_000L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EcoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        challenges = ChallengeRepository(db)
        pets = PetRepository(db)
        progress = ProgressRepository(db)

        userId = db.userDao().insert(
            UserEntity(name = "Ana", email = "ana@correo.com", passwordHash = null, passwordSalt = null, createdAt = t0)
        )
        db.progressDao().upsert(ProgressEntity(userId = userId, balance = 50, historicalPoints = 50))
        db.petDao().upsert(PetEntity(userId = userId, species = "Koala", name = "Pepe", decayTimestamp = t0))
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---------- Usuarios ----------

    @Test
    fun elCorreoEsUnico() = runTest {
        try {
            db.userDao().insert(
                UserEntity(name = "Otra", email = "ana@correo.com", passwordHash = null, passwordSalt = null, createdAt = t0)
            )
            fail("Debió rechazar el correo repetido")
        } catch (expected: SQLiteConstraintException) {
            assertEquals(1, db.userDao().count())
        }
    }

    @Test
    fun alBorrarElUsuarioSeBorranSusDatos() = runTest {
        challenges.add(userId, "bike", "Ir en bici", "", 3, t0)
        db.openHelper.writableDatabase.execSQL("DELETE FROM users WHERE id = $userId")

        assertNull(db.progressDao().get(userId))
        assertNull(db.petDao().get(userId))
        assertTrue(challenges.getChallenges(userId).isEmpty())
    }

    // ---------- Mascota ----------

    @Test
    fun alimentarGastaPuntosYSubeLaEnergia() = runTest {
        db.petDao().updateStats(userId, hunger = 50, happiness = 100, decayTimestamp = t0)

        assertEquals(PetActionResult.Done, pets.perform(userId, PetAction.FEED))

        val pet = pets.getPet(userId)!!
        assertEquals(70, pet.hunger)
        val state = progress.observeProgress(userId).first()!!
        assertEquals(30, state.balance)
        assertEquals(50, state.historicalPoints) // gastar nunca baja el histórico (ni el nivel)
        assertEquals(1, state.feedCount)
    }

    @Test
    fun sinPuntosNoSePuedeAlimentar() = runTest {
        db.petDao().updateStats(userId, hunger = 50, happiness = 100, decayTimestamp = t0)
        db.progressDao().spend(userId, 40) // quedan 10

        assertEquals(PetActionResult.NotEnoughPoints, pets.perform(userId, PetAction.FEED))
        assertEquals(50, pets.getPet(userId)!!.hunger)
    }

    @Test
    fun unaMascotaLlenaNoGastaPuntos() = runTest {
        assertEquals(PetActionResult.AlreadyFull, pets.perform(userId, PetAction.FEED))
        assertEquals(50, progress.observeProgress(userId).first()!!.balance)
    }

    @Test
    fun jugarSubeLaFelicidadYBajaLaEnergia() = runTest {
        db.petDao().updateStats(userId, hunger = 80, happiness = 60, decayTimestamp = t0)

        assertEquals(PetActionResult.Done, pets.perform(userId, PetAction.PLAY))

        val pet = pets.getPet(userId)!!
        assertEquals(75, pet.happiness)
        assertEquals(70, pet.hunger)
    }

    @Test
    fun elDesgasteSeAplicaPorPeriodosCompletos() = runTest {
        pets.applyDecay(userId, now = t0 + 2 * EcoRules.DECAY_PERIOD_MILLIS + 1_000)

        val pet = pets.getPet(userId)!!
        assertEquals(80, pet.hunger)
        assertEquals(80, pet.happiness)
    }

    // ---------- Retos ----------

    @Test
    fun cumplirUnRetoSumaSusPuntosUnaSolaVez() = runTest {
        val id = challenges.add(userId, "bike", "Ir en bici", "", 3, t0)

        assertEquals(25, challenges.complete(userId, id, "/evidencia.jpg", now = t0 + 1_000))
        assertNull(challenges.complete(userId, id, "/evidencia.jpg", now = t0 + 2_000))

        val state = progress.observeProgress(userId).first()!!
        assertEquals(75, state.balance)
        assertEquals(75, state.historicalPoints)
    }

    @Test
    fun unRetoVencidoNoSePuedeCumplir() = runTest {
        val id = challenges.add(userId, "bike", "Ir en bici", "", 1, t0)
        val vencido = t0 + EcoChallenge.DAY_MILLIS + 1

        assertNull(challenges.complete(userId, id, "/evidencia.jpg", now = vencido))
    }

    @Test
    fun editarElPlazoReiniciaElConteoYElAviso() = runTest {
        val id = challenges.add(userId, "bike", "Ir en bici", "", 1, t0)
        challenges.markReminded(userId, challenges.getChallenges(userId))
        assertTrue(challenges.getChallenges(userId).first().alreadyReminded)

        challenges.update(userId, id, "bike", "Ir en bici", "", 7, now = t0 + 1_000)

        val reto = challenges.getChallenges(userId).first()
        assertEquals(t0 + 1_000, reto.startMillis)
        assertFalse(reto.alreadyReminded) // cambió el vencimiento: vuelve a avisar
    }

    @Test
    fun elAvisoLlegaEnLaVentanaYSoloUnaVez() = runTest {
        challenges.add(userId, "bike", "Ir en bici", "", 1, t0)
        val enVentana = t0 + EcoChallenge.DAY_MILLIS - 2 * EcoChallenge.HOUR_MILLIS

        assertTrue(challenges.dueForReminder(userId, t0).isEmpty())
        val due = challenges.dueForReminder(userId, enVentana)
        assertEquals(1, due.size)

        challenges.markReminded(userId, due)
        assertTrue(challenges.dueForReminder(userId, enVentana).isEmpty())
        assertNull(challenges.nextReminderMillis(userId, enVentana))
    }

    @Test
    fun losRetosSonDeCadaUsuario() = runTest {
        val otro = db.userDao().insert(
            UserEntity(name = "Luis", email = "luis@correo.com", passwordHash = null, passwordSalt = null, createdAt = t0)
        )
        challenges.add(userId, "bike", "Ir en bici", "", 3, t0)

        assertEquals(1, challenges.getChallenges(userId).size)
        assertTrue(challenges.getChallenges(otro).isEmpty())
    }

    // ---------- Progreso ----------

    @Test
    fun laPrimeraVisitaDelDiaDaBonoYRacha() = runTest {
        val visita = progress.registerDailyVisit(userId, today = "2026-10-08", yesterday = "2026-10-07", bonus = 12)

        assertNotNull(visita)
        assertEquals(1, visita!!.streak)
        assertEquals(12, visita.bonus)
        assertNull(progress.registerDailyVisit(userId, today = "2026-10-08", yesterday = "2026-10-07", bonus = 12))
        assertEquals(62, progress.observeProgress(userId).first()!!.balance)
    }

    @Test
    fun laRachaSeAlargaSiAyerTambienEntro() = runTest {
        progress.registerDailyVisit(userId, today = "2026-10-07", yesterday = "2026-10-06", bonus = 5)
        val hoy = progress.registerDailyVisit(userId, today = "2026-10-08", yesterday = "2026-10-07", bonus = 5)

        assertEquals(2, hoy!!.streak)
    }

    @Test
    fun laRachaVuelveA1SiSeSaltoUnDia() = runTest {
        progress.registerDailyVisit(userId, today = "2026-10-05", yesterday = "2026-10-04", bonus = 5)
        val hoy = progress.registerDailyVisit(userId, today = "2026-10-08", yesterday = "2026-10-07", bonus = 5)

        assertEquals(1, hoy!!.streak)
    }

    @Test
    fun cadaLogroSeAvisaUnaSolaVez() = runTest {
        val id = challenges.add(userId, "bike", "Ir en bici", "", 1, t0)
        challenges.complete(userId, id, "/evidencia.jpg", now = t0 + 1_000)

        val primeros = progress.checkNewAchievements(userId)
        assertTrue(primeros.any { it.id == "first_challenge" })
        assertTrue(progress.checkNewAchievements(userId).isEmpty())
    }
}
