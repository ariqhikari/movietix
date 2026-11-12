package org.ariqhikari.movietix.domain.ticket.model

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED;

    companion object {
        fun fromString(status: String): TicketStatus {
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: ACTIVE
        }
    }
}
