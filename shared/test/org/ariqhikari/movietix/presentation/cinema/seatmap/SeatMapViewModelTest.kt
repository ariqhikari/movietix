package org.ariqhikari.movietix.presentation.cinema.seatmap

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.Seat
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.cinema.usecase.CalculateBookingPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetSeatLayoutUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SeatMapViewModelTest {

    private class FakeScheduleRepo : CinemaScheduleRepository {
        var layoutToReturn: SeatLayout = SeatLayout("sch-1", emptyList(), 10, 4, emptyList())

        override fun getCinemasWithSchedules(movieId: Int, date: LocalDate) =
            flowOf(AppResult.Success(emptyList<Cinema>()))

        override fun getSeatLayout(
            scheduleId: String,
            movieId: Int,
            cinemaId: String,
            studioId: String,
            date: LocalDate,
            time: String
        ) = flowOf(AppResult.Success(layoutToReturn))

        override suspend fun reserveSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun releaseSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun getMovieMinimumPrice(movieId: Int, date: LocalDate): AppResult<Long> =
            AppResult.Success(45_000L)
    }

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepo = FakeScheduleRepo()
    private val getSeatLayoutUseCase = GetSeatLayoutUseCase(fakeRepo)
    private val calculateBookingPriceUseCase = CalculateBookingPriceUseCase()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createDummyLayout(): SeatLayout {
        val rows = listOf("A", "B")
        val seats = listOf(
            Seat("A1", "A", 1, SeatStatus.AVAILABLE),
            Seat("A2", "A", 2, SeatStatus.AVAILABLE),
            Seat("A3", "A", 3, SeatStatus.AVAILABLE),
            Seat("A4", "A", 4, SeatStatus.OCCUPIED), // Occupied seat
            Seat("A5", "A", 5, SeatStatus.AVAILABLE),
            Seat("A6", "A", 6, SeatStatus.AVAILABLE),
            Seat("A7", "A", 7, SeatStatus.AVAILABLE),
            Seat("A8", "A", 8, SeatStatus.AVAILABLE)
        )
        return SeatLayout(
            scheduleId = "sch-1",
            rows = rows,
            totalColumns = 8,
            aisleAfterColumn = 4,
            seats = seats
        )
    }

    private fun createViewModel(): SeatMapViewModel {
        fakeRepo.layoutToReturn = createDummyLayout()
        return SeatMapViewModel(
            scheduleId = "sch-1",
            movieId = 550,
            movieTitle = "Fight Club",
            cinemaId = "cgv-gi",
            cinemaName = "CGV Grand Indonesia",
            studioId = "std-reg",
            studioName = "Regular 2D",
            studioTypeStr = "REGULAR",
            dateStr = "2026-11-11",
            timeStr = "13:30",
            pricePerSeat = 45_000L,
            getSeatLayoutUseCase = getSeatLayoutUseCase,
            calculateBookingPriceUseCase = calculateBookingPriceUseCase
        )
    }

    @Test
    fun initialState_loadsSeatLayoutAndZeroSelection() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("sch-1", state.scheduleId)
        assertEquals("Fight Club", state.movieTitle)
        assertEquals("CGV Grand Indonesia", state.cinemaName)
        assertEquals(StudioType.REGULAR, state.studioType)
        assertFalse(state.isLoading)
        assertTrue(state.selectedSeats.isEmpty())
        assertEquals(0L, state.priceBreakdown.total)
    }

    @Test
    fun toggleSeat_selectsAvailableSeat_andRecalculatesPrice() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SeatMapAction.ToggleSeat("A1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf("A1"), state.selectedSeats)
        // 1 ticket = 45,000 + 3,000 service fee = 48,000
        assertEquals(45_000L, state.priceBreakdown.subtotal)
        assertEquals(3_000L, state.priceBreakdown.serviceFee)
        assertEquals(48_000L, state.priceBreakdown.total)
    }

    @Test
    fun toggleSeat_deselectsAlreadySelectedSeat() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SeatMapAction.ToggleSeat("A1"))
        advanceUntilIdle()
        assertEquals(listOf("A1"), viewModel.state.value.selectedSeats)

        viewModel.onAction(SeatMapAction.ToggleSeat("A1"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.selectedSeats.isEmpty())
        assertEquals(0L, viewModel.state.value.priceBreakdown.total)
    }

    @Test
    fun toggleSeat_ignoresOccupiedSeat() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // A4 is OCCUPIED
        viewModel.onAction(SeatMapAction.ToggleSeat("A4"))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.selectedSeats.isEmpty())
    }

    @Test
    fun toggleSeat_warnsWhenSelectingMoreThan6Seats() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Select 6 seats: A1, A2, A3, A5, A6, A7
        viewModel.onAction(SeatMapAction.ToggleSeat("A1"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A2"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A3"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A5"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A6"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A7"))
        advanceUntilIdle()
        assertEquals(6, viewModel.state.value.selectedSeats.size)

        // Attempting to select 7th seat (A8) triggers warning effect
        viewModel.effect.test {
            viewModel.onAction(SeatMapAction.ToggleSeat("A8"))
            val effect = awaitItem()
            assertTrue(effect is SeatMapEffect.ShowWarning)
            assertTrue(effect.message.contains("6 kursi"))
        }

        assertEquals(6, viewModel.state.value.selectedSeats.size)
    }

    @Test
    fun proceedToCheckout_warnsWhenNoSeatsSelected() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(SeatMapAction.ProceedToCheckout)
            val effect = awaitItem()
            assertTrue(effect is SeatMapEffect.ShowWarning)
            assertTrue(effect.message.contains("minimal 1 kursi"))
        }
    }

    @Test
    fun proceedToCheckout_emitsNavigateToCheckoutWithOrderSummary() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SeatMapAction.ToggleSeat("A1"))
        viewModel.onAction(SeatMapAction.ToggleSeat("A2"))
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(SeatMapAction.ProceedToCheckout)
            val effect = awaitItem()
            assertTrue(effect is SeatMapEffect.NavigateToCheckout)
            assertEquals("sch-1", effect.scheduleId)
            assertEquals("Fight Club", effect.movieTitle)
            assertEquals("CGV Grand Indonesia", effect.cinemaName)
            assertEquals(listOf("A1", "A2"), effect.selectedSeats)
            assertEquals(90_000L, effect.subtotal)
            assertEquals(6_000L, effect.serviceFee)
            assertEquals(96_000L, effect.totalPrice)
        }
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(SeatMapAction.BackClicked)
            val effect = awaitItem()
            assertTrue(effect is SeatMapEffect.NavigateBack)
        }
    }
}
