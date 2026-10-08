package com.example.ecopoints.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Ubicación del usuario con el GPS (Google Play Services). El permiso se pide en la pantalla. */
class LocationProvider(context: Context) {
    private val appContext = context.applicationContext

    /** Ubicación actual (o la última conocida); null si el GPS está apagado. */
    @SuppressLint("MissingPermission") // Se llama solo después de comprobar el permiso
    suspend fun currentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val client = LocationServices.getFusedLocationProviderClient(appContext)
        val cancellation = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    client.lastLocation
                        .addOnSuccessListener { continuation.resume(it) }
                        .addOnFailureListener { continuation.resume(null) }
                }
            }
            .addOnFailureListener { continuation.resume(null) }
        continuation.invokeOnCancellation { cancellation.cancel() }
    }
}
