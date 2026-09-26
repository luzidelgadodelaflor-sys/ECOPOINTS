package com.example.ecopoints.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)

        // Parámetros recibidos por Intent (Criterio P03)
        val userName = intent.getStringExtra("EXTRA_USER_NAME") ?: prefs.getUserName().ifBlank { "EcoAmigo" }
        val petLevel = intent.getStringExtra("EXTRA_PET_LEVEL") ?: prefs.getPetLevel()

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    HomeScreenContent(
                        userName = userName,
                        petLevel = petLevel,
                        prefs = prefs,
                        onOpenSettings = {
                            // Comunicación con SettingsActivity vía Intent
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                        },
                        onLogout = {
                            prefs.setUserLoggedIn(false)
                            // Comunicación con MainActivity para cerrar sesión
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

data class EcoChallenge(
    val id: Int,
    val icon: String,
    val title: String,
    val description: String,
    val points: Int,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userName: String,
    petLevel: String,
    prefs: PreferencesManager,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // Estados reactivos conectados a SharedPreferences
    var balance by remember { mutableStateOf(prefs.getEcoPointsBalance()) }
    var hunger by remember { mutableStateOf(prefs.getPetHunger()) }
    var happiness by remember { mutableStateOf(prefs.getPetHappiness()) }

    val petDisplayName = if (petLevel.contains("(")) {
        petLevel.substringAfter("(").substringBefore(")")
    } else {
        petLevel.substringBefore(" ")
    }

    val petImageRes = when {
        petLevel.contains("Panda", ignoreCase = true) -> R.drawable.panda_ecopoints
        petLevel.contains("Zorro", ignoreCase = true) -> R.drawable.zorro_ecopoints
        petLevel.contains("Gato", ignoreCase = true) -> R.drawable.gato_ecopoints
        petLevel.contains("Búho", ignoreCase = true) || petLevel.contains("Buho", ignoreCase = true) -> R.drawable.buho_ecopoints
        else -> R.drawable.koala_ecopoints
    }

    // Lista de retos ecológicos interactivos
    var challenges by remember {
        mutableStateOf(
            listOf(
                EcoChallenge(1, "🚲", "Movilidad Sostenible", "Usar bicicleta o caminar en lugar de auto", 20),
                EcoChallenge(2, "♻️", "Reciclaje Activo", "Separar 3 botellas plásticas o latas", 15),
                EcoChallenge(3, "🛍️", "Cero Plásticos", "Llevar bolsa ecológica a las compras", 15),
                EcoChallenge(4, "💡", "Ahorro Energético", "Desconectar aparatos y apagar luces sin uso", 10)
            )
        )
    }

    val completedCount = challenges.count { it.isCompleted }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_ecopoints),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
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
                                "¡Hola, $userName! 🌿",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Text("⚙️", fontSize = 18.sp)
                    }
                    IconButton(onClick = onLogout) {
                        Text("🚪", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                        Text("Tu Saldo Ecológico", color = EcoGreenLight, fontSize = 13.sp)
                        Text(
                            "$balance pts",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "🏆 Amigo de la Naturaleza (Nivel 1)",
                            color = EcoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌱", fontSize = 28.sp)
                    }
                }
            }

            // 2. Tarjeta Interactiva de la Mascota Guardián
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        Text(
                            "Compañero: $petDisplayName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenDark
                        )
                        Text(
                            "Guardia Verde",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EcoGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(EcoBackground)
                                .border(BorderStroke(1.5.dp, EcoGreenLight), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = petImageRes),
                                contentDescription = "Mascota",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(100.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            // Barra de Hambre
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🍖 Energía / Hambre", fontSize = 12.sp, color = Color.DarkGray)
                                Text("$hunger%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                            }
                            LinearProgressIndicator(
                                progress = { hunger / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (hunger > 40) EcoGreen else EcoError,
                                trackColor = Color(0xFFE0E0E0)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Barra de Felicidad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💖 Felicidad", fontSize = 12.sp, color = Color.DarkGray)
                                Text("$happiness%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                            }
                            LinearProgressIndicator(
                                progress = { happiness / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFE91E63),
                                trackColor = Color(0xFFE0E0E0)
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
                            onClick = {
                                if (prefs.spendEcoPoints(10)) {
                                    balance = prefs.getEcoPointsBalance()
                                    hunger = (hunger + 20).coerceAtMost(100)
                                    happiness = (happiness + 10).coerceAtMost(100)
                                    prefs.setPetHunger(hunger)
                                    prefs.setPetHappiness(happiness)
                                    Toast.makeText(context, "¡$petDisplayName comió rico! (-10 pts, +Energía)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "¡Puntos insuficientes! Completa retos para ganar más.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                        ) {
                            Text("🍎 Alimentar (-10)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                happiness = (happiness + 15).coerceAtMost(100)
                                prefs.setPetHappiness(happiness)
                                Toast.makeText(context, "¡$petDisplayName está jugando alegremente! 🐾", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EcoGreen)
                        ) {
                            Text("🎾 Jugar (+15)", fontSize = 12.sp, color = EcoGreenDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. Sección de Retos Diarios (Core Gamification)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🌱 Retos del Día", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                            Text("Completa acciones para ganar EcoPoints", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "$completedCount/${challenges.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    challenges.forEach { challenge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (challenge.isCompleted) EcoGreenLight else Color(0xFFF9FBF9))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(challenge.icon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        challenge.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (challenge.isCompleted) EcoGreenDark else Color.Black
                                    )
                                    Text(challenge.description, fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "+${challenge.points} pts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (challenge.isCompleted) EcoGreen else Color.Gray,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Checkbox(
                                    checked = challenge.isCompleted,
                                    onCheckedChange = { checked ->
                                        if (checked && !challenge.isCompleted) {
                                            challenge.isCompleted = true
                                            prefs.earnEcoPoints(challenge.points)
                                            balance = prefs.getEcoPointsBalance()
                                            prefs.setDailyChallengeProgress(completedCount + 1)
                                            Toast.makeText(context, "¡Reto superado! +${challenge.points} EcoPoints 🌱", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = EcoGreen)
                                )
                            }
                        }
                    }
                }
            }

            // 4. EcoMapa / Puntos Ecológicos Cercanos (Preview)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📍 EcoMapa Cercano",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenDark
                        )
                        Text(
                            "Radio: ${prefs.getMapSearchRadiusKm()} ${prefs.getMapDistanceUnit()}",
                            fontSize = 11.sp,
                            color = EcoGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ecoSpots = listOf(
                        "🌿 Punto Limpio Central - Av. Principal 123 (a 350 m)",
                        "🔋 Contenedor RAEE y Pilas - Supermercado Verde (a 600 m)",
                        "📦 Estación de Plásticos y Cartón - Parque Ecológico (a 850 m)"
                    )

                    ecoSpots.forEach { spot ->
                        Text(
                            spot,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
