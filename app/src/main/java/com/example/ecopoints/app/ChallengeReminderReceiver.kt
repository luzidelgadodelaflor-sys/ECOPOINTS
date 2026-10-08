package com.example.ecopoints.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ecopoints.app.data.EcoChallenge
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Avisa cuando se está acabando el plazo de los retos pendientes
 * (3 h antes en los de 1 día, 24 h antes en los más largos). Cada plazo se avisa una sola vez.
 */
class ChallengeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as EcoPointsApp).container
        val pending = goAsync()
        container.appScope.launch {
            try {
                container.legacyMigrator.migrateIfNeeded()
                val userId = container.settingsRepository.sessionUserId.first()
                val config = container.settingsRepository.settings.first()
                if (userId != null && config.challengeRemindersEnabled) {
                    val due = container.challengeRepository.dueForReminder(userId, System.currentTimeMillis())
                    if (due.isNotEmpty()) {
                        showNotification(context, due)
                        container.challengeRepository.markReminded(userId, due)
                    }
                }
                container.reminderScheduler.scheduleChallengeReminder()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "challenge_reminders"
        private const val NOTIFICATION_ID = 1002

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
