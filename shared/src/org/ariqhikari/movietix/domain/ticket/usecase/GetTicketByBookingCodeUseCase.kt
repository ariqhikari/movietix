package org.ariqhikari.movietix.domain.ticket.usecase

import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver

/**
 * Use case to retrieve a single ticket by its booking code, deriving its status.
 * Returns AppError.Storage.ReadFailed if not found.
 */
class GetTicketByBookingCodeUseCase(
    private val ticketRepository: TicketRepository,
    private val ticketStatusResolver: TicketStatusResolver,
) {

    suspend operator fun invoke(bookingCode: String): AppResult<TicketDisplayItem> {
        return when (val result = ticketRepository.getTicketByBookingCode(bookingCode)) {
            is AppResult.Success -> {
                val ticket = result.data
                if (ticket != null) {
                    AppResult.Success(ticketStatusResolver.toDisplayItem(ticket))
                } else {
                    AppResult.Error(
                        AppError.Storage.ReadFailed("Tiket dengan kode $bookingCode tidak ditemukan")
                    )
                }
            }
            is AppResult.Error -> AppResult.Error(result.error)
        }
    }
}
