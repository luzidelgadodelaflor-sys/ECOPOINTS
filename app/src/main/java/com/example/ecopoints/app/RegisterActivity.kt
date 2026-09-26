package com.example.ecopoints.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecopoints.app.data.PreferencesManager
import com.example.ecopoints.app.ui.theme.*

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(this)

        setContent {
            EcoPointsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EcoBackground) {
                    RegisterScreenContent(
                        onRegisterSuccess = { fullName, email, selectedPet, petName ->
                            prefs.setUserLoggedIn(true)
                            prefs.setUserName(fullName)
                            prefs.earnEcoPoints(50)
                            val fullPetInfo = if (petName.isNotBlank()) "$selectedPet ($petName)" else selectedPet
                            prefs.setPetLevel(fullPetInfo)

                            // Comunicación entre actividades mediante Intent con extras (Criterio P03)
                            val intent = Intent(this, WelcomeActivity::class.java).apply {
                                putExtra("EXTRA_USER_NAME", fullName)
                                putExtra("EXTRA_PET_LEVEL", fullPetInfo)
                                putExtra("EXTRA_SELECTED_PET", selectedPet)
                                putExtra("EXTRA_PET_NAME", petName)
                                putExtra("EXTRA_IS_NEW_USER", true)
                            }
                            startActivity(intent)
                            finish()
                        },
                        onBackToLogin = {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreenContent(
    onRegisterSuccess: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf("Koala") }

    val pets = listOf(
        Triple("Zorro", "🦊", "Zorro"),
        Triple("Panda", "🐼", "Panda"),
        Triple("Koala", "🐨", "Koala"),
        Triple("Gato", "🐱", "Gato"),
        Triple("Búho", "🦉", "Búho")
    )

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
                .padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_ecopoints),
                contentDescription = "Logo EcoPoints",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ELIGE TU COMPAÑERO GUÍA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
                Text("1 seleccionado", fontSize = 13.sp, color = EcoGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                pets.forEach { (name, emoji, _) ->
                    val isSelected = selectedPet == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EcoGreenLight else Color.White)
                            .border(
                                BorderStroke(1.5.dp, if (isSelected) EcoGreen else Color.LightGray),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedPet = name }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) EcoGreenDark else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    Text("Crear cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre de usuario") },
                        leadingIcon = { Text("👤") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Nombre para tu mascota") },
                        leadingIcon = { Text("🐾") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Text("📧") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Text("🔒") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = { Text("🛡️") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = EcoGreen)
                        )
                        Text("Acepto los Términos de servicio", fontSize = 12.sp, color = Color.Gray)
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            errorMessage,
                            color = EcoError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() || email.isBlank() || password.isBlank() ->
                                    errorMessage = "Completa todos los campos principales"
                                password.length < 8 ->
                                    errorMessage = "Mínimo 8 caracteres en contraseña"
                                password != confirmPassword ->
                                    errorMessage = "Las contraseñas no coinciden"
                                !termsAccepted ->
                                    errorMessage = "Acepta los términos y condiciones"
                                else -> {
                                    errorMessage = ""
                                    onRegisterSuccess(fullName, email, selectedPet, petName)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                    ) {
                        Text("Crear cuenta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿Ya tienes cuenta? ", fontSize = 13.sp, color = Color.Black)
                Text(
                    "Iniciar sesión",
                    color = EcoGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
    }
}
