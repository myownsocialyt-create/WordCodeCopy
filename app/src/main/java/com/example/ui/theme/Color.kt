package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ---- Word Search game palette ----
val GameBackground = Color(0xFFF0F4FF)
val GameBlue = Color(0xFF3B82F6)
val GameOrange = Color(0xFFF59E0B)
val GamePink = Color(0xFFEC4899)
val GameDarkText = Color(0xFF1E293B)
val GameSecondaryText = Color(0xFF64748B)
val GameCardBorder = Color(0xFFE2E8F0)
val GameSuccessGreen = Color(0xFF22C55E)
val GameGrayedFoundWord = Color(0xFF94A3B8)
val GameRedTimer = Color(0xFFDC2626)
val GameRedTimerBg = Color(0xFFFEE2E2)

/** Highlight colors cycled through as words are found (18 distinct hues). */
val WordHighlightColors: List<Color> = listOf(
    Color(0xFF3B82F6), // blue
    Color(0xFFEF4444), // red
    Color(0xFF22C55E), // green
    Color(0xFFF59E0B), // amber
    Color(0xFF8B5CF6), // violet
    Color(0xFFEC4899), // pink
    Color(0xFF06B6D4), // cyan
    Color(0xFFF97316), // orange
    Color(0xFF84CC16), // lime
    Color(0xFF6366F1), // indigo
    Color(0xFF14B8A6), // teal
    Color(0xFFD946EF), // fuchsia
    Color(0xFFEAB308), // yellow
    Color(0xFF0EA5E9), // sky
    Color(0xFFA855F7), // purple
    Color(0xFF10B981), // emerald
    Color(0xFFF43F5E), // rose
    Color(0xFF64748B)  // slate
)
