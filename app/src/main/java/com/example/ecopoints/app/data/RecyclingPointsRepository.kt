package com.example.ecopoints.app.data

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Punto de reciclaje real obtenido de OpenStreetMap. */
data class RecyclingPoint(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val materials: List<String>,
    val distanceMeters: Int
)

/**
 * Busca puntos de reciclaje cercanos en OpenStreetMap mediante la API pública de Overpass
 * (gratuita y sin clave). Los datos los aporta la comunidad de OpenStreetMap, así que en
 * algunas zonas puede haber pocos puntos registrados.
 */
object RecyclingPointsRepository {

    private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
    private const val MAX_RESULTS = 50
    private const val TIMEOUT_MS = 20_000

    /** Materiales de OpenStreetMap ("recycling:xxx=yes") traducidos al español. */
    private val MATERIAL_NAMES = linkedMapOf(
        "glass_bottles" to "Vidrio",
        "glass" to "Vidrio",
        "paper" to "Papel",
        "cardboard" to "Cartón",
        "plastic" to "Plástico",
        "plastic_bottles" to "Plástico",
        "plastic_packaging" to "Plástico",
        "cans" to "Latas",
        "batteries" to "Pilas",
        "clothes" to "Ropa",
        "shoes" to "Calzado",
        "electrical_items" to "Electrónicos",
        "small_appliances" to "Electrónicos",
        "organic" to "Orgánico",
        "green_waste" to "Orgánico",
        "cooking_oil" to "Aceite"
    )

    /** Lanza una excepción si no hay conexión o el servicio no responde. */
    suspend fun findNearby(latitude: Double, longitude: Double, radiusKm: Int): List<RecyclingPoint> =
        withContext(Dispatchers.IO) {
            val radiusMeters = radiusKm * 1_000
            val query = """
                [out:json][timeout:25];
                (
                  node["amenity"="recycling"](around:$radiusMeters,$latitude,$longitude);
                  way["amenity"="recycling"](around:$radiusMeters,$latitude,$longitude);
                );
                out center $MAX_RESULTS;
            """.trimIndent()

            val connection = (URL(OVERPASS_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doOutput = true
                // Overpass pide identificar la aplicación que hace la consulta
                setRequestProperty("User-Agent", "EcoPoints/1.0 (app educativa Android)")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
            try {
                connection.outputStream.use {
                    it.write("data=${URLEncoder.encode(query, "UTF-8")}".toByteArray())
                }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    error("Overpass respondió ${connection.responseCode}")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parse(body, latitude, longitude)
            } finally {
                connection.disconnect()
            }
        }

    private fun parse(body: String, fromLat: Double, fromLon: Double): List<RecyclingPoint> {
        val elements = JSONObject(body).getJSONArray("elements")
        return (0 until elements.length()).mapNotNull { i ->
            val element = elements.getJSONObject(i)
            // Los nodos traen lat/lon; las áreas (way) traen su centro en "center"
            val position = if (element.has("lat")) element else element.optJSONObject("center") ?: return@mapNotNull null
            val lat = position.getDouble("lat")
            val lon = position.getDouble("lon")
            val tags = element.optJSONObject("tags") ?: JSONObject()

            val materials = MATERIAL_NAMES.entries
                .filter { (key, _) -> tags.optString("recycling:$key") == "yes" }
                .map { it.value }
                .distinct()

            val isCentre = tags.optString("recycling_type") == "centre"
            val name = tags.optString("name").ifBlank {
                if (isCentre) "Centro de reciclaje" else "Contenedor de reciclaje"
            }

            val distance = FloatArray(1)
            Location.distanceBetween(fromLat, fromLon, lat, lon, distance)

            RecyclingPoint(
                id = element.getLong("id"),
                name = name,
                latitude = lat,
                longitude = lon,
                materials = materials,
                distanceMeters = distance[0].toInt()
            )
        }.sortedBy { it.distanceMeters }
    }
}
