package org.ariqhikari.movietix.domain.ticket.model

/**
 * Parsed representation of MovieTix QR payload.
 * Schema v1: MOVIETIX:v1:<bookingCode>:<scheduleId>:<commaSeparatedSeats>
 */
data class QrTicketPayload(
    val version: String,
    val bookingCode: String,
    val scheduleId: String,
    val seats: List<String>
)
