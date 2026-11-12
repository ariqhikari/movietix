package org.ariqhikari.movietix.presentation.ticket.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.ariqhikari.movietix.core.presentation.theme.Dimensions
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing

/**
 * Renders an offline-capable high-contrast QR code for cinema entrance validation.
 *
 * Requirements:
 * - Rendered purely locally via Compose Canvas (qrose) - zero network requests.
 * - Max contrast: Pure black `#000000` modules on pure white `#FFFFFF` surface with quiet zone.
 * - Error correction: [QrErrorCorrectionLevel.Medium] (approx 15% recovery, optimum density vs scan speed).
 * - Semantics contentDescription includes the booking code for screen readers.
 * - Supports tapping to enlarge if difficult to scan.
 */
@Composable
fun OfflineQrCode(
    payload: String,
    bookingCode: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimensions.qrCodeSize,
    onClick: (() -> Unit)? = null
) {
    val painter = rememberQrCodePainter(
        data = payload,
        errorCorrectionLevel = QrErrorCorrectionLevel.Medium
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .size(size + Spacing.md * 2)
            .clip(RoundedCornerShape(Radius.md))
            .background(Color.White)
            .border(width = 1.dp, color = MovieTixColors.CinemaSurfaceBorder, shape = RoundedCornerShape(Radius.md))
            .then(clickModifier)
            .semantics {
                contentDescription = "Kode QR tiket bioskop untuk kode booking $bookingCode"
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .padding(Spacing.xs)
        )
    }
}
