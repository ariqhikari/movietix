package org.ariqhikari.movietix.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository

class FakeTicketRepository : TicketRepository {
    private val tickets = MutableStateFlow<List<Ticket>>(emptyList())
    var shouldFailSave: Boolean = false
    var shouldFailGet: Boolean = false
    var saveFailureCause: String = "Simulated save error"

    override fun getTickets(): Flow<AppResult<List<Ticket>>> =
        tickets.map {
            if (shouldFailGet) {
                AppResult.Error(AppError.Storage.ReadFailed("Simulated read error"))
            } else {
                AppResult.Success(it)
            }
        }

    override suspend fun getTicketByBookingCode(bookingCode: String): AppResult<Ticket?> {
        if (shouldFailGet) {
            return AppResult.Error(AppError.Storage.ReadFailed("Simulated read error"))
        }
        val ticket = tickets.value.firstOrNull { it.bookingCode == bookingCode }
        return AppResult.Success(ticket)
    }

    override suspend fun saveTicket(ticket: Ticket): AppResult<Unit> {
        if (shouldFailSave) {
            return AppResult.Error(AppError.Storage.WriteFailed(saveFailureCause))
        }
        if (tickets.value.any { it.bookingCode == ticket.bookingCode }) {
            return AppResult.Error(AppError.Storage.WriteFailed("Duplicate booking code"))
        }
        tickets.value = listOf(ticket) + tickets.value
        return AppResult.Success(Unit)
    }

    fun getAllTickets(): List<Ticket> = tickets.value
}
