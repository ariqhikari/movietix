package org.ariqhikari.movietix.core.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius

/**
 * Shimmer effect modifier for skeleton loading states.
 * Uses high contrast tonal layering adhering to docs/DESIGN_SYSTEM.md.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(
            MovieTixColors.CinemaSurfaceCard,
            MovieTixColors.CinemaSurfaceBorder,
            MovieTixColors.CinemaSurfaceCard
        )
    } else {
        listOf(
            MovieTixColors.LightSurfaceSubtle,
            MovieTixColors.LightBorder,
            MovieTixColors.LightSurfaceSubtle
        )
    }

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    background(brush)
}

fun Modifier.shimmerPlaceholder(
    shape: Shape = RoundedCornerShape(Radius.lg)
): Modifier = this.clip(shape).shimmerEffect()

/**
 * Reusable placeholder box displaying the shimmer animation.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.lg)
) {
    Box(
        modifier = modifier.shimmerPlaceholder(shape)
    )
}
