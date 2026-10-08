package com.example.ecopoints.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.ecopoints.app.ChallengeReminderReceiver
import com.example.ecopoints.app.PetReminderReceiver
import com.example.ecopoints.app.data.repository.ChallengeRepository
import com.example.ecopoints.app.data.repository.PetRepository
import com.example.ecopoints.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

/**
 * Programa (y cancela) las alarmas de los avisos: mascota con hambre y retos por vencer.
 * Usa alarmas inexactas: no requieren permisos especiales y ahorran batería.
 */
class ReminderScheduler(
    private val context: Context,
    private val settings: SettingsRepository,
    private val pets: PetRepository,
    private val challenges: ChallengeRepository
) {
    private val alarmManager: AlarmManager get() = context.getSystemService(AlarmManager::class.java)

    /** Al salir de la app se programan los avisos; si no hay sesión se cancelan. */
    suspend fun scheduleAll() {
        if (settings.sessionUserId.first() == null) {
            cancelPetReminder()
            cancelChallengeReminder()
            return
        }
        schedulePetReminder()
        scheduleChallengeReminder()
    }

    /** Programa el aviso para cuando la energía baje del umbral (si aplica). */
    suspend fun schedulePetReminder() {
        val userId = settings.sessionUserId.first()
        val config = settings.settings.first()
        val triggerAt = if (userId != null && config.petRemindersEnabled) pets.nextLowHungerMillis(userId) else null
        if (triggerAt == null) {
            cancelPetReminder()
            return
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, petIntent())
    }

    fun cancelPetReminder() {
        alarmManager.cancel(petIntent())
    }

    /** Programa la alarma para el próximo aviso pendiente (o la cancela si no hay ninguno). */
    suspend fun scheduleChallengeReminder() {
        val userId = settings.sessionUserId.first()
        val config = settings.settings.first()
        val nextAt = if (userId != null && config.challengeRemindersEnabled) challenges.nextReminderMillis(userId) else null
        if (nextAt == null) {
            cancelChallengeReminder()
            return
        }
        // Si la hora ya pasó, la alarma se dispara enseguida.
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextAt, challengeIntent())
    }

    fun cancelChallengeReminder() {
        alarmManager.cancel(challengeIntent())
    }

    private fun petIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        PET_REQUEST_CODE,
        Intent(context, PetReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun challengeIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        CHALLENGE_REQUEST_CODE,
        Intent(context, ChallengeReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private companion object {
        const val PET_REQUEST_CODE = 2001
        const val CHALLENGE_REQUEST_CODE = 2002
    }
}
