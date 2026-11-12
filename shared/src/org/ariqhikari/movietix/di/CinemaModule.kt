package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.cinema.local.ProceduralScheduleEngine
import org.ariqhikari.movietix.data.cinema.repository.CinemaScheduleRepositoryImpl
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.cinema.usecase.CalculateBookingPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetCinemasWithSchedulesUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetMovieMinimumPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetSeatLayoutUseCase
import org.koin.dsl.module

val cinemaModule = module {
    single { ProceduralScheduleEngine() }
    single<CinemaScheduleRepository> { CinemaScheduleRepositoryImpl(get(), get(), get()) }

    factory { GetCinemasWithSchedulesUseCase(get()) }
    factory { GetSeatLayoutUseCase(get()) }
    factory { CalculateBookingPriceUseCase() }
    factory { GetMovieMinimumPriceUseCase(get()) }
}
