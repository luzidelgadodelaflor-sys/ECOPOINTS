package com.example.ecopoints.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores de EcoPoints para el tema claro y el oscuro. Las pantallas usan los
 * nombres de abajo (EcoGreen, EcoBackground, EcoCard...) y reciben automáticamente
 * la versión del tema activo.
 */
@Immutable
data class EcoPalette(
    val green: Color,
    val greenDark: Color,
    val greenLight: Color,
    val background: Color,
    val card: Color,
    val row: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val track: Color,
    val error: Color,
    val errorContainer: Color,
    val gold: Color,
    val goldContainer: Color
)

val LightEcoPalette = EcoPalette(
    green = Color(0xFF2E7D32),
    greenDark = Color(0xFF1B5E20),
    greenLight = Color(0xFFE9F7EF),
    background = Color(0xFFF1F8F2),
    card = Color(0xFFFFFFFF),
    row = Color(0xFFF9FBF9),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF444444),
    textMuted = Color(0xFF888888),
    border = Color(0xFFCCCCCC),
    track = Color(0xFFE0E0E0),
    error = Color(0xFFB00020),
    errorContainer = Color(0xFFFDECEC),
    gold = Color(0xFFB8860B),
    goldContainer = Color(0xFFFFF9E6)
)

val DarkEcoPalette = EcoPalette(
    green = Color(0xFF43A047),
    greenDark = Color(0xFFA5D6A7),
    greenLight = Color(0xFF1F3A26),
    background = Color(0xFF0F1A12),
    card = Color(0xFF1C2A1F),
    row = Color(0xFF243328),
    textPrimary = Color(0xFFE8F0E9),
    textSecondary = Color(0xFFC4CFC5),
    textMuted = Color(0xFF94A396),
    border = Color(0xFF3A4A3D),
    track = Color(0xFF3A4A3D),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF3A1F22),
    gold = Color(0xFFE0B84A),
    goldContainer = Color(0xFF3A3320)
)

val LocalEcoPalette = staticCompositionLocalOf { LightEcoPalette }

val EcoGreen: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.green
val EcoGreenDark: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.greenDark
val EcoGreenLight: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.greenLight
val EcoBackground: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.background
val EcoCard: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.card
val EcoRow: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.row
val EcoTextPrimary: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.textPrimary
val EcoTextSecondary: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.textSecondary
val EcoTextMuted: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.textMuted
val EcoBorder: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.border
val EcoTrack: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.track
val EcoError: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.error
val EcoErrorContainer: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.errorContainer
val EcoGold: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.gold
val EcoGoldContainer: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.goldContainer

// Iguales en ambos temas
val EcoStreak = Color(0xFFFF9800)
val EcoHappiness = Color(0xFFE91E63)
