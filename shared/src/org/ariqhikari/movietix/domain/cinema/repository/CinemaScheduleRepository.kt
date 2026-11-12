package org.ariqhikari.movietix.domain.cinema.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout

/**
 * Repository interface for cinema schedules and interactive seat layout management.
 */
interface CinemaScheduleRepository {

    /**
     * Retrieves all cinemas and their studios/showtimes for a given movie and date.
     */
    fun getCinemasWithSchedules(
        movieId: Int,
        date: LocalDate
    ): Flow<AppResult<List<Cinema>>>

    /**
     * Observes the seat layout for a specific showtime.
     * Merges deterministic engine seats with user-reserved seats from local DataStore.
     */
    fun getSeatLayout(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: LocalDate,
        time: String
    ): Flow<AppResult<SeatLayout>>

    /**
     * Atomically reserves the specified seats for a given showtime in DataStore.
     * Returns [org.ariqhikari.movietix.core.domain.model.AppError.Booking.SeatAlreadyOccupied] if any seat is already taken.
     */
    suspend fun reserveSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit>

    /**
     * Releases previously reserved seats (e.g. on booking cancellation or rollback).
     */
    suspend fun releaseSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit>

    /**
     * Computes the lowest ticket price available for a movie on a given date.
     */
    suspend fun getMovieMinimumPrice(
        movieId: Int,
        date: LocalDate
    ): AppResult<Long>
}
