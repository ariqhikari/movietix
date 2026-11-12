package org.ariqhikari.movietix.domain.ticket.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus

/**
 * Resolves the display status of a ticket based on injected Clock and TimeZone.
 *
 * Rules:
 * 1. If stored status is CANCELLED -> remain CANCELLED.
 * 2. If showtime is strictly before clock.now() -> EXPIRED.
 * 3. If showtime is equal to or after clock.now() -> ACTIVE.
 *
 * Handles both ISO instant strings (e.g. 2026-11-12T15:30:00Z) and
 * local date-time strings (e.g. 2026-11-12T15:30:00) by converting with [timeZone].
 */
class TicketStatusResolver(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    fun resolve(ticket: Ticket): TicketStatus {
        if (ticket.status == TicketStatus.CANCELLED) {
            return TicketStatus.CANCELLED
        }

        val showtimeEpochMs = parseShowtimeEpochMs(ticket.showtimeIso)
        val nowEpochMs = clock.now().toEpochMilliseconds()

        return if (showtimeEpochMs < nowEpochMs) {
            TicketStatus.EXPIRED
        } else {
            TicketStatus.ACTIVE
        }
    }

    fun toDisplayItem(ticket: Ticket): TicketDisplayItem {
        return TicketDisplayItem(
            ticket = ticket,
            derivedStatus = resolve(ticket)
        )
    }

    private fun parseShowtimeEpochMs(isoString: String): Long {
        return try {
            kotlinx.datetime.Instant.parse(isoString).toEpochMilliseconds()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(isoString).toInstant(timeZone).toEpochMilliseconds()
            } catch (_: Exception) {
                // If unparseable fallback to 0 (expired)
                0L
            }
        }
    }
}
