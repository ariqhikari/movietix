package org.ariqhikari.movietix.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for MovieTix.
 */
@Serializable
sealed interface ScreenDestination {
    @Serializable
    data object Home : ScreenDestination

    @Serializable
    data class Detail(val movieId: Int) : ScreenDestination
}
