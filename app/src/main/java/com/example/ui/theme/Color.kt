package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Holds whether Dark & Orange mode is currently active so accent tokens
 * automatically switch between Minimal Light Blue (Light) and Vibrant Orange (Dark).
 */
object ActiveThemeState {
    var isDark by mutableStateOf(false)
}

// ============================================================================
// PERSONAL BM — MINIMAL LIGHT (WHITE DEFAULT) & DARK + ORANGE DESIGN TOKENS
// ============================================================================

// Minimal Light Surfaces & Backgrounds (Default White Theme)
val LightBackground = Color(0xFFF8FAFC)       // Primary background: #F8FAFC
val LightSurface = Color(0xFFFFFFFF)          // Card background: #FFFFFF
val LightSurfaceVariant = Color(0xFFF1F5F9)   // Muted surface: #F1F5F9
val LightBorder = Color(0xFFE2E8F0)           // Border: #E2E8F0

// Minimal Light Typography Colors
val SlateTextPrimary = Color(0xFF0F172A)      // Primary text: #0F172A
val SlateTextSecondary = Color(0xFF64748B)    // Secondary text: #64748B
val SlateTextMuted = Color(0xFF94A3B8)

// Light Theme Blue Tokens
val LightRoyalBlue = Color(0xFF2563EB)        // Light primary accent: #2563EB
val LightRoyalBlueDark = Color(0xFF1D4ED8)    // Light accent hover/active: #1D4ED8
val LightRoyalBlueSoft = Color(0xFFEFF6FF)    // Light subtle blue tint

// Dark & Orange Theme Tokens
val DarkOrangePrimary = Color(0xFFFF6B00)     // Vibrant Orange primary in Dark mode: #FF6B00
val DarkOrangeBright = Color(0xFFFF8533)      // Luminous Orange secondary accent: #FF8533
val DarkOrangeDark = Color(0xFFE65100)        // Deep Orange active state: #E65100
val DarkOrangeSoft = Color(0xFF3B1D06)        // Warm dark orange container tint: #3B1D06

// Reactive Primary Accent Tokens (Blue in White Theme, Orange in Dark Theme)
val RoyalBlue: Color
    get() = if (ActiveThemeState.isDark) DarkOrangePrimary else LightRoyalBlue

val RoyalBlueDark: Color
    get() = if (ActiveThemeState.isDark) DarkOrangeDark else LightRoyalBlueDark

val RoyalBlueSoft: Color
    get() = if (ActiveThemeState.isDark) DarkOrangeSoft else LightRoyalBlueSoft

val VibrantCyan: Color
    get() = if (ActiveThemeState.isDark) DarkOrangeBright else Color(0xFF0284C7)

val CyanLight: Color
    get() = if (ActiveThemeState.isDark) DarkOrangeSoft else Color(0xFFF0F9FF)

val EmeraldSuccess = Color(0xFF059669)        // Income/success: #059669
val EmeraldSoft: Color
    get() = if (ActiveThemeState.isDark) Color(0xFF063E2E) else Color(0xFFECFDF5)

val CrimsonDanger = Color(0xFFE11D48)         // Expense/danger: #E11D48
val CrimsonSoft: Color
    get() = if (ActiveThemeState.isDark) Color(0xFF4C0D1D) else Color(0xFFFFF1F2)

val AmberWarning = Color(0xFFD97706)          // Budget warning
val AmberSoft: Color
    get() = if (ActiveThemeState.isDark) Color(0xFF452205) else Color(0xFFFFFBEB)

// Dark & Orange Surfaces & Borders
val ObsidianDark = Color(0xFF0D0A08)          // Warm obsidian dark background: #0D0A08
val DeepNavy = Color(0xFF17120E)              // Elevated dark-orange charcoal card: #17120E
val SlateSurfaceDark = Color(0xFF241C16)      // Dark muted warm surface: #241C16
val SlateBorderDark = Color(0xFF3D2E22)       // Warm bronze-orange dark border: #3D2E22

// Bangladeshi Wallet Brand Accents
val BkashPink = Color(0xFFE2136E)
val NagadOrange = Color(0xFFF7941D)
val RocketPurple = Color(0xFF8C3494)
val UpayBlue = Color(0xFF0072BC)
val BankIndigo = Color(0xFF2563EB)
val CashEmerald = Color(0xFF059669)

