package com.example.ecopoints.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.EcoLevel
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)

        // Recepción de parámetros por Intent (Criterio P03)
        val userName = intent.getStringExtra(IntentExtras.USER_NAME) ?: prefs.getUserName().ifBlank { "EcoAmigo" }
        val petLevel = intent.getStringExtra(IntentExtras.PET_LEVEL) ?: prefs.getPetLevel()
        val isNewUser = intent.getBooleanExtra(IntentExtras.IS_NEW_USER, false)

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    WelcomeScreenContent(
                        userName = userName,
                        petLevel = petLevel,
                        isNewUser = isNewUser,
                        balance = prefs.getEcoPointsBalance(),
                        level = EcoLevel.forPoints(prefs.getEcoPointsHistorical()),
                        onGoHome = {
                            // Comunicación hacia HomeActivity mediante Intent
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra(IntentExtras.USER_NAME, userName)
                                putExtra(IntentExtras.PET_LEVEL, petLevel)
                            }
                            startActivity(intent)
                            finish()
                        },
                        onOpenSettings = {
                            // Comunicación hacia SettingsActivity mediante Intent
                            val intent = Intent(this, SettingsActivity::class.java).apply {
                                putExtra(IntentExtras.USER_NAME, userName)
                            }
                            startActivity(intent)
                        },
                        onLogout = {
                            prefs.setUserLoggedIn(false)
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
}

@Composable
fun WelcomeScreenContent(
    userName: String,
    petLevel: String,
    isNewUser: Boolean,
    balance: Int,
    level: EcoLevel,
    onGoHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val petImageRes = when {
        petLevel.contains("Panda", ignoreCase = true) -> R.drawable.panda_ecopoints
        petLevel.contains("Zorro", ignoreCase = true) -> R.drawable.zorro_ecopoints
        petLevel.contains("Gato", ignoreCase = true) -> R.drawable.gato_ecopoints
        petLevel.contains("Búho", ignoreCase = true) || petLevel.contains("Buho", ignoreCase = true) -> R.drawable.buho_ecopoints
        else -> R.drawable.koala_ecopoints
    }

    val petDisplayName = if (petLevel.contains("(")) {
        petLevel.substringAfter("(").substringBefore(")")
    } else {
        petLevel.substringBefore(" ")
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_leaf_logo),
                    contentDescription = "Logo EcoPoints",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "¡Bienvenido, $userName!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "¡Hola! Soy tu guardián $petDisplayName 🌱",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = EcoGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Cuidaremos el medio ambiente juntos para ganar EcoPoints y subir de nivel.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(BorderStroke(2.dp, EcoGreenLight), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = petImageRes),
                    contentDescription = "Mascota Elegida",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(220.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (isNewUser) "¡Todo listo para comenzar!" else "¡Qué bueno verte de nuevo!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EcoGreenDark
                    )
                    Text(
                        if (isNewUser) "Hemos preparado tu espacio ecológico" else "Tienes $balance EcoPoints esperando en tu alcancía verde",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // El bono de bienvenida (RF-03) solo se muestra la primera vez
                    if (isNewUser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoGreenLight)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎁", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bono de bienvenida", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EcoGreenDark)
                                    Text("Acreditados a tu alcancía verde", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                            Text("+50 EcoPoints", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EcoGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF9E6))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏆", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (isNewUser) "Insignia Desbloqueada" else "Tu insignia actual", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EcoGold)
                                Text("\"${level.badge}\"", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                        Text("Nivel ${level.number}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EcoGold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onGoHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                    ) {
                        Text("Ir al Panel Principal →", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚙️ Ajustes", fontSize = 12.sp, color = EcoGreenDark)
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EcoError)
                        ) {
                            Text("Cerrar sesión", fontSize = 12.sp, color = EcoError)
                        }
                    }
                }
            }
        }
    }
}
