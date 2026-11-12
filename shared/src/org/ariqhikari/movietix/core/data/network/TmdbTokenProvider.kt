package org.ariqhikari.movietix.core.data.network

/**
 * Platform-specific TMDB Access Token provider (Alternative A).
 * Android: reads from local.properties on device or environment.
 * iOS: reads from Info.plist (TMDB_ACCESS_TOKEN).
 */
expect fun getTmdbAccessToken(): String
