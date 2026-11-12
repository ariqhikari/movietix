package org.ariqhikari.movietix.core.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing, Radius, and Sizing tokens
 * Follows docs/DESIGN_SYSTEM.md section 4.
 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Radius {
    val sm = 6.dp    // chip, slot pill
    val md = 10.dp   // seat box, rating badge
    val lg = 16.dp   // poster card, detail card, dialog modal
    val pill = 999.dp // circular CTA, status filter
}

object Dimensions {
    val minTouchTarget = 48.dp
    val seatBoxSize = 34.dp
    val qrCodeSize = 160.dp
    val strokeWidth = 1.dp
    val seatStrokeWidth = 1.5.dp
}
