package org.ariqhikari.movietix.presentation.ticket.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom Compose [Shape] that draws a rounded rectangle with circular notches
 * punched into both the left and right edges at a given vertical position [notchCenterYFraction].
 *
 * Designed in compliance with DESIGN_SYSTEM.md section 5.3:
 * - Corner radius [cornerRadius] (default 16.dp).
 * - Semicircular notches of diameter 24.dp (radius [notchRadius] = 12.dp).
 */
class PerforatedTicketShape(
    private val cornerRadius: Dp = 16.dp,
    private val notchRadius: Dp = 12.dp,
    private val notchCenterYFraction: Float = 0.65f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val notchPx = with(density) { notchRadius.toPx() }
        val notchCenterY = size.height * notchCenterYFraction.coerceIn(0.1f, 0.9f)

        val mainRectPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    topLeft = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                    topRight = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                    bottomRight = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                    bottomLeft = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
                )
            )
        }

        val leftNotch = Path().apply {
            addOval(
                Rect(
                    left = -notchPx,
                    top = notchCenterY - notchPx,
                    right = notchPx,
                    bottom = notchCenterY + notchPx
                )
            )
        }

        val rightNotch = Path().apply {
            addOval(
                Rect(
                    left = size.width - notchPx,
                    top = notchCenterY - notchPx,
                    right = size.width + notchPx,
                    bottom = notchCenterY + notchPx
                )
            )
        }

        val punchedPath = Path().apply {
            op(mainRectPath, leftNotch, PathOperation.Difference)
            op(this, rightNotch, PathOperation.Difference)
        }

        return Outline.Generic(punchedPath)
    }
}
