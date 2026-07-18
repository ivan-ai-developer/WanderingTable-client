package ru.gohasoft.wanderingtable.core.uikit.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.R

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
    Font(R.font.barlow_condensed_extrabold, FontWeight.ExtraBold),
)

@OptIn(ExperimentalTextApi::class)
private fun manropeFont(weight: FontWeight) = Font(
    resId = R.font.manrope,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Manrope = FontFamily(
    manropeFont(FontWeight.Normal),
    manropeFont(FontWeight.Medium),
    manropeFont(FontWeight.SemiBold),
    manropeFont(FontWeight.Bold),
    manropeFont(FontWeight.ExtraBold),
)

private val CaptionColor = Color(0xFF9B93B8)

val WanderingTableTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 0.55.sp,
        color = Purple600,
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = CaptionColor,
    ),
)
