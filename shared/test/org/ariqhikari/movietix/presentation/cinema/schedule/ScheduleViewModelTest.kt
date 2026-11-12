package org.ariqhikari.movietix.presentation.cinema.schedule

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.model.Showtime
import org.ariqhikari.movietix.domain.cinema.model.Studio
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.cinema.usecase.GetCinemasWithSchedulesUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private class FakeScheduleRepo : CinemaScheduleRepository {
        var resultToEmit: AppResult<List<Cinema>> = AppResult.Success(emptyList())

        override fun getCinemasWithSchedules(movieId: Int, date: LocalDate) =
            flowOf(resultToEmit)

        override fun getSeatLayout(
            scheduleId: String,
            movieId: Int,
            cinemaId: String,
            studioId: String,
            date: LocalDate,
            time: String
        ) = flowOf(AppResult.Success(SeatLayout("sch-1", emptyList(), 10, 4, emptyList())))

        override suspend fun reserveSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun releaseSeats(scheduleId: String, seatIds: List<String>): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun getMovieMinimumPrice(movieId: Int, date: LocalDate): AppResult<Long> =
            AppResult.Success(45_000L)
    }

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepo = FakeScheduleRepo()
    private val useCase = GetCinemasWithSchedulesUseCase(fakeRepo)
    private val fixedClock = FixedClock(Instant.parse("2026-11-11T08:00:00Z"))
    private val timeZone = TimeZone.UTC

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        movieId: Int = 550,
        movieTitle: String = "Fight Club",
        posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster.jpg"
    ): ScheduleViewModel {
        return ScheduleViewModel(
            movieId = movieId,
            movieTitle = movieTitle,
            posterUrl = posterUrl,
            getCinemasWithSchedulesUseCase = useCase,
            clock = fixedClock,
            timeZone = timeZone
        )
    }

    @Test
    fun initialState_has7AvailableDatesAndLoadsSchedules() = runTest(testDispatcher) {
        val dummyCinema = Cinema(
            id = "cgv-gi",
            name = "CGV Grand Indonesia",
            address = "Grand Indonesia",
            city = "Jakarta",
            studios = emptyList()
        )
        fakeRepo.resultToEmit = AppResult.Success(listOf(dummyCinema))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(550, state.movieId)
        assertEquals("Fight Club", state.movieTitle)
        assertEquals(LocalDate(2026, 11, 11), state.selectedDate)
        assertEquals(7, state.availableDates.size)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.cinemas.size)
        assertEquals("cgv-gi", state.cinemas[0].id)
    }

    @Test
    fun selectDate_updatesSelectedDateAndFetchesNewSchedules() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val newDate = LocalDate(2026, 11, 13)
        viewModel.onAction(ScheduleAction.SelectDate(newDate))
        advanceUntilIdle()

        assertEquals(newDate, viewModel.state.value.selectedDate)
    }

    @Test
    fun selectShowtime_emitsNavigateToSeatMapEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val cinema = Cinema("c1", "CGV Central Park", "CP Mall", "Jakarta", emptyList())
        val studio = Studio("s1", "Regular 2D", StudioType.REGULAR, emptyList())
        val showtime = Showtime("sch-100", "13:30", 45_000L, 20, 80)

        viewModel.effect.test {
            viewModel.onAction(ScheduleAction.SelectShowtime(cinema, studio, showtime))
            val effect = awaitItem()
            assertTrue(effect is ScheduleEffect.NavigateToSeatMap)
            assertEquals("sch-100", effect.scheduleId)
            assertEquals("CGV Central Park", effect.cinemaName)
            assertEquals("Regular 2D", effect.studioName)
            assertEquals("REGULAR", effect.studioType)
            assertEquals("13:30", effect.timeStr)
            assertEquals(45_000L, effect.pricePerSeat)
        }
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(ScheduleAction.BackClicked)
            val effect = awaitItem()
            assertTrue(effect is ScheduleEffect.NavigateBack)
        }
    }

    @Test
    fun repositoryError_updatesStateWithErrorMessage() = runTest(testDispatcher) {
        fakeRepo.resultToEmit = AppResult.Error(AppError.Storage.ReadFailed("Disk IO"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage != null)
    }
}
