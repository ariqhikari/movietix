package org.ariqhikari.movietix.domain.cinema.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository

/**
 * Use case to observe the interactive seat layout for a showtime,
 * reactive to local DataStore reservations.
 */
class GetSeatLayoutUseCase(
    private val repository: CinemaScheduleRepository
) {
    operator fun invoke(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: LocalDate,
        time: String
    ): Flow<AppResult<SeatLayout>> {
        return repository.getSeatLayout(
            scheduleId = scheduleId,
            movieId = movieId,
            cinemaId = cinemaId,
            studioId = studioId,
            date = date,
            time = time
        )
    }
}
