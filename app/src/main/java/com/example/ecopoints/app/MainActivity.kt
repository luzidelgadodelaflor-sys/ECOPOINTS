package com.example.ecopoints.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)

        // Datos recibidos por Intent: correo escrito en RegisterActivity y aviso de cierre de sesión
        val prefillEmail = intent.getStringExtra(IntentExtras.EMAIL).orEmpty()
        if (savedInstanceState == null && intent.getBooleanExtra(IntentExtras.LOGGED_OUT, false)) {
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        }

        // Sesión guardada en SharedPreferences (RF-01): el login sigue siendo la pantalla inicial,
        // pero ofrece continuar sin volver a escribir las credenciales.
        val savedUserName = if (prefs.isUserLoggedIn()) prefs.getUserName().ifBlank { null } else null

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    LoginForm(
                        initialEmail = prefillEmail,
                        savedUserName = savedUserName,
                        onContinueSession = {
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra(IntentExtras.USER_NAME, prefs.getUserName())
                                putExtra(IntentExtras.PET_LEVEL, prefs.getPetLevel())
                            }
                            startActivity(intent)
                            finish()
                        },
                        onLogin = login@{ email, password ->
                            // Solo entra la cuenta registrada en este dispositivo y con su contraseña
                            if (!prefs.isAccountEmail(email)) {
                                return@login "No hay una cuenta con ese correo. Regístrate primero."
                            }
                            if (prefs.hasPassword() && !prefs.checkPassword(password)) {
                                return@login "Contraseña incorrecta"
                            }
                            // Cuentas creadas antes de guardar contraseñas: la primera que usen queda como suya
                            if (!prefs.hasPassword()) prefs.savePassword(password)

                            prefs.setUserLoggedIn(true)
                            val userName = prefs.getUserName()
                            val isFirstTime = prefs.getEcoPointsHistorical() == 0
                            if (isFirstTime) {
                                prefs.earnEcoPoints(50)
                                prefs.setPetLevel("Koala")
                            }

                            // Comunicación con WelcomeActivity mediante Intent (Criterio P03)
                            val intent = Intent(this, WelcomeActivity::class.java).apply {
                                putExtra(IntentExtras.USER_NAME, userName)
                                putExtra(IntentExtras.PET_LEVEL, prefs.getPetLevel())
                                putExtra(IntentExtras.IS_NEW_USER, isFirstTime)
                            }
                            startActivity(intent)
                            finish()
                            null
                        },
                        onResetPassword = reset@{ userName, email, newPassword ->
                            // Sin servidor de correo: se verifica con el nombre de usuario y el correo registrados
                            val nameMatches = userName.trim().equals(prefs.getUserName().trim(), ignoreCase = true)
                            if (!prefs.isAccountEmail(email) || !nameMatches) {
                                return@reset "El nombre de usuario y el correo no coinciden con la cuenta registrada"
                            }
                            prefs.savePassword(newPassword)
                            Toast.makeText(this, "Contraseña actualizada. Ya puedes iniciar sesión.", Toast.LENGTH_SHORT).show()
                            null
                        },
                        onNavigateToRegister = { typedEmail ->
                            // Comunicación con RegisterActivity: se le pasa el correo ya escrito
                            val intent = Intent(this, RegisterActivity::class.java).apply {
                                putExtra(IntentExtras.EMAIL, typedEmail)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    initialEmail: String,
    savedUserName: String?,
    onContinueSession: () -> Unit,
    /** Devuelve el mensaje de error, o null si inició sesión. */
    onLogin: (email: String, password: String) -> String?,
    /** Devuelve el mensaje de error, o null si se cambió la contraseña. */
    onResetPassword: (userName: String, email: String, newPassword: String) -> String?,
    onNavigateToRegister: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

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
                    modifier = Modifier
                        .size(140.dp)
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

                    if (savedUserName != null) {
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
                                "Continuar como $savedUserName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EcoGreenDark
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = EcoGreen) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = EcoGreen) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = EcoTextMuted
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
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

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EcoErrorContainer)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = EcoError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(errorMessage, color = EcoError, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() ->
                                    errorMessage = "Completa correo y contraseña"
                                !isValidEmail(email) ->
                                    errorMessage = "Ingresa un correo electrónico válido"
                                password.length < 8 ->
                                    errorMessage = "Mínimo 8 caracteres en contraseña"
                                else -> errorMessage = onLogin(email, password).orEmpty()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                    ) {
                        Text("Iniciar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

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
                            modifier = Modifier.clickable { onNavigateToRegister(email) }
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            initialEmail = email,
            onDismiss = { showResetDialog = false },
            onReset = { userName, resetEmail, newPassword ->
                onResetPassword(userName, resetEmail, newPassword).also { error ->
                    if (error == null) {
                        showResetDialog = false
                        email = resetEmail
                        password = ""
                        errorMessage = ""
                    }
                }
            }
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
    onDismiss: () -> Unit,
    onReset: (userName: String, email: String, newPassword: String) -> String?
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCard,
        title = { Text("Recuperar contraseña", color = EcoGreenDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Confirma tu nombre de usuario y tu correo, y elige una contraseña nueva.",
                    fontSize = 13.sp,
                    color = EcoTextSecondary
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre de usuario") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = EcoGreen) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = EcoGreen) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Contraseña nueva") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = EcoGreen) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = EcoGreen) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Text(error, color = EcoError, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = when {
                        userName.isBlank() || email.isBlank() || newPassword.isBlank() -> "Completa todos los campos"
                        newPassword.length < 8 -> "Mínimo 8 caracteres en contraseña"
                        newPassword != confirmPassword -> "Las contraseñas no coinciden"
                        else -> onReset(userName, email, newPassword).orEmpty()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) { Text("Cambiar contraseña") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = EcoGreenDark) }
        }
    )
}