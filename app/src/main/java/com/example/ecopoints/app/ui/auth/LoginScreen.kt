package com.example.ecopoints.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecopoints.app.R
import com.example.ecopoints.app.ui.components.EcoPrimaryButton
import com.example.ecopoints.app.ui.components.EcoTextField
import com.example.ecopoints.app.ui.components.ErrorBanner
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoTextPrimary
import com.example.ecopoints.app.ui.theme.EcoTextSecondary

/**
 * Pantalla de inicio de sesión. Mantiene el diseño de siempre; ahora solo dibuja el estado de
 * [LoginViewModel] y le avisa de lo que escribe el usuario.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    initialEmail: String,
    onLoggedIn: (isNewUser: Boolean) -> Unit,
    onContinueSession: () -> Unit,
    onGoToRegister: (typedEmail: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoggedIn -> onLoggedIn(event.isNewUser)
                is LoginEvent.PasswordUpdated -> {
                    showResetDialog = false
                    viewModel.clearResetState()
                    email = event.email
                    password = ""
                    Toast.makeText(
                        context,
                        "Contraseña actualizada. Ya puedes iniciar sesión.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_ecopoints),
                    contentDescription = "Logo EcoPoints",
                    modifier = Modifier.size(130.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Pequeñas acciones, grandes cambios",
                    fontSize = 15.sp,
                    color = EcoTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(15.dp))
                Image(
                    painter = painterResource(id = R.drawable.mascota_ecopoints),
                    contentDescription = "Mascota perezoso de EcoPoints",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(140.dp)
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(26.dp)) {
                    Text(
                        "Iniciar sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val savedName = state.savedUserName
                    if (savedName != null) {
                        OutlinedButton(
                            onClick = onContinueSession,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, EcoGreen)
                        ) {
                            Icon(Icons.Filled.Eco, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Continuar como $savedName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EcoGreenDark
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    EcoTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.onInputChanged()
                        },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    EcoTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.onInputChanged()
                        },
                        label = "Contraseña",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onDone = { viewModel.login(email, password) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            fontSize = 13.sp,
                            color = EcoGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showResetDialog = true }
                        )
                    }

                    val error = state.error
                    if (error != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        ErrorBanner(error)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EcoPrimaryButton(
                        text = "Iniciar sesión",
                        onClick = { viewModel.login(email, password) },
                        isLoading = state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿No tienes cuenta? ", fontSize = 13.sp, color = EcoTextPrimary)
                        Text(
                            "Regístrate",
                            color = EcoGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onGoToRegister(email) }
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            initialEmail = email,
            isLoading = state.resetLoading,
            error = state.resetError,
            onInputChanged = viewModel::onResetInputChanged,
            onDismiss = {
                showResetDialog = false
                viewModel.clearResetState()
            },
            onConfirm = viewModel::resetPassword
        )
    }
}

/**
 * Recuperar contraseña sin servidor: se confirma la identidad con el nombre de
 * usuario y el correo registrados, y se elige una contraseña nueva.
 */
@Composable
private fun ResetPasswordDialog(
    initialEmail: String,
    isLoading: Boolean,
    error: String?,
    onInputChanged: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (userName: String, email: String, newPassword: String, confirmPassword: String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCard,
        title = { Text("Recuperar contraseña", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Confirma tu nombre de usuario y tu correo, y elige una contraseña nueva.",
                    fontSize = 13.sp,
                    color = EcoTextSecondary
                )
                EcoTextField(
                    value = userName,
                    onValueChange = { userName = it; onInputChanged() },
                    label = "Nombre de usuario",
                    leadingIcon = Icons.Outlined.Person
                )
                EcoTextField(
                    value = email,
                    onValueChange = { email = it; onInputChanged() },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email
                )
                EcoTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; onInputChanged() },
                    label = "Contraseña nueva",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true
                )
                EcoTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; onInputChanged() },
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onDone = { onConfirm(userName, email, newPassword, confirmPassword) }
                )
                if (error != null) {
                    Text(error, color = EcoError, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(userName, email, newPassword, confirmPassword) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) { Text("Cambiar contraseña") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = EcoGreenDark) }
        }
    )
}
