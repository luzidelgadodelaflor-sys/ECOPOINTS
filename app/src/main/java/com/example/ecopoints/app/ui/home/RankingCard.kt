package com.example.ecopoints.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.domain.RankingBoard
import com.example.ecopoints.app.domain.RankingEntry
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoGold
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary

/**
 * Ranking por EcoPoints históricos. Si el usuario desactivó "Visible en el Ranking
 * público", su fila aparece como anónima.
 */
@Composable
fun RankingCard(userName: String, historicalPoints: Int, visible: Boolean) {
    val ranking = RankingBoard.build(userName, historicalPoints)
    val userPosition = RankingBoard.positionOf(ranking)

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
                    Icon(Icons.Filled.Leaderboard, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ranking de la comunidad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                }
                Text("Vas #$userPosition", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreen)
            }
            Text(
                "Según los EcoPoints ganados en total (gastar en tu mascota no te baja de puesto)",
                fontSize = 11.sp,
                color = EcoTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            ranking.take(RankingBoard.TOP_SIZE).forEachIndexed { index, entry ->
                RankingRow(position = index + 1, entry = entry, visible = visible)
            }
            // Si el usuario no está en el top, se muestra igual su posición al final
            if (userPosition > RankingBoard.TOP_SIZE) {
                Text("···", color = EcoTextMuted, modifier = Modifier.padding(start = 12.dp))
                RankingRow(position = userPosition, entry = ranking[userPosition - 1], visible = visible)
            }

            if (!visible) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = EcoTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Tu nombre está oculto para los demás. Puedes cambiarlo en Ajustes.",
                        fontSize = 11.sp,
                        color = EcoTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingRow(position: Int, entry: RankingEntry, visible: Boolean) {
    val medalColor = when (position) {
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFCD7F32)
        else -> null
    }
    val name = when {
        !entry.isUser -> entry.name
        visible -> "${entry.name} (tú)"
        else -> "Anónimo (tú)"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (entry.isUser) EcoGreenLight else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (medalColor != null) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = "Puesto $position", tint = medalColor, modifier = Modifier.size(20.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(EcoGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$position", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            name,
            fontSize = 13.sp,
            fontWeight = if (entry.isUser) FontWeight.Bold else FontWeight.Normal,
            color = if (entry.isUser) EcoGreenDark else EcoTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text("${entry.points} pts ganados", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EcoGold)
    }
}
