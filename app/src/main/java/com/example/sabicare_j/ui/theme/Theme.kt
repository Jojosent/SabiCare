package com.example.sabicare_j.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.sabicare_j.utils.ThemeManager

private val LightColors = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,
    secondary = Coral500,
    onSecondary = Color.White,
    secondaryContainer = Coral100,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = Indigo500,
    tertiaryContainer = Indigo100,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    errorContainer = ErrorRedSoft,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Teal900,
    onPrimaryContainer = Teal100,
    secondary = Coral500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Coral100,
    tertiary = Indigo500,
    tertiaryContainer = Color(0xFF312E81),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorRed,
    errorContainer = Color(0xFF7F1D1D),
    onError = Color.White
)

@Composable
fun SabiCareTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Initialise theme state on first composition so it's safe in previews too.
    val context = LocalContext.current
    LaunchedEffect(Unit) { ThemeManager.init(context) }

    val themePref by ThemeManager.state
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themePref) {
        "light" -> false
        "dark"  -> true
        else    -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SabiTypography,
        content = content
    )
}
