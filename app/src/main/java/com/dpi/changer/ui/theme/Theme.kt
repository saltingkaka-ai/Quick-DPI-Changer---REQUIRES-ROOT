package com.dpi.changer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Liquid Glass Color Palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6B8DD6),
    secondary = Color(0xFF9DB3D9),
    tertiary = Color(0xFFB8C9E8),
    background = Color(0xFF0A0F1C),
    surface = Color(0xFF121A2B),
    surfaceVariant = Color(0xFF1E2940),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E6F1),
    onSurface = Color(0xFFE0E6F1),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A6FA5),
    secondary = Color(0xFF7B9BD1),
    tertiary = Color(0xFFA8C0E8),
    background = Color(0xFFF0F4FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1F2E),
    onSurface = Color(0xFF1A1F2E),
)

@Composable
fun DPIChangerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}