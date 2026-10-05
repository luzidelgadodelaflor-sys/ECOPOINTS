package com.example.ecopoints.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

        // Dato recibido por Intent desde HomeActivity o WelcomeActivity
        val userName = intent.getStringExtra(IntentExtras.USER_NAME) ?: prefs.getUserName().ifBlank { "EcoAmigo" }

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    SettingsScreenContent(
                        userName = userName,
                        prefs = prefs,
                        onSettingsChanged = {
                            // Resultado que vuelve a la actividad que abrió los ajustes
                            val result = Intent().apply {
                                putExtra(IntentExtras.MAP_RADIUS_KM, prefs.getMapSearchRadiusKm())
                                putExtra(IntentExtras.RANKING_VISIBLE, prefs.isRankingVisible())
                                putExtra(IntentExtras.NOTIFICATIONS_ENABLED, prefs.areNotificationsEnabled())
                            }
                            setResult(RESULT_OK, result)
                        },
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
    userName: String,
    prefs: PreferencesManager,
    onSettingsChanged: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(prefs.areNotificationsEnabled()) }
    var petRemindersEnabled by remember { mutableStateOf(prefs.arePetRemindersEnabled()) }
    var rankingVisible by remember { mutableStateOf(prefs.isRankingVisible()) }
    var confirmSpend by remember { mutableStateOf(prefs.isConfirmSpendEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(prefs.isVibrationEnabled()) }
    var mapRadius by remember { mutableIntStateOf(prefs.getMapSearchRadiusKm()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Ajustes de EcoPoints",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EcoGreenDark
            )
        }
        Text(
            "Preferencias de $userName",
            fontSize = 13.sp,
            color = EcoTextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Apariencia: se aplica al instante en todas las pantallas
        SettingsSection("Apariencia") {
            SettingLabel(Icons.Outlined.DarkMode, "Tema")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    ThemeMode.SYSTEM to "Sistema",
                    ThemeMode.LIGHT to "Claro",
                    ThemeMode.DARK to "Oscuro"
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = AppearanceSettings.themeMode == mode,
                        onClick = {
                            AppearanceSettings.themeMode = mode
                            prefs.setThemeMode(mode)
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                icon = Icons.Outlined.TextFields,
                label = "Texto grande",
                checked = AppearanceSettings.largeText,
                onCheckedChange = {
                    AppearanceSettings.largeText = it
                    prefs.setLargeTextEnabled(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Notificaciones y Comunidad") {
            SettingSwitchRow(
                icon = Icons.Outlined.NotificationsActive,
                label = "Avisos de retos por vencer",
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    prefs.setNotificationsEnabled(it)
                    onSettingsChanged()
                    Toast.makeText(context, "Ajuste guardado en SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Pets,
                label = "Avisos de la mascota con hambre",
                checked = petRemindersEnabled,
                onCheckedChange = {
                    petRemindersEnabled = it
                    prefs.setPetRemindersEnabled(it)
                }
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Leaderboard,
                label = "Visible en el Ranking público",
                checked = rankingVisible,
                onCheckedChange = {
                    rankingVisible = it
                    prefs.setRankingVisible(it)
                    onSettingsChanged()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Mascota y EcoPoints") {
            SettingSwitchRow(
                icon = Icons.Outlined.WarningAmber,
                label = "Confirmar antes de gastar EcoPoints",
                checked = confirmSpend,
                onCheckedChange = {
                    confirmSpend = it
                    prefs.setConfirmSpendEnabled(it)
                }
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Vibration,
                label = "Vibración",
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it
                    prefs.setVibrationEnabled(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("EcoMapa") {
            SettingLabel(Icons.Outlined.Radar, "Radio de búsqueda")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PreferencesManager.MAP_RADIUS_OPTIONS_KM.forEach { km ->
                    FilterChip(
                        selected = mapRadius == km,
                        onClick = {
                            mapRadius = km
                            prefs.setMapSearchRadiusKm(km)
                            onSettingsChanged()
                        },
                        label = { Text("$km km") }
                    )
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
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EcoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun SettingLabel(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = EcoTextPrimary)
    }
}

@Composable
fun SettingSwitchRow(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingLabel(icon, label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EcoGreen)
        )
    }
}
