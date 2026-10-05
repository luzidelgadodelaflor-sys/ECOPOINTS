package com.example.ecopoints.app.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.example.ecopoints.app.data.PreferencesManager

/** Tema elegido en Ajustes. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Preferencias de apariencia compartidas por todas las pantallas. Son estado observable:
 * al cambiarlas en Ajustes, las pantallas abiertas se redibujan al instante.
 */
object AppearanceSettings {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var largeText by mutableStateOf(false)
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        val prefs = PreferencesManager(context)
        themeMode = prefs.getThemeMode()
        largeText = prefs.isLargeTextEnabled()
        loaded = true
    }
}

/** Escala del texto cuando está activado "Texto grande". */
private const val LARGE_TEXT_SCALE = 1.15f

private fun colorSchemeFor(palette: EcoPalette, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    // Fondo y tarjetas coinciden con la paleta para que el texto sin color explícito
    // tome el color correcto (onBackground / onSurface) en ambos temas.
    return base.copy(
        primary = palette.green,
        onPrimary = Color.White,
        secondary = palette.greenDark,
        tertiary = palette.gold,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.card,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.row,
        onSurfaceVariant = palette.textSecondary,
        secondaryContainer = palette.greenLight,
        onSecondaryContainer = palette.greenDark,
        outline = palette.border,
        outlineVariant = palette.border,
        error = palette.error,
        surfaceContainerHigh = palette.card,
        surfaceContainerHighest = palette.row
    )
}

@Composable
fun EcoPointsTheme(content: @Composable () -> Unit) {
    AppearanceSettings.load(LocalContext.current)

    val dark = when (AppearanceSettings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkEcoPalette else LightEcoPalette

    // Iconos de la barra de estado claros u oscuros según el tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    val density = LocalDensity.current
    val textScale = if (AppearanceSettings.largeText) LARGE_TEXT_SCALE else 1f

    CompositionLocalProvider(
        LocalEcoPalette provides palette,
        LocalDensity provides Density(density.density, density.fontScale * textScale)
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette, dark),
            typography = Typography,
            content = content
        )
    }
}
