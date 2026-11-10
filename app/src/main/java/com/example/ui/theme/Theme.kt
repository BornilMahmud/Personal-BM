package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = DarkOrangeSoft,
    onPrimaryContainer = Color(0xFFFFDBC2),
    secondary = DarkOrangeBright,
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFF2E1909),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFFF59E0B),
    onTertiary = ObsidianDark,
    tertiaryContainer = Color(0xFF452205),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFF43F5E),
    onError = Color.White,
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6),
    background = ObsidianDark,
    onBackground = Color(0xFFFAF8F5),
    surface = DeepNavy,
    onSurface = Color(0xFFFAF8F5),
    surfaceVariant = SlateSurfaceDark,
    onSurfaceVariant = Color(0xFFD6C7BC),
    outline = SlateBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = LightRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = LightRoyalBlueSoft,
    onPrimaryContainer = LightRoyalBlueDark,
    secondary = SlateTextPrimary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = SlateTextPrimary,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFECFDF5),
    onTertiaryContainer = EmeraldSuccess,
    error = CrimsonDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFFF1F2),
    onErrorContainer = CrimsonDanger,
    background = LightBackground,
    onBackground = SlateTextPrimary,
    surface = LightSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = LightBorder
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),      // Input & compact button radius: 10dp
    medium = RoundedCornerShape(16.dp),     // Card radius: 16dp
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun isEffectiveDarkTheme(themeMode: String = "light"): Boolean {
    val systemDark = isSystemInDarkTheme()
    return when (themeMode.lowercase()) {
        "dark" -> true
        "system" -> systemDark
        else -> false // Default is white/light Minimal Light Fintech theme
    }
}

@Composable
fun AppTheme(
    themeMode: String = "light",
    darkTheme: Boolean = isEffectiveDarkTheme(themeMode),
    content: @Composable () -> Unit
) {
    ActiveThemeState.isDark = darkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun PersonalBMTheme(
    themeMode: String = "light",
    content: @Composable () -> Unit
) {
    AppTheme(
        themeMode = themeMode,
        content = content
    )
}

