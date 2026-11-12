package org.ariqhikari.movietix.domain.booking.util

/**
 * Deterministic, versioned ticket QR payload factory.
 * Format: MOVIETIX:v1:<bookingCode>:<scheduleId>:<commaSeparatedSeats>
 */
object QrPayloadFactory {
    const val SCHEMA_VERSION = "v1"

    fun create(
        bookingCode: String,
        scheduleId: String,
        seats: List<String>
    ): String {
        val sortedSeats = seats.sorted().joinToString(",")
        return "MOVIETIX:$SCHEMA_VERSION:$bookingCode:$scheduleId:$sortedSeats"
    }
}
