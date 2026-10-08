package com.example.ecopoints.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Espaciado de EcoPoints: los mismos valores que ya usaban las pantallas, reunidos en un solo
 * lugar para que márgenes y separaciones sean iguales en toda la app.
 */
object EcoSpacing {
    /** Margen horizontal de las pantallas con tarjetas. */
    val Screen = 16.dp

    /** Separación vertical entre tarjetas. */
    val Section = 16.dp

    /** Relleno interno de una tarjeta. */
    val Card = 16.dp

    /** Separación entre elementos relacionados (campos de un formulario, filas de una lista). */
    val Item = 8.dp

    /** Separación mínima (icono y texto, título y subtítulo). */
    val Small = 4.dp

    /** Alto de los botones principales. */
    val Button = 44.dp

    /** Margen superior en pantallas completas (deja libre la barra de estado). */
    val TopInset = 50.dp
}
