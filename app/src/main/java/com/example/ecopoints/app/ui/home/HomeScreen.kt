package com.example.ecopoints.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.ecopoints.app.R
import com.example.ecopoints.app.data.PetStage
import com.example.ecopoints.app.domain.PetAction
import com.example.ecopoints.app.ui.components.EcoSectionCard
import com.example.ecopoints.app.ui.components.petImageRes
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoGold
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoHappiness
import com.example.ecopoints.app.ui.theme.EcoSpacing
import com.example.ecopoints.app.ui.theme.EcoStreak
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextSecondary
import com.example.ecopoints.app.ui.theme.EcoTrack
import kotlinx.coroutines.delay

/**
 * Panel principal. Solo dibuja el [HomeUiState] que entrega el [HomeViewModel] y le avisa
 * lo que el usuario toca; no conoce Room ni DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Mensajes breves y vibración que pide el ViewModel
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.Message -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                HomeEvent.Vibrate -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // Con la pantalla abierta las barras de la mascota bajan en vivo (se revisa cada 5 s)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(5_000)
                viewModel.tickDecay()
            }
        }
    }

    // Android 13+ pide permiso para mostrar el aviso de "tu mascota tiene hambre"
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    var askedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.isLoading, state.challengeRemindersEnabled) {
        if (!askedNotificationPermission &&
            !state.isLoading &&
            state.challengeRemindersEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            askedNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_leaf_logo),
                            contentDescription = "Logo EcoPoints",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "EcoPoints",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenDark
                            )
                            Text(
                                "¡Hola, ${state.userName.ifBlank { "EcoAmigo" }}!",
                                fontSize = 12.sp,
                                color = EcoTextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Ajustes",
                            tint = EcoGreenDark
                        )
                    }
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_exit),
                            contentDescription = "Cerrar sesión",
                            tint = EcoError
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EcoCard)
            )
        },
        containerColor = EcoBackground
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EcoGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = EcoSpacing.Screen, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(EcoSpacing.Section)
            ) {
                // 1. Tarjeta Resumen de EcoPoints
                BalanceCard(state)

                // 2. Tarjeta Interactiva de la Mascota Guardián
                PetCard(
                    state = state,
                    onFeed = { viewModel.onPetAction(PetAction.FEED) },
                    onPlay = { viewModel.onPetAction(PetAction.PLAY) }
                )

                // 3. Mis Retos: el usuario elige, edita y define el plazo de cada reto
                ChallengesCard(
                    challenges = state.challenges,
                    onAdd = viewModel::addChallenge,
                    onUpdate = viewModel::updateChallenge,
                    onDelete = viewModel::deleteChallenge,
                    onComplete = viewModel::completeChallenge
                )

                // 4. Impacto ecológico estimado de los retos cumplidos
                ImpactCard(state.achievementStats.impact)

                // 5. Logros e insignias
                AchievementsCard(state.achievementStats)

                // 6. Ranking por EcoPoints históricos
                RankingCard(
                    userName = state.userName,
                    historicalPoints = state.historicalPoints,
                    visible = state.rankingVisible
                )

                // 7. EcoMapa / Puntos Ecológicos Cercanos
                MapPreviewCard(radiusKm = state.mapRadiusKm, onOpenMap = onOpenMap)
            }
        }
    }

    // Confirmación antes de gastar EcoPoints (opción de Ajustes)
    state.pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPendingAction,
            containerColor = EcoCard,
            title = { Text("¿${action.label}?", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
            text = {
                Text("Esto gastará ${action.cost} EcoPoints. Tu saldo quedará en ${state.balance - action.cost} pts.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmPendingAction,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) { Text("Gastar ${action.cost} pts") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPendingAction) { Text("Cancelar", color = EcoGreenDark) }
            }
        )
    }

    // Logros recién desbloqueados (después del bono diario, para no mostrar dos avisos a la vez)
    if (state.dailyVisit == null && state.newAchievements.isNotEmpty()) {
        NewAchievementsDialog(state.newAchievements, onDismiss = viewModel::dismissAchievements)
    }

    // Bono diario: se muestra solo en la primera visita del día
    state.dailyVisit?.let { visit ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDailyVisit,
            containerColor = EcoCard,
            icon = { Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(40.dp)) },
            title = {
                Text("¡Bono diario!", color = EcoGreenDark, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("+${visit.bonus} EcoPoints", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = EcoGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = EcoStreak, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (visit.streak == 1) "Racha de 1 día" else "¡Racha de ${visit.streak} días!",
                            fontWeight = FontWeight.Bold,
                            color = EcoStreak
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Vuelve mañana para mantener tu racha y ganar otro bono. ${state.petDisplayName} te espera.",
                        fontSize = 13.sp,
                        color = EcoTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::dismissDailyVisit,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) { Text("¡Genial!") }
            }
        )
    }
}

/** Tarjeta verde con el saldo, el nivel y la racha. */
@Composable
private fun BalanceCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tu Saldo Ecológico", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(
                    "${state.balance} pts",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EcoGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${state.level.badge} (Nivel ${state.level.number})",
                        color = EcoGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = EcoStreak, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (state.streak == 1) "Racha: 1 día" else "Racha: ${state.streak} días",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

/** La mascota: crece con el nivel, muestra su ánimo y sus barras, y permite alimentarla o jugar. */
@Composable
private fun PetCard(state: HomeUiState, onFeed: () -> Unit, onPlay: () -> Unit) {
    val petStage = state.petStage
    val nextLevel = state.nextLevel

    // Ánimo de la mascota según sus indicadores (triste = imagen apagada)
    val unhappy = state.isHungry || state.isSad
    val moodIcon = if (unhappy) Icons.Filled.SentimentDissatisfied else Icons.Filled.SentimentSatisfied
    val moodText = when {
        state.isHungry -> "Hambriento"
        state.isSad -> "Triste"
        else -> "Feliz"
    }
    val moodColor = if (unhappy) EcoError else EcoGreen

    EcoSectionCard(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Compañero: ${state.petDisplayName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
                Text("Etapa: ${petStage.label}", fontSize = 12.sp, color = EcoTextMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(moodIcon, contentDescription = null, tint = moodColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(moodText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = moodColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // La mascota crece con el nivel; en la etapa legendaria gana un marco dorado
            val legendary = petStage == PetStage.LEGENDARY
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EcoBackground)
                    .border(
                        BorderStroke(if (legendary) 3.dp else 1.5.dp, if (legendary) EcoGold else EcoGreenLight),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = petImageRes(state.petSpecies)),
                    contentDescription = "Mascota",
                    contentScale = ContentScale.Fit,
                    // Si tiene hambre o está triste, la imagen se ve apagada
                    colorFilter = if (unhappy) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) })
                    } else null,
                    alpha = if (unhappy) 0.75f else 1f,
                    modifier = Modifier.size(petStage.imageSizeDp.dp)
                )
                if (legendary) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Mascota legendaria",
                        tint = EcoGold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Barra de Hambre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatLabel(Icons.Filled.Restaurant, "Energía / Hambre", EcoGreen)
                    Text("${state.hunger}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                }
                LinearProgressIndicator(
                    progress = { state.hunger / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (state.hunger > 40) EcoGreen else EcoError,
                    trackColor = EcoTrack
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Barra de Felicidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatLabel(Icons.Filled.Favorite, "Felicidad", EcoHappiness)
                    Text("${state.happiness}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                }
                LinearProgressIndicator(
                    progress = { state.happiness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EcoHappiness,
                    trackColor = EcoTrack
                )
            }
        }

        // Cuánto falta para que la mascota crezca a la siguiente etapa
        if (nextLevel != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Crecerá a ${PetStage.forLevel(nextLevel).label} al ganar ${nextLevel.minPoints} pts " +
                        "(te faltan ${nextLevel.minPoints - state.historicalPoints})",
                    fontSize = 11.sp,
                    color = EcoTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Botones de acción con la mascota
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onFeed,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alimentar (-${PetAction.FEED.cost})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onPlay,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, EcoGreen)
            ) {
                Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Jugar (-${PetAction.PLAY.cost})", fontSize = 12.sp, color = EcoGreenDark, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Acceso al EcoMapa con el radio de búsqueda elegido en Ajustes. */
@Composable
private fun MapPreviewCard(radiusKm: Int, onOpenMap: () -> Unit) {
    EcoSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "EcoMapa Cercano",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
            }
            Text(
                "Radio: $radiusKm km",
                fontSize = 11.sp,
                color = EcoGreen,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Encuentra contenedores y centros de reciclaje reales cerca de ti con tu GPS.",
            fontSize = 12.sp,
            color = EcoTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onOpenMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
        ) {
            Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Abrir EcoMapa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatLabel(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = EcoTextSecondary)
    }
}
