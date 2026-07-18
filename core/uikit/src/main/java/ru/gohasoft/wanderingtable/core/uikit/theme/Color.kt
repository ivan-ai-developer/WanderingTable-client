package ru.gohasoft.wanderingtable.core.uikit.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand
val Purple900 = Color(0xFF332960)
val Purple800 = Color(0xFF3F386E)
val Purple600 = Color(0xFF5E5497)
val Gold = Color(0xFFE8A33D)
val Destructive = Color(0xFFC0503D)

// Light neutrals/surfaces
val LightInk = Color(0xFF1B1830)
val LightBackground = Color(0xFFF3F1FA)
val LightSurface = Color(0xFFF9F5FF)
val LightSurfaceBorder = Color(0xFFD7CCF2)
val LightBorder = Color(0xFFE4DEF7)
val LightMuted = Color(0xFF6B6485)
val LightCaption = Color(0xFF8B84A8)
val LightLabel = Purple800
val LightGoldTint = Color(0xFFFFF7E9)
val LightGoldTintBorder = Color(0xFFF0D6A0)
val LightTextOnGradient = Color(0xFFE7E4F5)

// Dark neutrals/surfaces
val DarkBackground = Color(0xFF1B1421)
val DarkInk = Color(0xFFF5F2FF)
val DarkSurface = Color(0xFF241E3F)
val DarkSurfaceBorder = Color(0x29FFFFFF)
val DarkBorder = Color(0x24FFFFFF)
val DarkTint = Color(0xFF2A2444)
val DarkTintBorder = Color(0x29FFFFFF)
val DarkMuted = Color(0xFFA79FC8)
val DarkCaption = Color(0xFF8A81AC)
val DarkLabel = Color(0xFFC6BEE8)
val DarkGoldTint = Color(0xFF332A18)
val DarkGoldTintBorder = Color(0x59E8A33D)

val WanderingTableLightColorScheme = lightColorScheme(
    primary = Purple600,
    onPrimary = Color.White,
    primaryContainer = Purple800,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Purple900,
    error = Destructive,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    outlineVariant = LightSurfaceBorder,
)

val WanderingTableDarkColorScheme = darkColorScheme(
    primary = Purple600,
    onPrimary = Color.White,
    primaryContainer = Purple800,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Purple900,
    error = Destructive,
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    outlineVariant = DarkSurfaceBorder,
)
