package com.example.ecopoints.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ecopoints.app.domain.EcoRules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Avisa con una notificación cuando la energía de la mascota baja del 30%.
 * MainActivity programa la alarma al salir de la app y la cancela al volver.
 */
class PetReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as EcoPointsApp).container
        val pending = goAsync()
        container.appScope.launch {
            try {
                container.legacyMigrator.migrateIfNeeded()
                val userId = container.settingsRepository.sessionUserId.first() ?: return@launch
                val config = container.settingsRepository.settings.first()
                if (!config.petRemindersEnabled) return@launch

                container.petRepository.applyDecay(userId)
                val pet = container.petRepository.getPet(userId) ?: return@launch
                if (pet.hunger >= EcoRules.PET_LOW_STAT) {
                    // Alguien la alimentó mientras tanto: se vuelve a calcular el aviso.
                    container.reminderScheduler.schedulePetReminder()
                } else {
                    showNotification(context, pet.displayName)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "pet_reminders"
        private const val NOTIFICATION_ID = 1001

        @SuppressLint("MissingPermission") // Notifications.canPost() ya revisa el permiso
        private fun showNotification(context: Context, petName: String) {
            if (!Notifications.canPost(context)) return
            Notifications.ensureChannel(
                context,
                CHANNEL_ID,
                "Cuidado de la mascota",
                "Avisos cuando tu mascota necesita comer"
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_leaf_logo)
                .setContentTitle("¡$petName tiene hambre!")
                .setContentText("Su energía bajó del ${EcoRules.PET_LOW_STAT}%. Entra a alimentarlo y no pierdas tu racha.")
                .setContentIntent(Notifications.openAppIntent(context))
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }
}
