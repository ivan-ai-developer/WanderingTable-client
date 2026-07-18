package ru.gohasoft.wanderingtable.core.uikit.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class WanderingTableExtendedColors(
    val caption: Color,
    val label: Color,
    val tint: Color,
    val tintBorder: Color,
    val goldTint: Color,
    val goldTintBorder: Color,
    val textOnGradient: Color,
)

val LightExtendedColors = WanderingTableExtendedColors(
    caption = LightCaption,
    label = LightLabel,
    tint = LightBackground,
    tintBorder = LightBorder,
    goldTint = LightGoldTint,
    goldTintBorder = LightGoldTintBorder,
    textOnGradient = LightTextOnGradient,
)

val DarkExtendedColors = WanderingTableExtendedColors(
    caption = DarkCaption,
    label = DarkLabel,
    tint = DarkTint,
    tintBorder = DarkTintBorder,
    goldTint = DarkGoldTint,
    goldTintBorder = DarkGoldTintBorder,
    textOnGradient = LightTextOnGradient,
)

val LocalWanderingTableExtendedColors = staticCompositionLocalOf { LightExtendedColors }
