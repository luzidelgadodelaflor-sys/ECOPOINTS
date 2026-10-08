package com.example.ecopoints.app.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecopoints.app.R
import com.example.ecopoints.app.ui.components.petImageRes
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoGold
import com.example.ecopoints.app.ui.theme.EcoGoldContainer
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextSecondary

/** Bienvenida tras iniciar sesión o registrarse. Los datos salen de [WelcomeViewModel]. */
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    isNewUser: Boolean,
    onGoHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EcoGreen)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    "¡Bienvenido, ${state.userName}!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "¡Hola! Soy tu guardián ${state.petDisplayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = EcoGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Cuidaremos el medio ambiente juntos para ganar EcoPoints y subir de nivel.",
                            fontSize = 13.sp,
                            color = EcoTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(EcoCard)
                    .border(BorderStroke(2.dp, EcoGreenLight), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = petImageRes(state.petSpecies)),
                    contentDescription = "Mascota Elegida",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(220.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCard),
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
                        if (isNewUser) {
                            "Hemos preparado tu espacio ecológico"
                        } else {
                            "Tienes ${state.balance} EcoPoints esperando en tu alcancía verde"
                        },
                        fontSize = 12.sp,
                        color = EcoTextMuted
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
                                Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bono de bienvenida", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EcoGreenDark)
                                    Text("Acreditados a tu alcancía verde", fontSize = 11.sp, color = EcoTextSecondary)
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
                            .background(EcoGoldContainer)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = EcoGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    if (isNewUser) "Insignia Desbloqueada" else "Tu insignia actual",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EcoGold
                                )
                                Text("\"${state.level.badge}\"", fontSize = 11.sp, color = EcoTextSecondary)
                            }
                        }
                        Text("Nivel ${state.level.number}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EcoGold)
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
                        Text("Ir al Panel Principal", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            Icon(Icons.Filled.Settings, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajustes", fontSize = 12.sp, color = EcoGreenDark)
                        }

                        OutlinedButton(
                            onClick = { viewModel.logout(onLoggedOut) },
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
