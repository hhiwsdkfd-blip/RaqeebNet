package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = CyanOnAccent,
    primaryContainer = CyberSurfaceHighlight,
    onPrimaryContainer = CyanAccent,
    secondary = EmeraldPulse,
    onSecondary = Color.Black,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldPulse,
    tertiary = ElectricBlue,
    error = AlertRed,
    errorContainer = AlertRedContainer,
    background = CyberBackground,
    onBackground = TextPrimaryDark,
    surface = CyberSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = CyberSurfaceHighlight
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF164E63),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = ElectricBlue,
    error = AlertRed,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline
)

@Composable
fun RaqeebNetTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = RaqeebNetTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
