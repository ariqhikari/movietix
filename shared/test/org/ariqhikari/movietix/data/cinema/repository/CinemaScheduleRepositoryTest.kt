package org.ariqhikari.movietix.data.cinema.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import okio.Path.Companion.toPath
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.data.cinema.local.ProceduralScheduleEngine
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CinemaScheduleRepositoryTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val fixedClock = FixedClock(Instant.parse("2026-11-11T08:00:00Z"))
    private val timeZone = TimeZone.UTC
    private val testDispatcher = StandardTestDispatcher()

    private val engine = ProceduralScheduleEngine(
        clock = fixedClock,
        timeZone = timeZone
    )

    private fun createTestDataStore(): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        val randomSuffix = Random.nextLong()
        val tempPath = "${okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/test_store_$randomSuffix.preferences_pb".toPath()
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempPath }
        )
    }

    @Test
    fun getCinemasWithSchedules_emitsSuccess() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        repository.getCinemasWithSchedules(550, LocalDate(2026, 11, 11)).test {
            val result = awaitItem()
            assertTrue(result is AppResult.Success)
            assertEquals(4, result.data.size)
            awaitComplete()
        }
    }

    @Test
    fun getSeatLayout_mergesEngineAndUserBookedSeats() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        val movieId = 550
        val cinemaId = "cgv-gi"
        val studioId = "std-reg"
        val date = LocalDate(2026, 11, 11)
        val time = "10:30"
        val scheduleId = engine.createScheduleId(movieId, cinemaId, studioId, date.toString(), time)

        // Find an available seat
        val baseLayout = engine.generateSeatLayout(scheduleId, movieId, cinemaId, studioId, date.toString(), time)
        val availableSeat = baseLayout.seats.first { it.status == SeatStatus.AVAILABLE }

        repository.getSeatLayout(scheduleId, movieId, cinemaId, studioId, date, time).test {
            val initial = awaitItem()
            assertTrue(initial is AppResult.Success)
            assertEquals(SeatStatus.AVAILABLE, initial.data.seats.first { it.id == availableSeat.id }.status)

            // Reserve that seat
            val reserveResult = repository.reserveSeats(scheduleId, listOf(availableSeat.id))
            assertTrue(reserveResult is AppResult.Success)

            // DataStore update causes flow to emit updated layout with OCCUPIED status
            val updated = awaitItem()
            assertTrue(updated is AppResult.Success)
            assertEquals(SeatStatus.OCCUPIED, updated.data.seats.first { it.id == availableSeat.id }.status)
        }
    }

    @Test
    fun reserveSeats_returnsError_whenSeatAlreadyOccupiedByEngine() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        val movieId = 550
        val cinemaId = "cgv-gi"
        val studioId = "std-reg"
        val date = LocalDate(2026, 11, 11)
        val time = "10:30"
        val scheduleId = engine.createScheduleId(movieId, cinemaId, studioId, date.toString(), time)

        val engineOccupied = engine.generateOccupiedSeats(scheduleId, movieId, cinemaId, studioId, date.toString(), time)
        val occupiedSeatId = engineOccupied.first()

        val reserveResult = repository.reserveSeats(scheduleId, listOf(occupiedSeatId))
        assertTrue(reserveResult is AppResult.Error)
        assertEquals(AppError.Booking.SeatAlreadyOccupied, reserveResult.error)
    }

    @Test
    fun reserveSeats_returnsError_onSecondReservationConflict() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        val movieId = 550
        val cinemaId = "cgv-gi"
        val studioId = "std-reg"
        val date = LocalDate(2026, 11, 11)
        val time = "10:30"
        val scheduleId = engine.createScheduleId(movieId, cinemaId, studioId, date.toString(), time)

        val baseLayout = engine.generateSeatLayout(scheduleId, movieId, cinemaId, studioId, date.toString(), time)
        val availableSeats = baseLayout.seats.filter { it.status == SeatStatus.AVAILABLE }.take(2).map { it.id }

        // User A reserves seats
        val resA = repository.reserveSeats(scheduleId, availableSeats)
        assertTrue(resA is AppResult.Success)

        // User B attempts to reserve same seats -> Conflict
        val resB = repository.reserveSeats(scheduleId, listOf(availableSeats[0]))
        assertTrue(resB is AppResult.Error)
        assertEquals(AppError.Booking.SeatAlreadyOccupied, resB.error)
    }

    @Test
    fun releaseSeats_releasesUserReservedSeats() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        val movieId = 550
        val cinemaId = "cgv-gi"
        val studioId = "std-reg"
        val date = LocalDate(2026, 11, 11)
        val time = "10:30"
        val scheduleId = engine.createScheduleId(movieId, cinemaId, studioId, date.toString(), time)

        val baseLayout = engine.generateSeatLayout(scheduleId, movieId, cinemaId, studioId, date.toString(), time)
        val seatToBook = baseLayout.seats.first { it.status == SeatStatus.AVAILABLE }.id

        repository.reserveSeats(scheduleId, listOf(seatToBook))

        // Now release
        val releaseRes = repository.releaseSeats(scheduleId, listOf(seatToBook))
        assertTrue(releaseRes is AppResult.Success)

        // Now seat should be reservable again
        val rebookRes = repository.reserveSeats(scheduleId, listOf(seatToBook))
        assertTrue(rebookRes is AppResult.Success)
    }

    @Test
    fun getMovieMinimumPrice_returnsCorrectLowestPrice() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = CinemaScheduleRepositoryImpl(engine, dataStore, testDispatcher)

        val weekdayDate = LocalDate(2026, 11, 11) // Wednesday
        val minPrice = repository.getMovieMinimumPrice(550, weekdayDate)
        assertTrue(minPrice is AppResult.Success)
        assertEquals(45_000L, minPrice.data)
    }
}
