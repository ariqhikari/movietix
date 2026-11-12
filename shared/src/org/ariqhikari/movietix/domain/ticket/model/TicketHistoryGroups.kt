package org.ariqhikari.movietix.domain.ticket.model

/**
 * Grouped tickets for display on TicketHistoryScreen.
 *
 * - [upcoming]: ACTIVE tickets, sorted by showtime ascending (earliest upcoming first).
 * - [completed]: EXPIRED and CANCELLED tickets, sorted by showtime descending (most recent first).
 */
data class TicketHistoryGroups(
    val upcoming: List<TicketDisplayItem> = emptyList(),
    val completed: List<TicketDisplayItem> = emptyList(),
) {
    val isEmpty: Boolean get() = upcoming.isEmpty() && completed.isEmpty()
    val totalCount: Int get() = upcoming.size + completed.size
}
