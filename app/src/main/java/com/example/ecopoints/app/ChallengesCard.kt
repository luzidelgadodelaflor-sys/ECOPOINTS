package com.example.ecopoints.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import kotlin.math.ceil

private data class ChallengeSuggestion(val icon: String, val title: String, val description: String)

private val suggestions = listOf(
    ChallengeSuggestion("🚲", "Movilidad Sostenible", "Usar bicicleta o caminar en lugar de auto"),
    ChallengeSuggestion("♻️", "Reciclaje Activo", "Separar 3 botellas plásticas o latas"),
    ChallengeSuggestion("🛍️", "Cero Plásticos", "Llevar bolsa ecológica a las compras"),
    ChallengeSuggestion("💡", "Ahorro Energético", "Desconectar aparatos y apagar luces sin uso"),
    ChallengeSuggestion("💧", "Ahorro de Agua", "Cerrar la llave mientras te cepillas los dientes"),
    ChallengeSuggestion("🌳", "Siembra Algo", "Plantar o cuidar una planta")
)

private fun durationLabel(days: Int) = if (days == 1) "1 día" else "$days días"

private fun deadlineText(challenge: EcoChallenge, now: Long): String = when {
    challenge.completed -> "¡Cumplido!"
    challenge.isExpired(now) -> "Vencido"
    else -> {
        val left = ceil((challenge.deadlineMillis - now).toDouble() / EcoChallenge.DAY_MILLIS)
            .toInt()
            .coerceAtLeast(1)
        if (left == 1) "Queda 1 día" else "Quedan $left días"
    }
}

/**
 * Tarjeta "Mis Retos": el usuario elige qué retos quiere cumplir, define el plazo
 * (1, 3, 7 o 14 días), puede editarlos, borrarlos y marcarlos o desmarcarlos.
 * Todo se guarda en SharedPreferences.
 */
@Composable
fun ChallengesCard(prefs: PreferencesManager, onBalanceChanged: () -> Unit) {
    val context = LocalContext.current
    var challenges by remember { mutableStateOf(prefs.getChallenges()) }
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<EcoChallenge?>(null) }
    var pendingDelete by remember { mutableStateOf<EcoChallenge?>(null) }

    fun update(newList: List<EcoChallenge>) {
        challenges = newList
        prefs.saveChallenges(newList)
    }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    val now = System.currentTimeMillis()

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
                Column(modifier = Modifier.weight(1f)) {
                    Text("🌱 Mis Retos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                    Text(
                        "${challenges.count { it.completed }}/${challenges.size} cumplidos",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                OutlinedButton(
                    onClick = {
                        editing = null
                        showDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreen)
                ) {
                    Icon(painterResource(id = R.drawable.ic_add), contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontSize = 12.sp, color = EcoGreenDark, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (challenges.isEmpty()) {
                Text(
                    "Aún no tienes retos. Toca «Agregar», elige los que quieras cumplir y decide en cuántos días.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            challenges.forEach { challenge ->
                val expired = challenge.isExpired(now)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                challenge.completed -> EcoGreenLight
                                expired -> Color(0xFFFDECEC)
                                else -> Color(0xFFF9FBF9)
                            }
                        )
                        .padding(start = 10.dp, top = 8.dp, bottom = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(challenge.icon, fontSize = 22.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            challenge.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (challenge.completed) EcoGreenDark else Color.Black
                        )
                        if (challenge.description.isNotBlank()) {
                            Text(challenge.description, fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "⏳ ${deadlineText(challenge, now)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    challenge.completed -> EcoGreen
                                    expired -> EcoError
                                    else -> Color.DarkGray
                                }
                            )
                            Text(
                                "  ·  +${challenge.points} pts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (challenge.completed) EcoGreen else Color.Gray
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Un reto cumplido no se edita: primero se desmarca.
                            if (!challenge.completed) {
                                IconButton(
                                    onClick = {
                                        editing = challenge
                                        showDialog = true
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "Editar reto",
                                        tint = EcoGreenDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { pendingDelete = challenge },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Eliminar reto",
                                    tint = EcoError,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Checkbox(
                        checked = challenge.completed,
                        // Si venció, solo se puede volver a activar editándolo.
                        enabled = challenge.completed || !expired,
                        onCheckedChange = { checked ->
                            if (checked) {
                                prefs.earnEcoPoints(challenge.points)
                                update(challenges.map { if (it.id == challenge.id) it.copy(completed = true) else it })
                                onBalanceChanged()
                                toast("¡Reto superado! +${challenge.points} EcoPoints 🌱")
                            } else if (prefs.revokeEcoPoints(challenge.points)) {
                                update(challenges.map { if (it.id == challenge.id) it.copy(completed = false) else it })
                                onBalanceChanged()
                                toast("Reto desmarcado (-${challenge.points} pts)")
                            } else {
                                toast("No puedes desmarcarlo: ya usaste esos puntos")
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = EcoGreen)
                    )
                }
            }
        }
    }

    if (showDialog) {
        ChallengeDialog(
            initial = editing,
            onDismiss = {
                showDialog = false
                editing = null
            },
            onSave = { icon, title, description, days ->
                val target = editing
                val savedAt = System.currentTimeMillis()
                if (target == null) {
                    update(challenges + EcoChallenge(savedAt, icon, title, description, days, savedAt))
                    toast("Reto agregado: tienes ${durationLabel(days)} para cumplirlo")
                } else {
                    update(challenges.map {
                        if (it.id != target.id) it else {
                            // El plazo vuelve a contar desde hoy si lo cambian o si ya había vencido.
                            val restart = days != it.durationDays || it.isExpired(savedAt)
                            it.copy(
                                icon = icon,
                                title = title,
                                description = description,
                                durationDays = days,
                                startMillis = if (restart) savedAt else it.startMillis
                            )
                        }
                    })
                    toast("Reto actualizado")
                }
                showDialog = false
                editing = null
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Color.White,
            title = { Text("¿Eliminar reto?", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
            text = { Text("«${target.title}» se quitará de tu lista. Los puntos que ya ganaste se conservan.") },
            confirmButton = {
                TextButton(onClick = {
                    update(challenges.filter { it.id != target.id })
                    pendingDelete = null
                }) { Text("Eliminar", color = EcoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar", color = EcoGreenDark) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChallengeDialog(
    initial: EcoChallenge?,
    onDismiss: () -> Unit,
    onSave: (icon: String, title: String, description: String, days: Int) -> Unit
) {
    var icon by remember { mutableStateOf(initial?.icon ?: "🌿") }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var days by remember { mutableStateOf(initial?.durationDays ?: 3) }
    var showTitleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                if (initial == null) "Nuevo reto" else "Editar reto",
                color = EcoGreenDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (initial == null) {
                    Text("Elige uno sugerido o escribe el tuyo", fontSize = 12.sp, color = Color.Gray)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    ) {
                        suggestions.forEach { s ->
                            AssistChip(
                                onClick = {
                                    icon = s.icon
                                    title = s.title
                                    description = s.description
                                    showTitleError = false
                                },
                                label = { Text("${s.icon} ${s.title}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        showTitleError = false
                    },
                    label = { Text("Nombre del reto") },
                    singleLine = true,
                    isError = showTitleError,
                    supportingText = if (showTitleError) {
                        { Text("Escribe un nombre para tu reto") }
                    } else null,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Plazo para cumplirlo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    EcoChallenge.DURATION_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = days == option,
                            onClick = { days = option },
                            label = { Text(durationLabel(option), fontSize = 12.sp) }
                        )
                    }
                }
                Text(
                    "Recompensa: +${EcoChallenge.pointsForDuration(days)} EcoPoints",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoGreen,
                    modifier = Modifier.padding(top = 6.dp)
                )
                if (initial != null) {
                    Text(
                        "Si cambias el plazo, empieza a contar desde hoy.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        showTitleError = true
                    } else {
                        onSave(icon, title.trim(), description.trim(), days)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = EcoGreenDark) }
        }
    )
}
