package com.example.ecopoints.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    SettingsScreenContent(
                        prefs = prefs,
                        onBack = {
                            // Cierra la actividad y regresa a la actividad anterior
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(prefs.areNotificationsEnabled()) }
    var rankingVisible by remember { mutableStateOf(prefs.isRankingVisible()) }
    var petSoundEnabled by remember { mutableStateOf(prefs.isPetSoundEnabled()) }
    var mapUnit by remember { mutableStateOf(prefs.getMapDistanceUnit()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Text(
            "⚙️ Ajustes de EcoPoints",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = EcoGreenDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Notificaciones y Comunidad",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchRow(
                    label = "Recordatorios de retos diarios",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        prefs.setNotificationsEnabled(it)
                        Toast.makeText(context, "Ajuste guardado en SharedPreferences", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingSwitchRow(
                    label = "Visible en el Ranking público",
                    checked = rankingVisible,
                    onCheckedChange = {
                        rankingVisible = it
                        prefs.setRankingVisible(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingSwitchRow(
                    label = "Sonido y efectos de la mascota",
                    checked = petSoundEnabled,
                    onCheckedChange = {
                        petSoundEnabled = it
                        prefs.setPetSoundEnabled(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Preferencias de EcoMapa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unidad de distancia:", fontSize = 13.sp)
                    Row {
                        FilterChip(
                            selected = mapUnit == "km",
                            onClick = {
                                mapUnit = "km"
                                prefs.setMapDistanceUnit("km")
                            },
                            label = { Text("Kilómetros (km)") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = mapUnit == "millas",
                            onClick = {
                                mapUnit = "millas"
                                prefs.setMapDistanceUnit("millas")
                            },
                            label = { Text("Millas") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
        ) {
            Text("Guardar y Volver", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EcoGreen)
        )
    }
}
