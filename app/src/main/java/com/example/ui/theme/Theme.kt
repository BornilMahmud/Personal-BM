package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFF10B981),
    onTertiary = ObsidianDark,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF43F5E),
    onError = Color.White,
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6),
    background = ObsidianDark,
    onBackground = Color(0xFFF8FAFC),
    surface = DeepNavy,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = SlateSurfaceDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = SlateBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueSoft,
    onPrimaryContainer = RoyalBlueDark,
    secondary = SlateTextPrimary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = SlateTextPrimary,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSoft,
    onTertiaryContainer = EmeraldSuccess,
    error = CrimsonDanger,
    onError = Color.White,
    errorContainer = CrimsonSoft,
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
fun PersonalBMTheme(
    themeMode: String = "light",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "system" -> isSystemInDarkTheme()
        else -> false // Default is Minimal Light Fintech
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
