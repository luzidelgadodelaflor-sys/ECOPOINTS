package com.example.ecopoints.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecopoints.app.R
import com.example.ecopoints.app.data.model.PetSpecies
import com.example.ecopoints.app.ui.components.EcoPrimaryButton
import com.example.ecopoints.app.ui.components.EcoTextField
import com.example.ecopoints.app.ui.components.petImageRes
import com.example.ecopoints.app.ui.theme.EcoBackground
import com.example.ecopoints.app.ui.theme.EcoBorder
import com.example.ecopoints.app.ui.theme.EcoCard
import com.example.ecopoints.app.ui.theme.EcoError
import com.example.ecopoints.app.ui.theme.EcoGreen
import com.example.ecopoints.app.ui.theme.EcoGreenDark
import com.example.ecopoints.app.ui.theme.EcoGreenLight
import com.example.ecopoints.app.ui.theme.EcoTextMuted
import com.example.ecopoints.app.ui.theme.EcoTextPrimary

/** Registro de cuenta: elige compañero, escribe los datos y crea la cuenta con [RegisterViewModel]. */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    initialEmail: String,
    onRegistered: () -> Unit,
    onBackToLogin: (typedEmail: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var petName by rememberSaveable { mutableStateOf("") }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var selectedPet by rememberSaveable { mutableStateOf(PetSpecies.DEFAULT) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.Registered -> onRegistered()
            }
        }
    }

    val submit = {
        viewModel.register(fullName, petName, email, password, confirmPassword, termsAccepted, selectedPet)
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
                PetSpecies.ALL.forEach { name ->
                    val isSelected = selectedPet == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EcoGreenLight else EcoCard)
                            .border(
                                BorderStroke(1.5.dp, if (isSelected) EcoGreen else EcoBorder),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedPet = name }
                            .padding(vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = petImageRes(name)),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) EcoGreenDark else EcoTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    Text("Crear cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)

                    Spacer(modifier = Modifier.height(10.dp))

                    EcoTextField(
                        value = fullName,
                        onValueChange = { fullName = it; viewModel.onInputChanged() },
                        label = "Nombre de usuario",
                        leadingIcon = Icons.Outlined.Person
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EcoTextField(
                        value = petName,
                        onValueChange = { petName = it; viewModel.onInputChanged() },
                        label = "Nombre para tu mascota",
                        leadingIcon = Icons.Outlined.Pets
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EcoTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.onInputChanged() },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EcoTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.onInputChanged() },
                        label = "Contraseña",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EcoTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; viewModel.onInputChanged() },
                        label = "Confirmar contraseña",
                        leadingIcon = Icons.Outlined.VerifiedUser,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onDone = { submit() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it; viewModel.onInputChanged() },
                            colors = CheckboxDefaults.colors(checkedColor = EcoGreen)
                        )
                        Text("Acepto los Términos de servicio", fontSize = 12.sp, color = EcoTextMuted)
                    }

                    val error = state.error
                    if (error != null) {
                        Text(
                            error,
                            color = EcoError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EcoPrimaryButton(
                        text = "Crear cuenta",
                        onClick = { submit() },
                        isLoading = state.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿Ya tienes cuenta? ", fontSize = 13.sp, color = EcoTextPrimary)
                Text(
                    "Iniciar sesión",
                    color = EcoGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onBackToLogin(email) }
                )
            }
        }
    }
}
