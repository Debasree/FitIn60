package com.fitin60.app.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Mint600,
    onPrimary = Color.White,
    primaryContainer = Mint300,
    onPrimaryContainer = Ink900,
    secondary = Ink800,
    onSecondary = Color.White,
    tertiary = Amber500,
    onTertiary = Color.White,
    background = SurfaceBotLight,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Ink100,
    onSurfaceVariant = Ink600,
    outline = Ink300,
    error = Coral500,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Mint500,
    onPrimary = Ink900,
    primaryContainer = Ink700,
    onPrimaryContainer = Mint300,
    secondary = Ink100,
    onSecondary = Ink900,
    tertiary = Amber500,
    onTertiary = Ink900,
    background = SurfaceTopDark,
    onBackground = Ink50,
    surface = Ink800,
    onSurface = Ink50,
    surfaceVariant = Ink700,
    onSurfaceVariant = Ink300,
    outline = Ink600,
    error = Coral500,
    onError = Ink900,
)

@Composable
fun Fitin60Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Fitin60Typography,
        content = content,
    )
}
