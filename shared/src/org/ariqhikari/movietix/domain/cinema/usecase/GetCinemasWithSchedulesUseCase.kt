package org.ariqhikari.movietix.domain.cinema.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository

/**
 * Use case to retrieve cinema schedules for a given movie and selected date.
 */
class GetCinemasWithSchedulesUseCase(
    private val repository: CinemaScheduleRepository
) {
    operator fun invoke(movieId: Int, date: LocalDate): Flow<AppResult<List<Cinema>>> {
        return repository.getCinemasWithSchedules(movieId, date)
    }
}
