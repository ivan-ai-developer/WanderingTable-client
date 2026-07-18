package ru.gohasoft.wanderingtable.core.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WanderingTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) WanderingTableDarkColorScheme else WanderingTableLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalWanderingTableExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WanderingTableTypography,
            content = content,
        )
    }
}

val MaterialTheme.extendedColors: WanderingTableExtendedColors
    @Composable
    get() = LocalWanderingTableExtendedColors.current
