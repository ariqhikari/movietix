package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.cinema.local.ProceduralScheduleEngine
import org.ariqhikari.movietix.data.cinema.repository.CinemaScheduleRepositoryImpl
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.cinema.usecase.CalculateBookingPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetCinemasWithSchedulesUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetMovieMinimumPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetSeatLayoutUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cinemaModule = module {
    single { ProceduralScheduleEngine() }
    single<CinemaScheduleRepository> { CinemaScheduleRepositoryImpl(get(), get(), get()) }

    factory { GetCinemasWithSchedulesUseCase(get()) }
    factory { GetSeatLayoutUseCase(get()) }
    factory { CalculateBookingPriceUseCase() }
    factory { GetMovieMinimumPriceUseCase(get()) }

    viewModel { (movieId: Int, movieTitle: String, posterUrl: String?) ->
        org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleViewModel(
            movieId = movieId,
            movieTitle = movieTitle,
            posterUrl = posterUrl,
            getCinemasWithSchedulesUseCase = get()
        )
    }

    viewModel { (params: org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapNavParams) ->
        org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapViewModel(
            scheduleId = params.scheduleId,
            movieId = params.movieId,
            movieTitle = params.movieTitle,
            cinemaId = params.cinemaId,
            cinemaName = params.cinemaName,
            studioId = params.studioId,
            studioName = params.studioName,
            studioTypeStr = params.studioTypeStr,
            dateStr = params.dateStr,
            timeStr = params.timeStr,
            pricePerSeat = params.pricePerSeat,
            getSeatLayoutUseCase = get(),
            calculateBookingPriceUseCase = get()
        )
    }
}
