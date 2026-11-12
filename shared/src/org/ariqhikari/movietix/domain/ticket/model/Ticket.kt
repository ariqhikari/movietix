package org.ariqhikari.movietix.domain.ticket.model

import org.ariqhikari.movietix.domain.booking.model.PaymentMethod

/**
 * Domain model representing an issued, confirmed movie ticket.
 * Free of persistence / DTO annotations.
 */
data class Ticket(
    val bookingCode: String,
    val movieId: Int,
    val movieTitle: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val cinemaName: String,
    val studioName: String,
    val studioType: String,
    val showtimeIso: String,
    val seats: List<String>,
    val pricePerSeat: Long,
    val serviceFee: Long,
    val totalPrice: Long,
    val paymentMethod: PaymentMethod,
    val transactionTimeEpochMs: Long,
    val status: TicketStatus = TicketStatus.ACTIVE,
    val qrPayload: String
)
