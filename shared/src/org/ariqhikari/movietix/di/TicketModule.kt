package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.ticket.repository.TicketRepositoryImpl
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketByBookingCodeUseCase
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketsUseCase
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailViewModel
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ticketModule = module {
    single<TicketRepository> {
        TicketRepositoryImpl(
            dataStore = get(),
            dispatcher = get()
        )
    }

    single { TicketStatusResolver() }

    factory {
        GetTicketsUseCase(
            ticketRepository = get(),
            ticketStatusResolver = get()
        )
    }

    factory {
        GetTicketByBookingCodeUseCase(
            ticketRepository = get(),
            ticketStatusResolver = get()
        )
    }

    viewModel { TicketHistoryViewModel(get()) }
    viewModel { (bookingCode: String) -> TicketDetailViewModel(bookingCode, get()) }
}
