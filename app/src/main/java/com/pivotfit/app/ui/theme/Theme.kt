package com.pivotfit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PivotColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFB6FF3B),
    onPrimary = Color(0xFF101211),
    secondary = Color(0xFFFF7A1A),
    onSecondary = Color(0xFF101211),
    tertiary = Color(0xFF62F5FF),
    background = Color(0xFF101211),
    onBackground = Color(0xFFF7F7F0),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFF7F7F0),
    surfaceVariant = Color(0xFF252A27),
    onSurfaceVariant = Color(0xFFD8DDD4),
    error = Color(0xFFFF6B6B)
)

@Composable
fun PivotFitTheme(content: @Composable () -> Unit) {
    val ignored = isSystemInDarkTheme()
    MaterialTheme(colorScheme = PivotColors, typography = MaterialTheme.typography, content = content)
}
