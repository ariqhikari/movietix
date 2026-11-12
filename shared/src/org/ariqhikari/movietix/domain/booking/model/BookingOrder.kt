package org.ariqhikari.movietix.domain.booking.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingOrder(
    val scheduleId: String,
    val movieId: Int,
    val movieTitle: String,
    val cinemaId: String,
    val cinemaName: String,
    val studioName: String,
    val showtime: String,
    val date: String,
    val selectedSeats: List<String>,
    val pricePerSeat: Long,
    val serviceFee: Long,
    val totalPrice: Long
)
