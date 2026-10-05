package com.example.ecopoints.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ecopoints.app.data.EcoLevel
import com.example.ecopoints.app.data.PetStage
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*
import kotlinx.coroutines.delay

class HomeActivity : ComponentActivity() {

    // Radio del EcoMapa: se actualiza con el resultado que devuelve SettingsActivity
    private var mapRadiusKm by mutableIntStateOf(1)

    // Visibilidad en el ranking: también llega en el resultado de SettingsActivity
    private var rankingVisible by mutableStateOf(true)

    // Recibe el resultado (extras) que SettingsActivity devuelve al cerrarse
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            mapRadiusKm = data.getIntExtra(IntentExtras.MAP_RADIUS_KM, mapRadiusKm)
            rankingVisible = data.getBooleanExtra(IntentExtras.RANKING_VISIBLE, rankingVisible)
            Toast.makeText(this, "Ajustes actualizados: EcoMapa a $mapRadiusKm km", Toast.LENGTH_SHORT).show()
        }
    }

    // Android 13+ pide permiso para mostrar el aviso de "tu mascota tiene hambre"
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)
        mapRadiusKm = prefs.getMapSearchRadiusKm()
        rankingVisible = prefs.isRankingVisible()

        if (savedInstanceState == null &&
            prefs.areNotificationsEnabled() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Parámetros recibidos por Intent (Criterio P03)
        val userName = intent.getStringExtra(IntentExtras.USER_NAME) ?: prefs.getUserName().ifBlank { "EcoAmigo" }
        val petLevel = intent.getStringExtra(IntentExtras.PET_LEVEL) ?: prefs.getPetLevel()

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    HomeScreenContent(
                        userName = userName,
                        petLevel = petLevel,
                        prefs = prefs,
                        mapRadiusKm = mapRadiusKm,
                        rankingVisible = rankingVisible,
                        onOpenMap = { startActivity(Intent(this, EcoMapActivity::class.java)) },
                        onOpenSettings = {
                            // Comunicación con SettingsActivity: viaja el nombre y vuelve un resultado
                            val intent = Intent(this, SettingsActivity::class.java).apply {
                                putExtra(IntentExtras.USER_NAME, userName)
                            }
                            settingsLauncher.launch(intent)
                        },
                        onLogout = {
                            prefs.setUserLoggedIn(false)
                            // Comunicación con MainActivity para cerrar sesión
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                putExtra(IntentExtras.LOGGED_OUT, true)
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    // Mientras el usuario mira la pantalla no hace falta avisarle
    override fun onStart() {
        super.onStart()
        PetReminderReceiver.cancel(this)
        // El radio también se puede cambiar dentro del EcoMapa
        mapRadiusKm = PreferencesManager(this).getMapSearchRadiusKm()
    }

    // Al salir se programan los avisos: mascota con hambre y retos por vencer
    override fun onStop() {
        super.onStop()
        if (PreferencesManager(this).isUserLoggedIn()) PetReminderReceiver.schedule(this)
        // Si cerró sesión, schedule() cancela los avisos de retos
        ChallengeReminderReceiver.schedule(this)
    }
}

/** Acciones con la mascota y su costo en EcoPoints. */
private enum class PetAction(val cost: Int, val label: String) {
    FEED(cost = 20, label = "Alimentar"),
    PLAY(cost = 25, label = "Jugar")
}

/** Nombre que el usuario le puso a su mascota, o la especie si no le puso nombre. */
fun petDisplayName(petLevel: String): String =
    if (petLevel.contains("(")) {
        petLevel.substringAfter("(").substringBefore(")")
    } else {
        petLevel.substringBefore(" ")
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userName: String,
    petLevel: String,
    prefs: PreferencesManager,
    mapRadiusKm: Int,
    rankingVisible: Boolean,
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    fun vibrate() {
        if (prefs.isVibrationEnabled()) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Acción pendiente de confirmar ("¿Gastar 20 EcoPoints?") si está activado en Ajustes
    var pendingAction by remember { mutableStateOf<PetAction?>(null) }

    // Primera visita del día: bono al azar y racha (se registra antes de leer el saldo)
    var dailyVisit by remember { mutableStateOf(prefs.registerDailyVisit()) }
    val streak = prefs.getStreak()

    // Estados reactivos conectados a SharedPreferences
    var balance by remember { mutableStateOf(prefs.getEcoPointsBalance()) }
    // El nivel depende de los puntos históricos y se recalcula cuando cambia el saldo (RF-04)
    val historicalPoints = remember(balance) { prefs.getEcoPointsHistorical() }
    val level = EcoLevel.forPoints(historicalPoints)
    val nextLevel = EcoLevel.nextAfter(level)
    val petStage = PetStage.forLevel(level)

    // Impacto y logros: todo lo que los cambia (retos, alimentar, jugar) también cambia el saldo
    val achievementStats = remember(balance) { prefs.getAchievementStats() }
    var newAchievements by remember { mutableStateOf(prefs.checkNewAchievements()) }
    fun checkAchievements() {
        val unlocked = prefs.checkNewAchievements()
        if (unlocked.isNotEmpty()) newAchievements = newAchievements + unlocked
    }
    // Antes de leer los indicadores se descuenta el desgaste acumulado mientras la app estuvo cerrada
    var hunger by remember {
        prefs.applyPetDecay()
        mutableStateOf(prefs.getPetHunger())
    }
    var happiness by remember { mutableStateOf(prefs.getPetHappiness()) }

    // Con la pantalla abierta las barras bajan en vivo (se revisa cada 5 s)
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            prefs.applyPetDecay()
            hunger = prefs.getPetHunger()
            happiness = prefs.getPetHappiness()
        }
    }

    val petDisplayName = petDisplayName(petLevel)

    fun perform(action: PetAction) {
        if (!prefs.spendEcoPoints(action.cost)) {
            toast("¡Puntos insuficientes! Completa retos para ganar más.")
            return
        }
        balance = prefs.getEcoPointsBalance()
        when (action) {
            PetAction.FEED -> {
                // Comer solo recupera energía; la felicidad sube jugando
                hunger = (hunger + 20).coerceAtMost(100)
                prefs.setPetHunger(hunger)
                prefs.incrementFeedCount()
                toast("¡$petDisplayName comió rico! (-${action.cost} pts, +Energía)")
            }
            PetAction.PLAY -> {
                happiness = (happiness + 15).coerceAtMost(100)
                hunger = (hunger - 10).coerceAtLeast(0)
                prefs.setPetHappiness(happiness)
                prefs.setPetHunger(hunger)
                prefs.incrementPlayCount()
                toast("¡$petDisplayName está jugando alegremente! (-${action.cost} pts, -10 energía)")
            }
        }
        vibrate()
        checkAchievements()
    }

    /** Revisa si la acción tiene sentido y, si está activado, pide confirmación antes de gastar. */
    fun request(action: PetAction) {
        when {
            action == PetAction.FEED && hunger >= 100 ->
                toast("¡$petDisplayName ya está lleno! No necesita comer ahora")
            action == PetAction.PLAY && happiness >= 100 ->
                toast("¡$petDisplayName ya está súper feliz! Déjalo descansar un rato")
            balance < action.cost ->
                toast("¡Puntos insuficientes! Completa retos para ganar más.")
            prefs.isConfirmSpendEnabled() -> pendingAction = action
            else -> perform(action)
        }
    }

    // Ánimo de la mascota según sus indicadores (triste = imagen apagada)
    val isHungry = hunger < PreferencesManager.PET_LOW_STAT
    val isSad = happiness < PreferencesManager.PET_LOW_STAT
    val moodIcon = if (isHungry || isSad) Icons.Filled.SentimentDissatisfied else Icons.Filled.SentimentSatisfied
    val moodText = when {
        isHungry -> "Hambriento"
        isSad -> "Triste"
        else -> "Feliz"
    }
    val moodColor = if (isHungry || isSad) EcoError else EcoGreen

    val petImageRes = when {
        petLevel.contains("Panda", ignoreCase = true) -> R.drawable.panda_ecopoints
        petLevel.contains("Zorro", ignoreCase = true) -> R.drawable.zorro_ecopoints
        petLevel.contains("Gato", ignoreCase = true) -> R.drawable.gato_ecopoints
        petLevel.contains("Búho", ignoreCase = true) || petLevel.contains("Buho", ignoreCase = true) -> R.drawable.buho_ecopoints
        else -> R.drawable.koala_ecopoints
    }

    val scrollState = rememberScrollState()

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
                                "¡Hola, $userName!",
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
                    IconButton(onClick = onLogout) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta Resumen de EcoPoints
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
                            "$balance pts",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EcoGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${level.badge} (Nivel ${level.number})",
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
                                if (streak == 1) "Racha: 1 día" else "Racha: $streak días",
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

            // 2. Tarjeta Interactiva de la Mascota Guardián
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Compañero: $petDisplayName",
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
                                painter = painterResource(id = petImageRes),
                                contentDescription = "Mascota",
                                contentScale = ContentScale.Fit,
                                // Si tiene hambre o está triste, la imagen se ve apagada
                                colorFilter = if (isHungry || isSad) {
                                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) })
                                } else null,
                                alpha = if (isHungry || isSad) 0.75f else 1f,
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
                                Text("$hunger%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                            }
                            LinearProgressIndicator(
                                progress = { hunger / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (hunger > 40) EcoGreen else EcoError,
                                trackColor = EcoTrack
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Barra de Felicidad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatLabel(Icons.Filled.Favorite, "Felicidad", EcoHappiness)
                                Text("$happiness%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                            }
                            LinearProgressIndicator(
                                progress = { happiness / 100f },
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
                                    "(te faltan ${nextLevel.minPoints - historicalPoints})",
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
                            onClick = { request(PetAction.FEED) },
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
                            onClick = { request(PetAction.PLAY) },
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

            // 3. Mis Retos: el usuario elige, edita y define el plazo de cada reto
            ChallengesCard(
                prefs = prefs,
                onBalanceChanged = {
                    balance = prefs.getEcoPointsBalance()
                    checkAchievements()
                }
            )

            // 4. Impacto ecológico estimado de los retos cumplidos
            ImpactCard(achievementStats.impact)

            // 5. Logros e insignias
            AchievementsCard(achievementStats)

            // 6. Ranking por EcoPoints históricos (se recalcula cuando cambia el saldo)
            RankingCard(
                userName = userName,
                historicalPoints = historicalPoints,
                visible = rankingVisible
            )

            // 7. EcoMapa / Puntos Ecológicos Cercanos (Preview)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            "Radio: $mapRadiusKm km",
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
        }
    }

    // Confirmación antes de gastar EcoPoints (opción de Ajustes)
    pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = EcoCard,
            title = { Text("¿${action.label}?", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
            text = {
                Text("Esto gastará ${action.cost} EcoPoints. Tu saldo quedará en ${balance - action.cost} pts.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction = null
                        perform(action)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) { Text("Gastar ${action.cost} pts") }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("Cancelar", color = EcoGreenDark) }
            }
        )
    }

    // Logros recién desbloqueados (después del bono diario, para no mostrar dos avisos a la vez)
    if (dailyVisit == null && newAchievements.isNotEmpty()) {
        NewAchievementsDialog(newAchievements, onDismiss = { newAchievements = emptyList() })
    }

    // Bono diario: se muestra solo en la primera visita del día
    dailyVisit?.let { visit ->
        AlertDialog(
            onDismissRequest = { dailyVisit = null },
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
                        "Vuelve mañana para mantener tu racha y ganar otro bono. $petDisplayName te espera.",
                        fontSize = 13.sp,
                        color = EcoTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { dailyVisit = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) { Text("¡Genial!") }
            }
        )
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
