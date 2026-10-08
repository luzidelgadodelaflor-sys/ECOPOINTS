package com.example.ecopoints.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    /** Fecha "yyyy-MM-dd" de hoy desplazada [offsetDays] días (para la racha diaria). */
    fun dayKey(offsetDays: Int = 0, now: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    /** Fecha y hora legible para el historial de retos. */
    fun formatDateTime(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
}
