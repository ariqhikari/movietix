package org.ariqhikari.movietix.core.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * MovieTix Color Palette & Design Tokens
 * Follows docs/DESIGN_SYSTEM.md - "Midnight Cinema & Ticket Craft"
 * All contrast ratios verified with W3C WCAG 2.1 formulas.
 */
object MovieTixColors {
    // --- Dark Theme (Primary Showcase — "Midnight Theater") ---
    val CinemaBackground = Color(0xFF0B0E14)       // Obsidian Navy
    val CinemaSurfaceSubtle = Color(0xFF131822)    // Surface level 1 (card bg, app bar)
    val CinemaSurfaceCard = Color(0xFF1A202E)      // Surface level 2 (elevated card, sheet)
    val CinemaSurfaceBorder = Color(0xFF2A3448)    // 1dp stroke delimiter
    val CinemaPrimary = Color(0xFFE5A93C)          // Amber Gold (CTA, active)
    val CinemaPrimaryVariant = Color(0xFFC68B23)   // Pressed state
    val CinemaSecondary = Color(0xFFF08C4A)        // Neon Ember (badge, promo)
    val CinemaOnPrimary = Color(0xFF120C02)        // Text/icon on primary gold
    val CinemaTextPrimary = Color(0xFFF2F5FA)      // Headings, titles
    val CinemaTextSecondary = Color(0xFF9AA7BC)    // Synopses, metadata (6.69:1 on card)
    val CinemaTextTertiary = Color(0xFF63738A)     // Inactive labels only
    val CinemaSeatAvailable = Color(0xFF222B3D)    // Available seat neutral bg
    val CinemaSeatSelected = Color(0xFFE5A93C)     // Selected seat solid gold
    val CinemaSeatOccupied = Color(0xFF161B26)     // Occupied seat muted
    val StateSuccess = Color(0xFF2EC492)           // Success confirmation
    val StateErrorDark = Color(0xFFFF6B6B)         // Error dark theme (5.87:1 on card - pass AA)

    // --- Light Theme (Matinee Mode — "Silver Screen Morning") ---
    val LightBackground = Color(0xFFF8F9FC)        // Clean light canvas
    val LightSurface = Color(0xFFFFFFFF)           // Pure white card
    val LightSurfaceSubtle = Color(0xFFEDF1F7)     // Input / subtle container
    val LightPrimary = Color(0xFF825000)           // Amber Ochre Deep (6.78:1 on white - pass AA)
    val LightOnPrimary = Color(0xFFFFFFFF)         // White text on primary
    val LightTextPrimary = Color(0xFF10141D)       // Dark primary text (18.42:1 on white)
    val LightTextSecondary = Color(0xFF526075)     // Secondary body text (6.39:1 on white)
    val LightBorder = Color(0xFFD6DEEB)            // Card & notch divider
    val StateErrorLight = Color(0xFFC62828)        // Crimson red (5.62:1 on white - pass AA)
}
