package org.ariqhikari.movietix.domain.cinema.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CinemaUseCasesTest {

    private class FakeCinemaScheduleRepository : CinemaScheduleRepository {
        var cinemasToReturn: List<Cinema> = emptyList()
        var seatLayoutToReturn: SeatLayout = SeatLayout("sch-1", emptyList(), 10, 4, emptyList())
        var minPriceToReturn: Long = 45_000L

        override fun getCinemasWithSchedules(movieId: Int, date: LocalDate) =
            flowOf(AppResult.Success(cinemasToReturn))

        override fun getSeatLayout(
            scheduleId: String,
            movieId: Int,
            cinemaId: String,
            studioId: String,
            date: LocalDate,
            time: String
        ) = flowOf(AppResult.Success(seatLayoutToReturn))

        override suspend fun reserveSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun releaseSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun getMovieMinimumPrice(movieId: Int, date: LocalDate): AppResult<Long> =
            AppResult.Success(minPriceToReturn)
    }

    private val fakeRepository = FakeCinemaScheduleRepository()

    @Test
    fun calculateBookingPriceUseCase_computesAccurately() {
        val useCase = CalculateBookingPriceUseCase()

        // 0 seats
        val zero = useCase(0, 50_000L)
        assertEquals(0, zero.seatCount)
        assertEquals(0L, zero.subtotal)
        assertEquals(0L, zero.serviceFee)
        assertEquals(0L, zero.total)

        // 1 seat at 45,000 + 3,000 fee = 48,000
        val one = useCase(1, 45_000L)
        assertEquals(1, one.seatCount)
        assertEquals(45_000L, one.subtotal)
        assertEquals(3_000L, one.serviceFee)
        assertEquals(48_000L, one.total)

        // 3 seats at 65,000 + 9,000 fee = 204,000
        val three = useCase(3, 65_000L)
        assertEquals(3, three.seatCount)
        assertEquals(195_000L, three.subtotal)
        assertEquals(9_000L, three.serviceFee)
        assertEquals(204_000L, three.total)
    }

    @Test
    fun getCinemasWithSchedulesUseCase_delegatesToRepository() = runTest {
        val useCase = GetCinemasWithSchedulesUseCase(fakeRepository)
        useCase(550, LocalDate(2026, 11, 11)).test {
            val item = awaitItem()
            assertTrue(item is AppResult.Success)
            assertEquals(fakeRepository.cinemasToReturn, item.data)
            awaitComplete()
        }
    }

    @Test
    fun getSeatLayoutUseCase_delegatesToRepository() = runTest {
        val useCase = GetSeatLayoutUseCase(fakeRepository)
        useCase("sch-1", 550, "c1", "s1", LocalDate(2026, 11, 11), "10:30").test {
            val item = awaitItem()
            assertTrue(item is AppResult.Success)
            assertEquals(fakeRepository.seatLayoutToReturn, item.data)
            awaitComplete()
        }
    }

    @Test
    fun getMovieMinimumPriceUseCase_delegatesToRepository() = runTest {
        val useCase = GetMovieMinimumPriceUseCase(fakeRepository)
        val result = useCase(550, LocalDate(2026, 11, 11))
        assertTrue(result is AppResult.Success)
        assertEquals(45_000L, result.data)
    }
}
