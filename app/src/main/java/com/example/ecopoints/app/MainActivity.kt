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
                        onLoginSuccess = { email ->
                            prefs.setUserLoggedIn(true)
                            // Si el correo es el del registro, se conserva el nombre que el usuario
                            // eligió; solo se deriva del correo cuando no hay un nombre guardado.
                            val savedName = prefs.getUserName()
                            val savedEmail = prefs.getUserEmail()
                            val isKnownUser = savedName.isNotBlank() &&
                                (savedEmail.isBlank() || savedEmail == email.trim().lowercase())
                            val userName = if (isKnownUser) {
                                savedName
                            } else {
                                email.substringBefore("@").replaceFirstChar { it.uppercase() }
                            }
                            prefs.setUserName(userName)
                            prefs.setUserEmail(email)
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
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                    color = Color.Black,
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Text(
                                "Continuar como $savedUserName 🌿",
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
                        leadingIcon = { Text("📧") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Text("🔒") },
                        trailingIcon = {
                            val icon = if (passwordVisible) "🙈" else "👁"
                            Text(
                                icon,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { passwordVisible = !passwordVisible }
                                    .padding(6.dp)
                            )
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
                            modifier = Modifier.clickable { }
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDECEC))
                                .padding(8.dp)
                        ) {
                            Text("⚠️ ", fontSize = 12.sp)
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
                                else -> {
                                    errorMessage = ""
                                    onLoginSuccess(email)
                                }
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(
                            " o continúa con ",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("G", fontWeight = FontWeight.Bold, color = Color(0xFFDB4437), fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("f", fontWeight = FontWeight.Bold, color = Color(0xFF4267B2), fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿No tienes cuenta? ", fontSize = 13.sp, color = Color.Black)
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
}