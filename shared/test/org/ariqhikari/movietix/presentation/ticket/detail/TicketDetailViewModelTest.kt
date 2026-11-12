package org.ariqhikari.movietix.presentation.ticket.detail

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
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketByBookingCodeUseCase
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import org.ariqhikari.movietix.fake.FakeTicketRepository
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.Action
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.Effect
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TicketDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeTicketRepository
    private lateinit var statusResolver: TicketStatusResolver
    private lateinit var getTicketByBookingCodeUseCase: GetTicketByBookingCodeUseCase

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
        getTicketByBookingCodeUseCase = GetTicketByBookingCodeUseCase(
            ticketRepository = fakeRepository,
            ticketStatusResolver = statusResolver
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads ticket successfully and resolves active status`() = runTest(testDispatcher) {
        val sampleTicket = createSampleTicket("MTX-123456", "2026-11-12T15:00:00Z")
        fakeRepository.saveTicket(sampleTicket)

        val viewModel = TicketDetailViewModel("MTX-123456", getTicketByBookingCodeUseCase)

        viewModel.state.test {
            val item = awaitItem()
            val state = if (item.isLoading) awaitItem() else item

            assertFalse(state.isLoading)
            assertNotNull(state.ticketItem)
            assertEquals("MTX-123456", state.ticketItem?.ticket?.bookingCode)
            assertEquals(TicketStatus.ACTIVE, state.ticketItem?.derivedStatus)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `sets error state when ticket is not found`() = runTest(testDispatcher) {
        val viewModel = TicketDetailViewModel("MTX-999999", getTicketByBookingCodeUseCase)

        viewModel.state.test {
            val item = awaitItem()
            val state = if (item.isLoading) awaitItem() else item

            assertFalse(state.isLoading)
            assertNull(state.ticketItem)
            assertEquals("Tiket dengan kode booking MTX-999999 tidak ditemukan.", state.errorMessage)
        }
    }

    @Test
    fun `BackClicked emits NavigateBack effect`() = runTest(testDispatcher) {
        val viewModel = TicketDetailViewModel("MTX-123456", getTicketByBookingCodeUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onAction(Action.BackClicked)
            val effect = awaitItem()
            assertEquals(Effect.NavigateBack, effect)
        }
    }

    @Test
    fun `QrClicked and DismissQrDialog toggle isQrEnlarged state`() = runTest(testDispatcher) {
        val sampleTicket = createSampleTicket("MTX-123456", "2026-11-12T15:00:00Z")
        fakeRepository.saveTicket(sampleTicket)

        val viewModel = TicketDetailViewModel("MTX-123456", getTicketByBookingCodeUseCase)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isQrEnlarged)

        viewModel.onAction(Action.QrClicked)
        assertTrue(viewModel.state.value.isQrEnlarged)

        viewModel.onAction(Action.DismissQrDialog)
        assertFalse(viewModel.state.value.isQrEnlarged)
    }

    @Test
    fun `RetryClicked reloads ticket when previously failed`() = runTest(testDispatcher) {
        fakeRepository.shouldFailGet = true
        val viewModel = TicketDetailViewModel("MTX-123456", getTicketByBookingCodeUseCase)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)

        fakeRepository.shouldFailGet = false
        fakeRepository.saveTicket(createSampleTicket("MTX-123456", "2026-11-12T15:00:00Z"))

        viewModel.onAction(Action.RetryClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.errorMessage)
        assertNotNull(viewModel.state.value.ticketItem)
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
