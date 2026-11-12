package org.ariqhikari.movietix.domain.ticket.util

import org.ariqhikari.movietix.domain.booking.util.QrPayloadFactory
import org.ariqhikari.movietix.domain.ticket.model.QrTicketPayload

/**
 * Validates and parses a MovieTix QR payload string.
 * Rejects unknown versions or malformed structures safely.
 */
object QrPayloadParser {
    private const val PREFIX = "MOVIETIX"

    fun parse(rawPayload: String): QrTicketPayload? {
        val parts = rawPayload.split(":")
        if (parts.size != 5) return null
        if (parts[0] != PREFIX) return null
        val version = parts[1]
        if (version != QrPayloadFactory.SCHEMA_VERSION) return null

        val bookingCode = parts[2]
        val scheduleId = parts[3]
        val rawSeats = parts[4]

        if (bookingCode.isBlank() || scheduleId.isBlank()) return null
        val seats = if (rawSeats.isBlank()) emptyList() else rawSeats.split(",")

        return QrTicketPayload(
            version = version,
            bookingCode = bookingCode,
            scheduleId = scheduleId,
            seats = seats
        )
    }

    fun isValid(rawPayload: String): Boolean = parse(rawPayload) != null
}
