package com.example.yummydiary.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    secondary = PrimaryYellow,
    tertiary = PrimaryBlue,
    background = OnBackgroundDark,
    surface = OnBackgroundDark,
    onPrimary = Color.White,
    onSecondary = PrimaryDark,
    onTertiary = PrimaryDark,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color.LightGray,
    surfaceVariant = Color(0xFF555555) // Nieco jaśniejszy od tła 0xFF454545
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

@Composable
fun YummyDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color by default to use our custom palette
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
            // W trybie jasnym używamy PrimaryRed dla paska stanu, a w ciemnym koloru tła.
            val statusBarColor = if (darkTheme) colorScheme.background else colorScheme.primary
            window.statusBarColor = statusBarColor.toArgb()
            
            // Pasek nawigacji zazwyczaj pasuje do tła aplikacji.
            window.navigationBarColor = colorScheme.background.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            // isAppearanceLightStatusBars = true oznacza ciemne ikony (dla jasnego tła).
            // PrimaryRed (0xFFF09494) jest wystarczająco jasny, by wymagać ciemnych ikon.
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
