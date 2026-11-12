package org.ariqhikari.movietix.domain.ticket.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem
import org.ariqhikari.movietix.domain.ticket.model.TicketHistoryGroups
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver

/**
 * Use case to observe tickets from repository, derive their statuses based on
 * current clock, and partition them into "Akan Datang" (upcoming) and "Selesai" (completed).
 *
 * Upcoming: ACTIVE, sorted by showtime ascending (earliest showtime first).
 * Completed: EXPIRED / CANCELLED, sorted by showtime descending (newest showtime first).
 */
class GetTicketsUseCase(
    private val ticketRepository: TicketRepository,
    private val ticketStatusResolver: TicketStatusResolver
) {

    operator fun invoke(): Flow<AppResult<TicketHistoryGroups>> {
        return ticketRepository.getTickets().map { result ->
            when (result) {
                is AppResult.Success -> {
                    val rawTickets: List<Ticket> = result.data
                    val displayItems: List<TicketDisplayItem> = rawTickets.map { ticket ->
                        ticketStatusResolver.toDisplayItem(ticket)
                    }

                    val upcoming = displayItems
                        .filter { it.derivedStatus == TicketStatus.ACTIVE }
                        .sortedBy { it.ticket.showtimeIso }

                    val completed = displayItems
                        .filter { it.derivedStatus != TicketStatus.ACTIVE }
                        .sortedByDescending { it.ticket.showtimeIso }

                    AppResult.Success(
                        TicketHistoryGroups(
                            upcoming = upcoming,
                            completed = completed
                        )
                    )
                }
                is AppResult.Error -> {
                    AppResult.Error(result.error)
                }
            }
        }
    }
}
