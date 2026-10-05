package com.example.ecopoints.app

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ecopoints.app.data.PreferencesManager

/**
 * Avisa con una notificación cuando la energía de la mascota baja del 30%.
 * HomeActivity programa la alarma al salir de la pantalla y la cancela al volver.
 */
class PetReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        if (!prefs.isUserLoggedIn() || !prefs.arePetRemindersEnabled()) return

        prefs.applyPetDecay()
        if (prefs.getPetHunger() >= PreferencesManager.PET_LOW_STAT) {
            // Alguien la alimentó mientras tanto: se vuelve a calcular el aviso.
            schedule(context)
            return
        }
        showNotification(context, petDisplayName(prefs.getPetLevel()))
    }

    companion object {
        private const val CHANNEL_ID = "pet_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 2001

        /** Programa el aviso para cuando la energía baje del umbral (si aplica). */
        fun schedule(context: Context) {
            val prefs = PreferencesManager(context)
            val triggerAt = prefs.nextLowHungerMillis()
            if (triggerAt == null || !prefs.arePetRemindersEnabled()) {
                cancel(context)
                return
            }
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            // Alarma inexacta: no requiere permisos especiales y ahorra batería.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
        }

        fun cancel(context: Context) {
            context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
        }

        private fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, PetReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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
                .setContentText("Su energía bajó del ${PreferencesManager.PET_LOW_STAT}%. Entra a alimentarlo y no pierdas tu racha.")
                .setContentIntent(Notifications.openAppIntent(context))
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }
}
