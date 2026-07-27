package com.nexuzstudios.qrcodescanner_quickscan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Classic dark theme colors ────────────────────────────────────────────────
// Muted, professional palette — no neon, no aurora
val AccentBlue    = Color(0xFF4A90D9)   // Calm, professional blue
val DarkBackground = Color(0xFF121212)  // Standard Material dark background
val SurfaceDark    = Color(0xFF1E1E1E)  // Elevated surface
val CardDark       = Color(0xFF252525)  // Card surface
val ScannerOverlay = Color(0xCC121212)
val ErrorRed       = Color(0xFFCF6679)  // Material dark error

// Keep NeonBlue as alias so existing references don't break — but it now maps to AccentBlue
val NeonBlue      = AccentBlue
val NeonBlueAlpha = AccentBlue.copy(alpha = 0.2f)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF263238),
    onSecondaryContainer = Color(0xFFB2DFDB),
    background = DarkBackground,
    onBackground = Color(0xFFE0E0E0),
    surface = SurfaceDark,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = CardDark,
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF424242),
    error = ErrorRed,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D40),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFEEEEEE),
    outline = Color(0xFFBDBDBD)
)

@Composable
fun QRCodeScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
