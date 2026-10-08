package com.example.ecopoints.app.ui.home

import android.content.ActivityNotFoundException
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.R
import com.example.ecopoints.app.util.DateUtils
import com.example.ecopoints.app.util.Validators
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoErrorContainer
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoRow
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary
import com.example.ecopoints.app.ui.theme.EcoTextSecondary
import java.io.File
import kotlin.math.ceil

private data class ChallengeSuggestion(val icon: String, val title: String, val description: String)

private val suggestions = listOf(
    ChallengeSuggestion("bike", "Movilidad Sostenible", "Usar bicicleta o caminar en lugar de auto"),
    ChallengeSuggestion("recycle", "Reciclaje Activo", "Separar 3 botellas plásticas o latas"),
    ChallengeSuggestion("bag", "Cero Plásticos", "Llevar bolsa ecológica a las compras"),
    ChallengeSuggestion("light", "Ahorro Energético", "Desconectar aparatos y apagar luces sin uso"),
    ChallengeSuggestion("water", "Ahorro de Agua", "Cerrar la llave mientras te cepillas los dientes"),
    ChallengeSuggestion("tree", "Siembra Algo", "Plantar o cuidar una planta")
)

private const val DEFAULT_ICON = "leaf"

/** El reto guarda el nombre del ícono; los desconocidos (p. ej. retos antiguos) usan la hoja. */
private fun challengeIcon(key: String): ImageVector = when (key) {
    "bike" -> Icons.Filled.PedalBike
    "recycle" -> Icons.Filled.Recycling
    "bag" -> Icons.Filled.ShoppingBag
    "light" -> Icons.Filled.Lightbulb
    "water" -> Icons.Filled.WaterDrop
    "tree" -> Icons.Filled.Park
    else -> Icons.Filled.Eco
}

private fun durationLabel(days: Int) = EcoChallenge.durationLabel(days)

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
 * (1, 3, 7 o 14 días), puede editarlos y borrarlos. Para cumplir un reto debe enviar
 * una foto como evidencia; entonces gana los puntos y el reto pasa al historial.
 * Los datos viven en Room: la tarjeta solo dibuja la lista y avisa al ViewModel lo que el usuario hace.
 */
@Composable
fun ChallengesCard(
    challenges: List<EcoChallenge>,
    onAdd: (icon: String, title: String, description: String, days: Int) -> Unit,
    onUpdate: (id: Long, icon: String, title: String, description: String, days: Int) -> Unit,
    onDelete: (id: Long) -> Unit,
    onComplete: (id: Long, evidencePath: String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<EcoChallenge?>(null) }
    var pendingDelete by remember { mutableStateOf<EcoChallenge?>(null) }
    var completing by remember { mutableStateOf<EcoChallenge?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val active = challenges.filter { !it.completed }
    val history = challenges.filter { it.completed }.sortedByDescending { it.completedMillis }

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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Eco, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mis Retos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
                    }
                    Text(
                        "${active.size} pendientes · ${history.size} cumplidos",
                        fontSize = 12.sp,
                        color = EcoTextMuted
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

            if (active.isEmpty()) {
                Text(
                    if (history.isEmpty()) {
                        "Aún no tienes retos. Toca «Agregar», elige los que quieras cumplir y decide en cuántos días."
                    } else {
                        "No tienes retos pendientes. ¡Agrega uno nuevo para seguir ganando EcoPoints!"
                    },
                    fontSize = 12.sp,
                    color = EcoTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            active.forEach { challenge ->
                val expired = challenge.isExpired(now)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (expired) EcoErrorContainer else EcoRow)
                        .padding(start = 10.dp, top = 8.dp, bottom = 4.dp, end = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        challengeIcon(challenge.icon),
                        contentDescription = null,
                        tint = EcoGreenDark,
                        modifier = Modifier.padding(top = 4.dp).size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(challenge.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoTextPrimary)
                        if (challenge.description.isNotBlank()) {
                            Text(challenge.description, fontSize = 11.sp, color = EcoTextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val deadlineColor = if (expired) EcoError else EcoTextSecondary
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = deadlineColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                deadlineText(challenge, now),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = deadlineColor
                            )
                            Text(
                                "  ·  +${challenge.points} pts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoTextMuted
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Spacer(modifier = Modifier.weight(1f))
                            // Si venció, solo se puede volver a activar editándolo.
                            Button(
                                onClick = { completing = challenge },
                                enabled = !expired,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cumplir", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                TextButton(
                    onClick = { showHistory = !showHistory },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        if (showHistory) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = EcoGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showHistory) "Ocultar retos cumplidos" else "Ver retos cumplidos (${history.size})",
                        fontSize = 12.sp,
                        color = EcoGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (showHistory) {
                    history.forEach { CompletedChallengeRow(it) }
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
                if (target == null) {
                    onAdd(icon, title, description, days)
                } else {
                    onUpdate(target.id, icon, title, description, days)
                }
                showDialog = false
                editing = null
            }
        )
    }

    completing?.let { target ->
        EvidenceDialog(
            challenge = target,
            onDismiss = { completing = null },
            onConfirm = { evidencePath ->
                onComplete(target.id, evidencePath)
                completing = null
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = EcoCard,
            title = { Text("¿Eliminar reto?", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
            text = { Text("«${target.title}» se quitará de tu lista.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target.id)
                    pendingDelete = null
                }) { Text("Eliminar", color = EcoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar", color = EcoGreenDark) }
            }
        )
    }
}

/** Fila del historial: reto cumplido con su foto de evidencia y la fecha. */
@Composable
private fun CompletedChallengeRow(challenge: EcoChallenge) {
    val thumbnail = remember(challenge.evidencePath) { loadEvidence(challenge.evidencePath, 160) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(EcoGreenLight)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EcoCard),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail,
                    contentDescription = "Evidencia de ${challenge.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(challengeIcon(challenge.icon), contentDescription = null, tint = EcoGreen, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(challenge.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    if (challenge.completedMillis > 0) "Cumplido el ${DateUtils.formatDateTime(challenge.completedMillis)}" else "Cumplido",
                    fontSize = 11.sp,
                    color = EcoTextSecondary
                )
            }
        }
        Text("+${challenge.points} pts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreen)
    }
}

/**
 * Pide una foto como evidencia antes de dar el reto por cumplido.
 * La foto (de la cámara o de la galería) se guarda en files/evidence/<id>.jpg.
 */
@Composable
private fun EvidenceDialog(
    challenge: EcoChallenge,
    onDismiss: () -> Unit,
    onConfirm: (evidencePath: String) -> Unit
) {
    val context = LocalContext.current
    val photoFile = remember {
        File(context.filesDir, "evidence/${challenge.id}.jpg").also { it.parentFile?.mkdirs() }
    }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) preview = loadEvidence(photoFile.absolutePath, 600)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                photoFile.outputStream().use { output -> input.copyTo(output) }
            }
            preview = loadEvidence(photoFile.absolutePath, 600)
        }
    }

    fun cancel() {
        // Si no se envió, la foto no se conserva.
        photoFile.delete()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { cancel() },
        containerColor = EcoCard,
        title = { Text("Evidencia del reto", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Sube una foto que demuestre que cumpliste «${challenge.title}» para ganar +${challenge.points} EcoPoints.",
                    fontSize = 13.sp,
                    color = EcoTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EcoRow),
                    contentAlignment = Alignment.Center
                ) {
                    val image = preview
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = "Foto de evidencia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = EcoTextMuted, modifier = Modifier.size(40.dp))
                            Text("Aún no hay foto", fontSize = 12.sp, color = EcoTextMuted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                            try {
                                cameraLauncher.launch(uri)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara", fontSize = 12.sp, color = EcoGreenDark)
                    }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería", fontSize = 12.sp, color = EcoGreenDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(photoFile.absolutePath) },
                enabled = preview != null,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) { Text("Enviar evidencia") }
        },
        dismissButton = {
            TextButton(onClick = { cancel() }) { Text("Cancelar", color = EcoGreenDark) }
        }
    )
}

/** Carga la foto reducida (lado mayor ≈ maxSize px) para no gastar memoria. */
private fun loadEvidence(path: String, maxSize: Int): ImageBitmap? {
    if (path.isBlank() || !File(path).exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSize) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, options)?.asImageBitmap()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChallengeDialog(
    initial: EcoChallenge?,
    onDismiss: () -> Unit,
    onSave: (icon: String, title: String, description: String, days: Int) -> Unit
) {
    var icon by remember { mutableStateOf(initial?.icon ?: DEFAULT_ICON) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var days by remember { mutableStateOf(initial?.durationDays ?: 3) }
    var titleError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCard,
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
                    Text("Elige uno sugerido o escribe el tuyo", fontSize = 12.sp, color = EcoTextMuted)
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
                                    titleError = null
                                },
                                leadingIcon = {
                                    Icon(challengeIcon(s.icon), contentDescription = null, tint = EcoGreen, modifier = Modifier.size(16.dp))
                                },
                                label = { Text(s.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Nombre del reto") },
                    singleLine = true,
                    isError = titleError != null,
                    supportingText = if (titleError != null) {
                        { Text(titleError.orEmpty()) }
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
                        color = EcoTextMuted
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val error = Validators.challengeTitleError(title)
                    if (error != null) {
                        titleError = error
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
