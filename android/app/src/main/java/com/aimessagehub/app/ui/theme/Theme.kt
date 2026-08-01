package com.aimessagehub.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF166B66),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F0E5),
    onPrimaryContainer = Color(0xFF073B38),
    secondary = Color(0xFF6A3FA0),
    onSecondary = Color.White,
    tertiary = Color(0xFFB24A2D),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EBF0),
    onSurface = Color(0xFF1B2430),
    outline = Color(0xFF7A8494),
)

@Composable
fun AiMessageHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content,
    )
}

