package org.ariqhikari.movietix.domain.booking.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.util.BookingCodeGenerator
import org.ariqhikari.movietix.fake.FakeCinemaScheduleRepository
import org.ariqhikari.movietix.fake.FakePaymentGateway
import org.ariqhikari.movietix.fake.FakeTicketRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmBookingUseCaseTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val fixedClock = FixedClock(Instant.parse("2026-11-12T10:00:00Z"))
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeCinemaRepository: FakeCinemaScheduleRepository
    private lateinit var fakePaymentGateway: FakePaymentGateway
    private lateinit var fakeTicketRepository: FakeTicketRepository
    private lateinit var useCase: ConfirmBookingUseCase

    private val sampleOrder = BookingOrder(
        scheduleId = "sch_mv100_cn1_st1_20261112_1430",
        movieId = 100,
        movieTitle = "Avatar 3",
        cinemaId = "cn1",
        cinemaName = "Grand Indonesia CGV",
        studioName = "Studio 1",
        studioType = "IMAX",
        showtime = "14:30",
        date = "2026-11-12",
        selectedSeats = listOf("C5", "C6"),
        pricePerSeat = 75000L,
        serviceFee = 5000L,
        totalPrice = 155000L,
        posterUrl = "https://image.tmdb.org/t/p/w500/sample.jpg"
    )

    @BeforeTest
    fun setup() {
        fakeCinemaRepository = FakeCinemaScheduleRepository()
        fakePaymentGateway = FakePaymentGateway()
        fakeTicketRepository = FakeTicketRepository()

        useCase = ConfirmBookingUseCase(
            cinemaScheduleRepository = fakeCinemaRepository,
            paymentGateway = fakePaymentGateway,
            ticketRepository = fakeTicketRepository,
            bookingCodeGenerator = BookingCodeGenerator(),
            clock = fixedClock
        )
    }

    @Test
    fun confirmBooking_success_reservesSeatsProcessesPaymentAndSavesTicket() = runTest(testDispatcher) {
        val result = useCase(sampleOrder, PaymentMethod.BCA_VA)

        assertIs<AppResult.Success<org.ariqhikari.movietix.domain.ticket.model.Ticket>>(result)
        val ticket = result.data
        assertEquals("Avatar 3", ticket.movieTitle)
        assertEquals(PaymentMethod.BCA_VA, ticket.paymentMethod)
        assertEquals(155000L, ticket.totalPrice)
        assertEquals(listOf("C5", "C6"), ticket.seats)
        assertTrue(ticket.bookingCode.startsWith("MTX-"))
        assertTrue(ticket.qrPayload.startsWith("MOVIETIX:v1:"))

        // Verify seats are reserved in cinema repo
        assertEquals(setOf("C5", "C6"), fakeCinemaRepository.getReservedSeats(sampleOrder.scheduleId))
        // Verify payment gateway called
        assertEquals(1, fakePaymentGateway.processCallCount)
        // Verify ticket saved in repo
        assertEquals(1, fakeTicketRepository.getAllTickets().size)
        // Verify no rollback occurred
        assertEquals(0, fakeCinemaRepository.releaseSeatsCallCount)
    }

    @Test
    fun confirmBooking_seatAlreadyOccupied_failsImmediatelyWithoutPaymentOrSave() = runTest(testDispatcher) {
        fakeCinemaRepository.shouldFailReserve = true

        val result = useCase(sampleOrder, PaymentMethod.GOPAY)

        assertIs<AppResult.Error>(result)
        assertIs<AppError.Booking.SeatAlreadyOccupied>(result.error)

        assertEquals(0, fakePaymentGateway.processCallCount, "Payment must NOT be processed if seat is occupied")
        assertEquals(0, fakeTicketRepository.getAllTickets().size, "Ticket must NOT be saved")
        assertEquals(0, fakeCinemaRepository.releaseSeatsCallCount, "No seats were reserved so no release needed")
    }

    @Test
    fun confirmBooking_paymentFails_releasesSeatsAndReturnsPaymentFailed() = runTest(testDispatcher) {
        fakePaymentGateway.shouldFailPayment = true

        val result = useCase(sampleOrder, PaymentMethod.OVO)

        assertIs<AppResult.Error>(result)
        assertIs<AppError.Booking.PaymentFailedSimulated>(result.error)

        // Seats should be rolled back / released
        assertEquals(1, fakeCinemaRepository.releaseSeatsCallCount)
        assertEquals(sampleOrder.scheduleId, fakeCinemaRepository.lastReleasedScheduleId)
        assertEquals(listOf("C5", "C6"), fakeCinemaRepository.lastReleasedSeats)
        // Ticket must NOT be saved
        assertEquals(0, fakeTicketRepository.getAllTickets().size)
    }

    @Test
    fun confirmBooking_saveTicketFails_releasesSeatsAndReturnsSaveTicketFailed() = runTest(testDispatcher) {
        fakeTicketRepository.shouldFailSave = true
        fakeTicketRepository.saveFailureCause = "Disk I/O error"

        val result = useCase(sampleOrder, PaymentMethod.CREDIT_CARD)

        assertIs<AppResult.Error>(result)
        assertIs<AppError.Booking.SaveTicketFailed>(result.error)
        assertEquals("Disk I/O error", (result.error as AppError.Booking.SaveTicketFailed).cause)

        // Rollback executed
        assertEquals(1, fakeCinemaRepository.releaseSeatsCallCount)
        assertEquals(listOf("C5", "C6"), fakeCinemaRepository.lastReleasedSeats)
    }

    @Test
    fun confirmBooking_paymentFailsAndReleaseFails_returnsRollbackFailed() = runTest(testDispatcher) {
        fakePaymentGateway.shouldFailPayment = true
        fakeCinemaRepository.shouldFailRelease = true

        val result = useCase(sampleOrder, PaymentMethod.BCA_VA)

        assertIs<AppResult.Error>(result)
        assertIs<AppError.Booking.RollbackFailed>(result.error)
        val rollbackError = result.error as AppError.Booking.RollbackFailed
        assertIs<AppError.Booking.PaymentFailedSimulated>(rollbackError.originalError)
        assertEquals(1, fakeCinemaRepository.releaseSeatsCallCount)
    }

    @Test
    fun confirmBooking_coroutineCancelledDuringPayment_executesNonCancellableReleaseSeats() = runTest(testDispatcher) {
        fakePaymentGateway.delayMillis = 5000L

        val job = launch {
            useCase(sampleOrder, PaymentMethod.BCA_VA)
        }

        testScheduler.advanceTimeBy(100)
        job.cancelAndJoin()

        // Verify seats were released despite cancellation
        assertEquals(1, fakeCinemaRepository.releaseSeatsCallCount)
        assertEquals(listOf("C5", "C6"), fakeCinemaRepository.lastReleasedSeats)
    }
}
