package com.cardvr.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Primary = Color(0xFF00C8FF)
val PrimaryDark = Color(0xFF0099CC)
val Background = Color(0xFF0A0E1A)
val Surface = Color(0xFF111827)
val SurfaceVariant = Color(0xFF1C2535)
val CardBackground = Color(0xFF161D2E)
val OnSurface = Color(0xFFE8EDF5)
val OnSurfaceVariant = Color(0xFF8B96A8)
val Error = Color(0xFFFF4444)
val RecordRed = Color(0xFFFF2D2D)
val LockGold = Color(0xFFFFB800)
val Success = Color(0xFF00D084)
val Warning = Color(0xFFFF8C00)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF001F2A),
    primaryContainer = Color(0xFF003544),
    onPrimaryContainer = Primary,
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF003549),
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    outline = Color(0xFF2A3547),
)

@Composable
fun CarDVRTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
