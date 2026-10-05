package com.example.ecopoints.app

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.PreferencesManager
import kotlin.math.ceil

/**
 * Avisa cuando se está acabando el plazo de los retos pendientes
 * (3 h antes en los de 1 día, 24 h antes en los más largos). Cada plazo se avisa una sola vez.
 */
class ChallengeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        if (prefs.isUserLoggedIn() && prefs.areNotificationsEnabled()) {
            val due = dueChallenges(prefs, System.currentTimeMillis())
            if (due.isNotEmpty()) {
                showNotification(context, due)
                prefs.markChallengesReminded(due.map { it.reminderKey })
            }
        }
        schedule(context)
    }

    companion object {
        private const val CHANNEL_ID = "challenge_reminders"
        private const val NOTIFICATION_ID = 1002
        private const val REQUEST_CODE = 2002

        /** Retos pendientes que ya entraron en su ventana de aviso y aún no se avisaron. */
        private fun dueChallenges(prefs: PreferencesManager, now: Long): List<EcoChallenge> {
            val reminded = prefs.getRemindedChallengeKeys()
            return pendingNotReminded(prefs, now, reminded)
                .filter { it.reminderMillis <= now }
                .sortedBy { it.deadlineMillis }
        }

        private fun pendingNotReminded(prefs: PreferencesManager, now: Long, reminded: Set<String>) =
            prefs.getChallenges().filter { !it.completed && now < it.deadlineMillis && it.reminderKey !in reminded }

        /** Programa la alarma para el próximo aviso pendiente (o la cancela si no hay ninguno). */
        fun schedule(context: Context) {
            val prefs = PreferencesManager(context)
            val nextAt = pendingNotReminded(prefs, System.currentTimeMillis(), prefs.getRemindedChallengeKeys())
                .minOfOrNull { it.reminderMillis }
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (nextAt == null || !prefs.isUserLoggedIn() || !prefs.areNotificationsEnabled()) {
                alarmManager.cancel(pendingIntent(context))
                return
            }
            // Alarma inexacta: no requiere permisos especiales. Si la hora ya pasó, se dispara enseguida.
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAt, pendingIntent(context))
        }

        private fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, ChallengeReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        private fun timeLeft(challenge: EcoChallenge, now: Long): String {
            val hours = ceil((challenge.deadlineMillis - now).toDouble() / EcoChallenge.HOUR_MILLIS).toInt()
            return if (hours <= 1) "en menos de 1 h" else "en $hours h"
        }

        @SuppressLint("MissingPermission") // Notifications.canPost() ya revisa el permiso
        private fun showNotification(context: Context, due: List<EcoChallenge>) {
            if (!Notifications.canPost(context)) return
            Notifications.ensureChannel(
                context,
                CHANNEL_ID,
                "Retos por vencer",
                "Avisos cuando se acaba el plazo de tus retos pendientes"
            )

            val now = System.currentTimeMillis()
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_leaf_logo)
                .setContentIntent(Notifications.openAppIntent(context))
                .setAutoCancel(true)

            if (due.size == 1) {
                val challenge = due.first()
                val text = "«${challenge.title}» vence ${timeLeft(challenge, now)}. " +
                    "Envía tu evidencia y gana +${challenge.points} EcoPoints."
                builder
                    .setContentTitle("¡Se acaba el tiempo de tu reto!")
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            } else {
                val totalPoints = due.sumOf { it.points }
                val style = NotificationCompat.InboxStyle()
                    .setSummaryText("Hasta +$totalPoints EcoPoints en juego")
                due.forEach { style.addLine("${it.title}: vence ${timeLeft(it, now)}") }
                builder
                    .setContentTitle("Tienes ${due.size} retos por vencer")
                    .setContentText(due.joinToString(", ") { it.title })
                    .setStyle(style)
            }

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }
}
