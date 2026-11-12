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

    @Serializable
    data class Schedule(
        val movieId: Int,
        val movieTitle: String,
        val posterUrl: String? = null
    ) : ScreenDestination

    @Serializable
    data class SeatMap(
        val scheduleId: String,
        val movieId: Int,
        val movieTitle: String,
        val cinemaId: String,
        val cinemaName: String,
        val studioId: String,
        val studioName: String,
        val studioType: String,
        val dateStr: String,
        val timeStr: String,
        val pricePerSeat: Long
    ) : ScreenDestination

    @Serializable
    data class Checkout(
        val scheduleId: String,
        val movieId: Int,
        val movieTitle: String,
        val cinemaId: String = "",
        val cinemaName: String,
        val studioName: String,
        val studioType: String,
        val dateStr: String,
        val timeStr: String,
        val seats: List<String>,
        val pricePerSeat: Long,
        val subtotal: Long,
        val serviceFee: Long,
        val totalPrice: Long,
        val posterUrl: String? = null
    ) : ScreenDestination
}
