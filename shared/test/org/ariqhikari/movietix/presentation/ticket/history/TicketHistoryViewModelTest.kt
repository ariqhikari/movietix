package org.ariqhikari.movietix.presentation.ticket.history

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketsUseCase
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import org.ariqhikari.movietix.fake.FakeTicketRepository
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.Action
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.Effect
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TicketHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeTicketRepository
    private lateinit var statusResolver: TicketStatusResolver
    private lateinit var getTicketsUseCase: GetTicketsUseCase

    // Reference time: 2026-11-12T12:00:00Z
    private val fakeClock = object : kotlinx.datetime.Clock {
        override fun now(): Instant = Instant.parse("2026-11-12T12:00:00Z")
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTicketRepository()
        statusResolver = TicketStatusResolver(
            clock = fakeClock,
            timeZone = TimeZone.UTC
        )
        getTicketsUseCase = GetTicketsUseCase(
            ticketRepository = fakeRepository,
            ticketStatusResolver = statusResolver
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads tickets and reflects empty when no tickets exist`() = runTest(testDispatcher) {
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)

        viewModel.state.test {
            // Initial loading or loaded empty
            val initial = awaitItem()
            if (initial.isLoading) {
                val loaded = awaitItem()
                assertFalse(loaded.isLoading)
                assertTrue(loaded.isEmpty)
                assertTrue(loaded.upcomingTickets.isEmpty())
                assertTrue(loaded.completedTickets.isEmpty())
            } else {
                assertTrue(initial.isEmpty)
            }
        }
    }

    @Test
    fun `loads tickets and partitions into upcoming and completed groups`() = runTest(testDispatcher) {
        // Upcoming ticket: 2026-11-12T15:00:00Z (after 12:00:00Z)
        fakeRepository.saveTicket(createSampleTicket(code = "MTX-111111", showtime = "2026-11-12T15:00:00Z"))
        // Expired ticket: 2026-11-12T09:00:00Z (before 12:00:00Z)
        fakeRepository.saveTicket(createSampleTicket(code = "MTX-222222", showtime = "2026-11-12T09:00:00Z"))

        val viewModel = TicketHistoryViewModel(getTicketsUseCase)

        viewModel.state.test {
            val item = awaitItem()
            val state = if (item.isLoading) awaitItem() else item

            assertFalse(state.isLoading)
            assertFalse(state.isEmpty)
            assertEquals(1, state.upcomingTickets.size)
            assertEquals("MTX-111111", state.upcomingTickets[0].ticket.bookingCode)
            assertEquals(1, state.completedTickets.size)
            assertEquals("MTX-222222", state.completedTickets[0].ticket.bookingCode)
        }
    }

    @Test
    fun `displays error message when repository fails to load tickets`() = runTest(testDispatcher) {
        fakeRepository.shouldFailGet = true
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)

        viewModel.state.test {
            val item = awaitItem()
            val state = if (item.isLoading) awaitItem() else item

            assertFalse(state.isLoading)
            assertNotNull(state.errorMessage)
        }
    }

    @Test
    fun `TicketClicked emits NavigateToDetail effect`() = runTest(testDispatcher) {
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(Action.TicketClicked("MTX-999999"))
            val effect = awaitItem()
            assertEquals(Effect.NavigateToDetail("MTX-999999"), effect)
        }
    }

    @Test
    fun `ExploreMoviesClicked emits NavigateToHome effect`() = runTest(testDispatcher) {
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(Action.ExploreMoviesClicked)
            val effect = awaitItem()
            assertEquals(Effect.NavigateToHome, effect)
        }
    }

    @Test
    fun `double click on ticket is prevented`() = runTest(testDispatcher) {
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(Action.TicketClicked("MTX-123456"))
            viewModel.onAction(Action.TicketClicked("MTX-123456"))

            val first = awaitItem()
            assertEquals(Effect.NavigateToDetail("MTX-123456"), first)
            expectNoEvents()
        }
    }

    @Test
    fun `RetryClicked reloads tickets`() = runTest(testDispatcher) {
        fakeRepository.shouldFailGet = true
        val viewModel = TicketHistoryViewModel(getTicketsUseCase)
        advanceUntilIdle()

        assertEquals("Gagal memuat riwayat tiket. Silakan coba lagi.", viewModel.state.value.errorMessage)

        fakeRepository.shouldFailGet = false
        fakeRepository.saveTicket(createSampleTicket("MTX-777777", "2026-11-12T16:00:00Z"))

        viewModel.onAction(Action.RetryClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.errorMessage)
        assertEquals(1, viewModel.state.value.upcomingTickets.size)
    }

    private fun createSampleTicket(
        code: String,
        showtime: String
    ): Ticket {
        return Ticket(
            bookingCode = code,
            movieId = 10,
            movieTitle = "Sample Movie",
            posterUrl = null,
            backdropUrl = null,
            cinemaName = "Cinema XXI",
            studioName = "Studio 1",
            studioType = "Regular",
            showtimeIso = showtime,
            seats = listOf("A1", "A2"),
            pricePerSeat = 50000L,
            serviceFee = 3000L,
            totalPrice = 103000L,
            paymentMethod = PaymentMethod.BCA_VA,
            transactionTimeEpochMs = 1700000000000L,
            qrPayload = "MOVIETIX:v1:$code:sch1:A1,A2"
        )
    }
}
