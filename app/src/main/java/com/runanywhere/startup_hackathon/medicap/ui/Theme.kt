package com.runanywhere.startup_hackathon.medicap.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette: bluish-pinkish-green vibe WITHOUT blue dominance
private val Mint = Color(0xFF2EC4B6)
private val SoftPink = Color(0xFFE8A6B8)
private val Ink = Color(0xFF121417)
private val Paper = Color(0xFFF7F7FA)
private val Card = Color(0xFFFFFFFF)
private val Muted = Color(0xFF6B7280)

private val LightColors = lightColorScheme(
    primary = Mint,
    secondary = SoftPink,
    background = Paper,
    surface = Card,
    onPrimary = Color.White,
    onSecondary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    outline = Color(0xFFE5E7EB)
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    secondary = SoftPink,
    background = Color(0xFF0E1114),
    surface = Color(0xFF151A1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
    outline = Color(0xFF2A3138)
)

@Composable
fun MediCapTheme(
    darkTheme: Boolean = false, // light-first by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
