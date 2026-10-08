package com.example.ecopoints.app.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ecopoints.app.ui.EcoViewModelFactory
import com.example.ecopoints.app.ui.auth.LoginScreen
import com.example.ecopoints.app.ui.auth.LoginViewModel
import com.example.ecopoints.app.ui.auth.RegisterScreen
import com.example.ecopoints.app.ui.auth.RegisterViewModel
import com.example.ecopoints.app.ui.home.HomeScreen
import com.example.ecopoints.app.ui.home.HomeViewModel
import com.example.ecopoints.app.ui.map.EcoMapScreen
import com.example.ecopoints.app.ui.map.MapViewModel
import com.example.ecopoints.app.ui.settings.SettingsScreen
import com.example.ecopoints.app.ui.settings.SettingsViewModel
import com.example.ecopoints.app.ui.welcome.WelcomeScreen
import com.example.ecopoints.app.ui.welcome.WelcomeViewModel

/** Rutas de la app. Los argumentos viajan en la ruta (correo escrito, si el usuario es nuevo). */
object Routes {
    const val EMAIL_ARG = "email"
    const val NEW_USER_ARG = "isNewUser"

    const val LOGIN = "login?$EMAIL_ARG={$EMAIL_ARG}"
    const val REGISTER = "register?$EMAIL_ARG={$EMAIL_ARG}"
    const val WELCOME = "welcome/{$NEW_USER_ARG}"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val MAP = "map"

    fun login(email: String = "") = "login?$EMAIL_ARG=${Uri.encode(email)}"
    fun register(email: String = "") = "register?$EMAIL_ARG=${Uri.encode(email)}"
    fun welcome(isNewUser: Boolean) = "welcome/$isNewUser"
}

/**
 * Mapa de navegación de EcoPoints (una sola Activity):
 * Login ⇄ Registro → Bienvenida → Panel principal → Ajustes / EcoMapa.
 */
@Composable
fun EcoNavHost(
    navController: NavHostController,
    viewModelFactory: EcoViewModelFactory
) {
    val context = LocalContext.current

    // Cerrar sesión: aviso, y se vuelve al login sin dejar pantallas atrás
    val goToLoginAfterLogout: () -> Unit = {
        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
        navController.navigate(Routes.login()) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(
            route = Routes.LOGIN,
            arguments = listOf(
                navArgument(Routes.EMAIL_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = viewModel,
                initialEmail = entry.arguments?.getString(Routes.EMAIL_ARG).orEmpty(),
                onLoggedIn = { isNewUser ->
                    navController.navigate(Routes.welcome(isNewUser)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onContinueSession = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onGoToRegister = { typedEmail -> navController.navigate(Routes.register(typedEmail)) }
            )
        }

        composable(
            route = Routes.REGISTER,
            arguments = listOf(
                navArgument(Routes.EMAIL_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val viewModel: RegisterViewModel = viewModel(factory = viewModelFactory)
            RegisterScreen(
                viewModel = viewModel,
                initialEmail = entry.arguments?.getString(Routes.EMAIL_ARG).orEmpty(),
                onRegistered = {
                    navController.navigate(Routes.welcome(true)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBackToLogin = { typedEmail ->
                    // Devuelve al login el correo que el usuario alcanzó a escribir
                    navController.navigate(Routes.login(typedEmail)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.WELCOME,
            arguments = listOf(navArgument(Routes.NEW_USER_ARG) { type = NavType.BoolType })
        ) { entry ->
            val viewModel: WelcomeViewModel = viewModel(factory = viewModelFactory)
            WelcomeScreen(
                viewModel = viewModel,
                isNewUser = entry.arguments?.getBoolean(Routes.NEW_USER_ARG) ?: false,
                onGoHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onLoggedOut = goToLoginAfterLogout
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = viewModel,
                onOpenMap = { navController.navigate(Routes.MAP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onLoggedOut = goToLoginAfterLogout
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAP) {
            val viewModel: MapViewModel = viewModel(factory = viewModelFactory)
            EcoMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
