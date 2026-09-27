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
    primary = NetisCyanAccent,
    onPrimary = Color(0xFF003258),
    primaryContainer = NetisBlueDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = NetisTeal,
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = NetisPurple,
    onTertiary = Color.White,
    background = NetisDarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = NetisDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NetisDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = NetisDarkBorder,
    error = NetisDanger
)

private val LightColorScheme = lightColorScheme(
    primary = NetisBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = NetisTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC2E8FF),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = NetisPurple,
    onTertiary = Color.White,
    background = NetisLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = NetisLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = NetisLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = NetisLightBorder,
    error = NetisDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Netis branding vibrant by default
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

