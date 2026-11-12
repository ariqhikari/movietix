package org.ariqhikari.movietix.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository

class FakeCinemaScheduleRepository : CinemaScheduleRepository {
    var shouldFailReserve: Boolean = false
    var shouldFailRelease: Boolean = false
    var releaseSeatsCallCount: Int = 0
    var lastReleasedScheduleId: String? = null
    var lastReleasedSeats: List<String> = emptyList()

    private val bookedSeatsMap = mutableMapOf<String, MutableSet<String>>()

    override fun getCinemasWithSchedules(
        movieId: Int,
        date: LocalDate
    ): Flow<AppResult<List<Cinema>>> = flowOf(AppResult.Success(emptyList()))

    override fun getSeatLayout(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: LocalDate,
        time: String
    ): Flow<AppResult<SeatLayout>> = flowOf(
        AppResult.Success(
            SeatLayout(
                scheduleId = scheduleId,
                rows = listOf("A", "B", "C"),
                totalColumns = 8,
                aisleAfterColumn = 4,
                seats = emptyList()
            )
        )
    )

    override suspend fun reserveSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit> {
        if (shouldFailReserve) {
            return AppResult.Error(AppError.Booking.SeatAlreadyOccupied)
        }
        val currentBooked = bookedSeatsMap.getOrPut(scheduleId) { mutableSetOf() }
        if (seatIds.any { it in currentBooked }) {
            return AppResult.Error(AppError.Booking.SeatAlreadyOccupied)
        }
        currentBooked.addAll(seatIds)
        return AppResult.Success(Unit)
    }

    override suspend fun releaseSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit> {
        releaseSeatsCallCount++
        lastReleasedScheduleId = scheduleId
        lastReleasedSeats = seatIds

        if (shouldFailRelease) {
            return AppResult.Error(AppError.Storage.WriteFailed("Simulated release failure"))
        }

        bookedSeatsMap[scheduleId]?.removeAll(seatIds.toSet())
        return AppResult.Success(Unit)
    }

    override suspend fun getMovieMinimumPrice(
        movieId: Int,
        date: LocalDate
    ): AppResult<Long> = AppResult.Success(50000L)

    fun getReservedSeats(scheduleId: String): Set<String> =
        bookedSeatsMap[scheduleId] ?: emptySet()
}
