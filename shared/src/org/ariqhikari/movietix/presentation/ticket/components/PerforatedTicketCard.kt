package org.ariqhikari.movietix.presentation.ticket.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Spacing

/**
 * Perforated cinema ticket card component.
 * Displays two sections ([topContent] and [bottomContent]) separated by a dashed
 * perforation line with circular punched notches on both sides.
 *
 * Implements DESIGN_SYSTEM.md section 5.3:
 * - Corner radius: 16.dp
 * - Notch radius: 12.dp (diameter 24.dp)
 * - Notch color follows background, stroke follows border color.
 */
@Composable
fun PerforatedTicketCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MovieTixColors.CinemaSurfaceBorder,
    dividerColor: Color = MovieTixColors.CinemaSeatBorder,
    cornerRadius: Dp = 16.dp,
    notchRadius: Dp = 12.dp,
    topContent: @Composable ColumnScope.() -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit
) {
    val shape = PerforatedTicketShape(
        cornerRadius = cornerRadius,
        notchRadius = notchRadius,
        notchCenterYFraction = 0.60f
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Bagian Atas: Detail Film & Jadwal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md)
            ) {
                topContent()
            }

            // Garis Perforasi Putus-putus
            PerforationDivider(
                modifier = Modifier.fillMaxWidth(),
                lineColor = dividerColor,
                notchRadius = notchRadius
            )

            // Bagian Bawah: QR Code & Kode Booking
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md)
            ) {
                bottomContent()
            }
        }
    }
}

@Composable
private fun PerforationDivider(
    modifier: Modifier = Modifier,
    lineColor: Color = MovieTixColors.CinemaSeatBorder,
    notchRadius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    dashGap: Dp = 4.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val notchPx = notchRadius.toPx()
        val startX = notchPx + 4.dp.toPx()
        val endX = size.width - notchPx - 4.dp.toPx()

        if (endX > startX) {
            drawLine(
                color = lineColor,
                start = Offset(x = startX, y = size.height / 2),
                end = Offset(x = endX, y = size.height / 2),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dashLength.toPx(), dashGap.toPx()),
                    0f
                )
            )
        }
    }
}
