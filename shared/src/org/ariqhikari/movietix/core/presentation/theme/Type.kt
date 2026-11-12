package org.ariqhikari.movietix.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.resources.Res
import org.ariqhikari.movietix.resources.outfit_bold
import org.ariqhikari.movietix.resources.outfit_extrabold
import org.ariqhikari.movietix.resources.outfit_regular
import org.ariqhikari.movietix.resources.outfit_semibold
import org.ariqhikari.movietix.resources.plusjakartasans_variable
import org.jetbrains.compose.resources.Font

@Composable
fun outfitFontFamily(): FontFamily = FontFamily(
    Font(Res.font.outfit_regular, FontWeight.Normal),
    Font(Res.font.outfit_semibold, FontWeight.SemiBold),
    Font(Res.font.outfit_bold, FontWeight.Bold),
    Font(Res.font.outfit_extrabold, FontWeight.ExtraBold),
)

/**
 * Plus Jakarta Sans uses a single variable font with FontVariation.weight
 * for distinct render weights (400, 500, 600, 700).
 */
@Composable
fun plusJakartaSansFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.plusjakartasans_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resource = Res.font.plusjakartasans_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resource = Res.font.plusjakartasans_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resource = Res.font.plusjakartasans_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
)

/**
 * Custom typography tokens beyond standard Material 3 scale.
 */
object MovieTixTypographyTokens {
    @Composable
    fun labelMono(): TextStyle = TextStyle(
        fontFamily = outfitFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.0.sp,
    )
}

/**
 * MovieTix Typography Scale
 * Follows docs/DESIGN_SYSTEM.md section 3.
 */
@Composable
fun movieTixTypography(): Typography {
    val display = outfitFontFamily()
    val body = plusJakartaSansFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.3).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.2.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.3.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.5.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 1.0.sp,
        ),
    )
}
