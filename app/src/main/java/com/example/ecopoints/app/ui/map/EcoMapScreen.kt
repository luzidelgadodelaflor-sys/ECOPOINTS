package com.example.ecopoints.app.ui.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecopoints.app.R
import com.example.ecopoints.app.data.RecyclingPoint
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoBorder
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary
import com.example.ecopoints.app.ui.theme.EcoTextSecondary
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun hasLocationPermission(context: Context) = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

private fun distanceLabel(meters: Int): String =
    if (meters < 1_000) "a $meters m" else "a %.1f km".format(meters / 1_000.0)

/** Zoom del mapa para que se vea todo el radio de búsqueda. */
private fun zoomFor(radiusKm: Int): Double = when (radiusKm) {
    1 -> 15.0
    3 -> 13.5
    else -> 12.8
}

/**
 * EcoMapa (US-08): mapa de OpenStreetMap con la ubicación del usuario (GPS) y los puntos
 * de reciclaje reales que hay dentro del radio elegido. La búsqueda la hace [MapViewModel].
 */
@Composable
fun EcoMapScreen(viewModel: MapViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val radiusKm = state.radiusKm

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.load(hasLocationPermission = result.values.any { it })
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission(context)) permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    // Se busca al abrir (cuando ya se conoce el radio) y cada vez que el radio cambia
    LaunchedEffect(radiusKm) {
        if (radiusKm != null) viewModel.load(hasLocationPermission(context))
    }

    val userLocation = remember(state.userLocation) {
        state.userLocation?.let { GeoPoint(it.latitude, it.longitude) }
    }
    val status = state.status
    val points = (status as? MapStatus.Loaded)?.points.orEmpty()
    val shownRadius = radiusKm ?: EcoRules.MAP_RADIUS_OPTIONS_KM.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoBackground)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = EcoGreenDark)
            }
            Column {
                Text("EcoMapa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                Text("Puntos de reciclaje reales cerca de ti", fontSize = 12.sp, color = EcoTextMuted)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Radio:", fontSize = 13.sp, color = EcoTextSecondary)
            EcoRules.MAP_RADIUS_OPTIONS_KM.forEach { km ->
                FilterChip(
                    selected = shownRadius == km,
                    onClick = { viewModel.setRadius(km) },
                    label = { Text("$km km") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, EcoBorder), RoundedCornerShape(16.dp))
        ) {
            OsmMap(
                userLocation = userLocation,
                radiusKm = shownRadius,
                points = points,
                selected = state.selected,
                onSelect = viewModel::select,
                modifier = Modifier.fillMaxSize()
            )
            // Atribución obligatoria de la licencia de OpenStreetMap
            Text(
                "© colaboradores de OpenStreetMap",
                fontSize = 9.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (status) {
            MapStatus.Loading -> StatusMessage(
                icon = null,
                text = "Buscando puntos de reciclaje cerca de ti…"
            )
            MapStatus.NeedsPermission -> StatusMessage(
                icon = Icons.Filled.LocationOff,
                text = "Para mostrarte los puntos de reciclaje cercanos, EcoPoints necesita tu ubicación.",
                actionLabel = "Permitir ubicación",
                onAction = { permissionLauncher.launch(LOCATION_PERMISSIONS) },
                secondaryLabel = "Abrir ajustes del celular",
                onSecondary = {
                    // Si el usuario marcó "no volver a preguntar", el permiso solo se da desde Ajustes
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                }
            )
            MapStatus.NoLocation -> StatusMessage(
                icon = Icons.Filled.LocationOff,
                text = "No pudimos obtener tu ubicación. Activa el GPS y vuelve a intentarlo.",
                actionLabel = "Reintentar",
                showRefreshIcon = true,
                onAction = { viewModel.load(hasLocationPermission(context)) }
            )
            MapStatus.NetworkError -> StatusMessage(
                icon = Icons.Filled.WifiOff,
                text = "No se pudieron cargar los puntos de reciclaje. Revisa tu conexión a internet.",
                actionLabel = "Reintentar",
                showRefreshIcon = true,
                onAction = { viewModel.load(hasLocationPermission(context)) }
            )
            is MapStatus.Loaded -> if (status.points.isEmpty()) {
                StatusMessage(
                    icon = Icons.Filled.Map,
                    text = "No hay puntos de reciclaje registrados en OpenStreetMap a menos de $shownRadius km. Prueba con un radio mayor.",
                    actionLabel = "Buscar de nuevo",
                    showRefreshIcon = true,
                    onAction = { viewModel.load(hasLocationPermission(context)) }
                )
            } else {
                PointsList(
                    points = status.points,
                    selected = state.selected,
                    onSelect = viewModel::select,
                    onDirections = { point ->
                        // Abre la app de mapas del celular para llegar al punto
                        val uri = Uri.parse("geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(${Uri.encode(point.name)})")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OsmMap(
    userLocation: GeoPoint?,
    radiusKm: Int,
    points: List<RecyclingPoint>,
    selected: RecyclingPoint?,
    onSelect: (RecyclingPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoomFor(radiusKm))
        }
    }
    val pointIcon = remember { ContextCompat.getDrawable(context, R.drawable.ic_map_point) }
    val userIcon = remember { ContextCompat.getDrawable(context, R.drawable.ic_map_user) }

    // El mapa sigue el ciclo de vida de la pantalla (pausa la descarga de mapas en segundo plano)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(userLocation, radiusKm) {
        userLocation?.let {
            mapView.controller.setZoom(zoomFor(radiusKm))
            mapView.controller.setCenter(it)
        }
    }
    LaunchedEffect(selected) {
        selected?.let { mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude)) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            if (userLocation != null) {
                // Círculo con el radio de búsqueda
                map.overlays.add(Polygon(map).apply {
                    setPoints(Polygon.pointsAsCircle(userLocation, radiusKm * 1_000.0))
                    fillPaint.color = 0x222E7D32
                    outlinePaint.color = 0x992E7D32.toInt()
                    outlinePaint.strokeWidth = 3f
                    infoWindow = null
                })
            }
            points.forEach { point ->
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(point.latitude, point.longitude)
                    icon = pointIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = point.name
                    snippet = point.materials.joinToString(", ").ifBlank { "Materiales no especificados" }
                    subDescription = distanceLabel(point.distanceMeters)
                    setOnMarkerClickListener { marker, _ ->
                        onSelect(point)
                        marker.showInfoWindow()
                        true
                    }
                    if (point == selected) showInfoWindow()
                })
            }
            if (userLocation != null) {
                map.overlays.add(Marker(map).apply {
                    position = userLocation
                    icon = userIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "Estás aquí"
                })
            }
            map.invalidate()
        }
    )
}

@Composable
private fun PointsList(
    points: List<RecyclingPoint>,
    selected: RecyclingPoint?,
    onSelect: (RecyclingPoint) -> Unit,
    onDirections: (RecyclingPoint) -> Unit
) {
    Text(
        if (points.size == 1) "1 punto de reciclaje encontrado" else "${points.size} puntos de reciclaje encontrados",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = EcoGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(points, key = { it.id }) { point ->
            val isSelected = point == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) EcoGreenLight else EcoCard)
                    .border(
                        BorderStroke(1.dp, if (isSelected) EcoGreen else EcoBorder),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(point) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Recycling, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(point.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoTextPrimary)
                    Text(
                        point.materials.joinToString(" · ").ifBlank { "Materiales no especificados" },
                        fontSize = 11.sp,
                        color = EcoTextMuted
                    )
                    Text(distanceLabel(point.distanceMeters), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EcoGreen)
                }
                IconButton(onClick = { onDirections(point) }) {
                    Icon(Icons.Filled.Directions, contentDescription = "Cómo llegar", tint = EcoGreenDark)
                }
            }
        }
    }
}

@Composable
private fun StatusMessage(
    icon: ImageVector?,
    text: String,
    actionLabel: String? = null,
    showRefreshIcon: Boolean = false,
    onAction: () -> Unit = {},
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = EcoTextMuted, modifier = Modifier.size(40.dp))
        } else {
            CircularProgressIndicator(color = EcoGreen, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text, fontSize = 13.sp, color = EcoTextSecondary, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) {
                if (showRefreshIcon) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(actionLabel)
            }
        }
        if (secondaryLabel != null) {
            OutlinedButton(onClick = onSecondary) { Text(secondaryLabel, color = EcoGreenDark) }
        }
    }
}
