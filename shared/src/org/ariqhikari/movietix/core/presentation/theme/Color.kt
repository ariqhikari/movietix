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
    val CinemaPrimaryContainer = Color(0xFF362103) // Deep Amber Container (14.32:1 vs text, 7.51:1 vs gold)
    val CinemaSecondary = Color(0xFFF08C4A)        // Neon Ember (badge, promo)
    val CinemaOnPrimary = Color(0xFF120C02)        // Text/icon on primary gold
    val CinemaTextPrimary = Color(0xFFF2F5FA)      // Headings, titles
    val CinemaTextSecondary = Color(0xFF9AA7BC)    // Synopses, metadata (6.69:1 on card)
    val CinemaTextTertiary = Color(0xFF63738A)     // Inactive labels only
    val CinemaTextHint = Color(0xFF7E8EAA)         // Placeholder & hint text (4.92:1 on card - pass AA)
    val CinemaSeatAvailable = Color(0xFF222B3D)    // Available seat neutral bg
    val CinemaSeatBorder = Color(0xFF3D4C69)       // Available seat boundary stroke
    val CinemaSeatSelected = Color(0xFFE5A93C)     // Selected seat solid gold
    val CinemaSeatOccupied = Color(0xFF161B26)     // Occupied seat muted
    val StateSuccess = Color(0xFF2EC492)           // Success confirmation
    val StateSuccessContainer = Color(0xFF1E3A2B)  // Success container circle
    val StateErrorDark = Color(0xFFFF6B6B)         // Error dark theme (5.87:1 on card - pass AA)
    val StateErrorContainerDark = Color(0xFF381E1E) // Error container circle

    // --- Light Theme (Matinee Mode — "Silver Screen Morning") ---
    val LightBackground = Color(0xFFF8F9FC)        // Clean light canvas
    val LightSurface = Color(0xFFFFFFFF)           // Pure white card
    val LightSurfaceSubtle = Color(0xFFEDF1F7)     // Input / subtle container
    val LightPrimary = Color(0xFF825000)           // Amber Ochre Deep (6.78:1 on white - pass AA)
    val LightOnPrimary = Color(0xFFFFFFFF)         // White text on primary
    val LightSecondary = Color(0xFFA34400)         // Warm Terracotta / Burnt Amber (6.21:1 on white - pass AA)
    val LightTextPrimary = Color(0xFF10141D)       // Dark primary text (18.42:1 on white)
    val LightTextSecondary = Color(0xFF526075)     // Secondary body text (6.39:1 on white)
    val LightTextHint = Color(0xFF5D6D82)          // Light placeholder & hint (5.22:1 on white - pass AA)
    val LightBorder = Color(0xFFD6DEEB)            // Card & notch divider
    val LightSeatBorder = Color(0xFFB0BDD0)        // Light seat boundary stroke
    val StateErrorLight = Color(0xFFC62828)        // Crimson red (5.62:1 on white - pass AA)
}
