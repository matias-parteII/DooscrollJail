package com.doomscrolljail.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5252),
    onPrimary = Color(0xFF1A0000),
    primaryContainer = Color(0xFF4A1212),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF261900),
    background = Color(0xFF0E1014),
    onBackground = Color(0xFFECEEF2),
    surface = Color(0xFF171A21),
    onSurface = Color(0xFFECEEF2),
    surfaceVariant = Color(0xFF222732),
    onSurfaceVariant = Color(0xFFB8C0CC),
    error = Color(0xFFFF6E6E)
)

@Composable
fun DoomscrollJailTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
