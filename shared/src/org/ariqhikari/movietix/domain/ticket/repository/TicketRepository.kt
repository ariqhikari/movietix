package org.ariqhikari.movietix.domain.ticket.repository

import kotlinx.coroutines.flow.Flow
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.model.Ticket

/**
 * Domain repository contract for managing purchased movie tickets.
 */
interface TicketRepository {
    fun getTickets(): Flow<AppResult<List<Ticket>>>
    suspend fun getTicketByBookingCode(bookingCode: String): AppResult<Ticket?>
    suspend fun saveTicket(ticket: Ticket): AppResult<Unit>
}
