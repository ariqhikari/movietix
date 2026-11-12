package org.ariqhikari.movietix.domain.ticket.model

/**
 * Presentation-ready domain representation of a ticket with derived status.
 *
 * Status derivation rule:
 * - If [Ticket.status] is [TicketStatus.CANCELLED], derived status is CANCELLED.
 * - Otherwise:
 *     - If showtime is strictly before current clock time, derived status is [TicketStatus.EXPIRED].
 *     - If showtime is equal to or after current clock time, derived status is [TicketStatus.ACTIVE].
 */
data class TicketDisplayItem(
    val ticket: Ticket,
    val derivedStatus: TicketStatus
) {
    val isActive: Boolean get() = derivedStatus == TicketStatus.ACTIVE
    val isExpired: Boolean get() = derivedStatus == TicketStatus.EXPIRED
    val isCancelled: Boolean get() = derivedStatus == TicketStatus.CANCELLED
}
