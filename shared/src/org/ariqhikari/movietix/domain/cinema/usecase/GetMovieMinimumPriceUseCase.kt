package org.ariqhikari.movietix.domain.cinema.usecase

import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository

/**
 * Use case to compute minimum ticket price for a movie on a given date.
 */
class GetMovieMinimumPriceUseCase(
    private val repository: CinemaScheduleRepository
) {
    suspend operator fun invoke(movieId: Int, date: LocalDate): AppResult<Long> {
        return repository.getMovieMinimumPrice(movieId, date)
    }
}
