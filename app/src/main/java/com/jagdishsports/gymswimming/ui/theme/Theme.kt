package com.jagdishsports.gymswimming.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = DeepOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = NavyBlue,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = SportGreen,
    background = CoolSurface,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7ECF2),
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = BrightOrange,
    secondary = androidx.compose.ui.graphics.Color(0xFF9DB7D7),
    tertiary = androidx.compose.ui.graphics.Color(0xFF8FD6B1),
    background = androidx.compose.ui.graphics.Color(0xFF111827),
    surface = androidx.compose.ui.graphics.Color(0xFF182235),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF26344A),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
)

@Composable
fun JagdishSportsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
