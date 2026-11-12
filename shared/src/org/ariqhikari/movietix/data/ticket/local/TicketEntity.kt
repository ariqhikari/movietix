package org.ariqhikari.movietix.data.ticket.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketHistoryContainer(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("tickets") val tickets: List<TicketEntity> = emptyList()
)

@Serializable
data class TicketEntity(
    @SerialName("booking_code") val bookingCode: String,
    @SerialName("movie_id") val movieId: Int,
    @SerialName("movie_title") val movieTitle: String,
    @SerialName("poster_url") val posterUrl: String?,
    @SerialName("backdrop_url") val backdropUrl: String?,
    @SerialName("cinema_name") val cinemaName: String,
    @SerialName("studio_name") val studioName: String,
    @SerialName("studio_type") val studioType: String,
    @SerialName("showtime_iso") val showtimeIso: String,
    @SerialName("seats") val seats: List<String>,
    @SerialName("price_per_seat") val pricePerSeat: Long,
    @SerialName("service_fee") val serviceFee: Long,
    @SerialName("total_price") val totalPrice: Long,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("transaction_time_epoch_ms") val transactionTimeEpochMs: Long,
    @SerialName("status") val status: String,
    @SerialName("qr_payload") val qrPayload: String
)
