package com.example.ecopoints.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecopoints.app.data.model.ThemeMode
import com.example.ecopoints.app.domain.EcoRules
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary

/**
 * Ajustes de EcoPoints. Cada cambio se guarda al instante en DataStore mediante [SettingsViewModel];
 * "Guardar y Volver" solo regresa a la pantalla anterior.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoBackground)
            .verticalScroll(rememberScrollState())
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
            "Preferencias de ${state.userName}",
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
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                icon = Icons.Outlined.TextFields,
                label = "Texto grande",
                checked = settings.largeText,
                onCheckedChange = viewModel::setLargeText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Notificaciones y Comunidad") {
            SettingSwitchRow(
                icon = Icons.Outlined.NotificationsActive,
                label = "Avisos de retos por vencer",
                checked = settings.challengeRemindersEnabled,
                onCheckedChange = {
                    viewModel.setChallengeReminders(it)
                    Toast.makeText(context, "Ajuste guardado en DataStore", Toast.LENGTH_SHORT).show()
                }
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Pets,
                label = "Avisos de la mascota con hambre",
                checked = settings.petRemindersEnabled,
                onCheckedChange = viewModel::setPetReminders
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Leaderboard,
                label = "Visible en el Ranking público",
                checked = settings.rankingVisible,
                onCheckedChange = viewModel::setRankingVisible
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("Mascota y EcoPoints") {
            SettingSwitchRow(
                icon = Icons.Outlined.WarningAmber,
                label = "Confirmar antes de gastar EcoPoints",
                checked = settings.confirmSpend,
                onCheckedChange = viewModel::setConfirmSpend
            )
            SettingSwitchRow(
                icon = Icons.Outlined.Vibration,
                label = "Vibración",
                checked = settings.vibrationEnabled,
                onCheckedChange = viewModel::setVibration
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("EcoMapa") {
            SettingLabel(Icons.Outlined.Radar, "Radio de búsqueda")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EcoRules.MAP_RADIUS_OPTIONS_KM.forEach { km ->
                    FilterChip(
                        selected = settings.mapRadiusKm == km,
                        onClick = { viewModel.setMapRadiusKm(km) },
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
private fun SettingSwitchRow(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
