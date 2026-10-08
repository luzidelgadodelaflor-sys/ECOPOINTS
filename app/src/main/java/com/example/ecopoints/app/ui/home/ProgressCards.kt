package com.example.ecopoints.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.Achievement
import com.example.ecopoints.app.data.AchievementStats
import com.example.ecopoints.app.data.EcoImpact
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoGold
import com.example.ecopoints.app.ui.theme.EcoGoldContainer
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoRow
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary
import com.example.ecopoints.app.ui.theme.EcoTextSecondary
import com.example.ecopoints.app.ui.theme.EcoTrack
import java.util.Locale

private fun formatAmount(value: Double): String =
    if (value % 1.0 == 0.0) "%.0f".format(Locale.getDefault(), value) else "%.1f".format(Locale.getDefault(), value)

/** Tarjeta "Tu impacto": CO₂, agua y plástico evitados con los retos cumplidos. */
@Composable
fun ImpactCard(impact: EcoImpact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EcoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Public, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tu impacto en el planeta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (impact.isEmpty) {
                Text(
                    "Cumple tu primer reto para empezar a ver cuánto ayudas al planeta.",
                    fontSize = 12.sp,
                    color = EcoTextMuted
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImpactStat(Icons.Filled.Co2, formatAmount(impact.co2Kg), "kg de CO₂\nevitados", Modifier.weight(1f))
                    ImpactStat(Icons.Filled.WaterDrop, formatAmount(impact.waterLiters), "litros de\nagua", Modifier.weight(1f))
                    ImpactStat(Icons.Outlined.ShoppingBag, "${impact.plasticItems}", "plásticos\nevitados", Modifier.weight(1f))
                }

                val carKm = impact.co2Kg / EcoImpact.CAR_CO2_KG_PER_KM
                if (carKm >= 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = EcoTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Equivale a ${carKm.toInt()} km sin usar el auto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EcoTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = EcoTextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Valores aproximados por cada reto cumplido", fontSize = 10.sp, color = EcoTextMuted)
            }
        }
    }
}

@Composable
private fun ImpactStat(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(EcoGreenLight)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(22.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = EcoGreenDark)
        Text(label, fontSize = 10.sp, color = EcoTextSecondary, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

/** Ícono de cada logro. */
private fun achievementIcon(id: String): ImageVector = when (id) {
    "first_challenge" -> Icons.Filled.TaskAlt
    "five_challenges" -> Icons.Filled.Star
    "ten_challenges" -> Icons.Filled.WorkspacePremium
    "recycler" -> Icons.Filled.Recycling
    "cyclist" -> Icons.Filled.PedalBike
    "streak_3", "streak_7" -> Icons.Filled.LocalFireDepartment
    "feeder" -> Icons.Filled.Restaurant
    "player" -> Icons.Filled.SportsTennis
    "points_200" -> Icons.Filled.Savings
    "level_3" -> Icons.Filled.MilitaryTech
    "co2_10" -> Icons.Filled.Public
    else -> Icons.Filled.EmojiEvents
}

/** Tarjeta "Logros": muestra los desbloqueados y el progreso de los que faltan. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsCard(stats: AchievementStats) {
    var expanded by remember { mutableStateOf(false) }
    val unlocked = Achievement.ALL.count { it.isUnlocked(stats) }

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
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EcoGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Logros", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                }
                Text("$unlocked de ${Achievement.ALL.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGold)
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { unlocked / Achievement.ALL.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EcoGold,
                trackColor = EcoTrack
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Vista compacta: todas las insignias; al expandir, el detalle con el progreso
            if (!expanded) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Achievement.ALL.forEach { AchievementBadge(it, it.isUnlocked(stats)) }
                }
            } else {
                Achievement.ALL.forEach { AchievementRow(it, stats) }
            }

            TextButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = EcoGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (expanded) "Ver menos" else "Ver detalle y progreso",
                    fontSize = 12.sp,
                    color = EcoGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement, unlocked: Boolean, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (unlocked) EcoGoldContainer else EcoRow),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (unlocked) achievementIcon(achievement.id) else Icons.Filled.Lock,
            contentDescription = achievement.title,
            tint = if (unlocked) EcoGold else EcoTextMuted.copy(alpha = 0.6f),
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, stats: AchievementStats) {
    val unlocked = achievement.isUnlocked(stats)
    val progress = achievement.progress(stats)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AchievementBadge(achievement, unlocked, size = 38)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                achievement.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) EcoTextPrimary else EcoTextSecondary
            )
            Text(achievement.description, fontSize = 11.sp, color = EcoTextMuted)
            if (!unlocked) {
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { progress / achievement.target.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = EcoGreen,
                    trackColor = EcoTrack
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (unlocked) "¡Listo!" else "$progress/${achievement.target}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) EcoGold else EcoTextMuted
        )
    }
}

/** Aviso de "¡Nuevo logro!" cuando se desbloquean uno o varios. */
@Composable
fun NewAchievementsDialog(achievements: List<Achievement>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCard,
        icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EcoGold, modifier = Modifier.size(44.dp)) },
        title = {
            Text(
                if (achievements.size == 1) "¡Nuevo logro!" else "¡${achievements.size} logros nuevos!",
                color = EcoGreenDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                achievements.forEach { achievement ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AchievementBadge(achievement, unlocked = true, size = 38)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(achievement.title, fontWeight = FontWeight.Bold, color = EcoTextPrimary)
                            Text(achievement.description, fontSize = 12.sp, color = EcoTextMuted)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) { Text("¡Genial!") }
        }
    )
}
