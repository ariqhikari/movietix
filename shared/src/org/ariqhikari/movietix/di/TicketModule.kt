package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.ticket.repository.TicketRepositoryImpl
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository
import org.koin.dsl.module

val ticketModule = module {
    single<TicketRepository> {
        TicketRepositoryImpl(
            dataStore = get(),
            dispatcher = get()
        )
    }
}
