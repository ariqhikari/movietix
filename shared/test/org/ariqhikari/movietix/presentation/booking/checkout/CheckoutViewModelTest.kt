package org.ariqhikari.movietix.presentation.booking.checkout

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.usecase.ConfirmBookingUseCase
import org.ariqhikari.movietix.domain.booking.util.BookingCodeGenerator
import org.ariqhikari.movietix.fake.FakeCinemaScheduleRepository
import org.ariqhikari.movietix.fake.FakePaymentGateway
import org.ariqhikari.movietix.fake.FakeTicketRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeCinemaRepository: FakeCinemaScheduleRepository
    private lateinit var fakePaymentGateway: FakePaymentGateway
    private lateinit var fakeTicketRepository: FakeTicketRepository
    private lateinit var confirmBookingUseCase: ConfirmBookingUseCase
    private lateinit var viewModel: CheckoutViewModel

    private val sampleOrder = BookingOrder(
        scheduleId = "sch_100_cn1_st1_20261112_1430",
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
        Dispatchers.setMain(testDispatcher)

        fakeCinemaRepository = FakeCinemaScheduleRepository()
        fakePaymentGateway = FakePaymentGateway()
        fakeTicketRepository = FakeTicketRepository()

        confirmBookingUseCase = ConfirmBookingUseCase(
            cinemaScheduleRepository = fakeCinemaRepository,
            paymentGateway = fakePaymentGateway,
            ticketRepository = fakeTicketRepository,
            bookingCodeGenerator = BookingCodeGenerator()
        )

        viewModel = CheckoutViewModel(
            confirmBookingUseCase = confirmBookingUseCase,
            initialOrder = sampleOrder,
            isDebug = true
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasCorrectOrderAndDefaultBcaVa() {
        val state = viewModel.state.value
        assertEquals(sampleOrder, state.order)
        assertEquals(PaymentMethod.BCA_VA, state.selectedPaymentMethod)
        assertFalse(state.isPaymentSheetVisible)
        assertIs<PaymentStatus.Idle>(state.paymentStatus)
        assertTrue(state.isDebug)
        assertFalse(state.isDebugSimulateFailure)
    }

    @Test
    fun paymentMethodSelected_updatesSelectedMethodAndDismissesSheet() {
        viewModel.onAction(CheckoutAction.PaymentMethodClicked)
        assertTrue(viewModel.state.value.isPaymentSheetVisible)

        viewModel.onAction(CheckoutAction.PaymentMethodSelected(PaymentMethod.GOPAY))
        assertEquals(PaymentMethod.GOPAY, viewModel.state.value.selectedPaymentMethod)
        assertFalse(viewModel.state.value.isPaymentSheetVisible)
    }

    @Test
    fun payClicked_successFlow_updatesStateToProcessingThenSuccess() = runTest(testDispatcher) {
        viewModel.state.test {
            assertEquals(PaymentStatus.Idle, awaitItem().paymentStatus)

            viewModel.onAction(CheckoutAction.PayClicked)

            // Step 1: Processing
            val processingState = awaitItem()
            assertEquals(PaymentStatus.Processing, processingState.paymentStatus)
            assertTrue(processingState.isProcessing)

            // Step 2: Success
            val successState = awaitItem()
            assertIs<PaymentStatus.Success>(successState.paymentStatus)
            assertEquals("Avatar 3", successState.confirmedTicket?.movieTitle)
            assertEquals(PaymentMethod.BCA_VA, successState.confirmedTicket?.paymentMethod)
        }
    }

    @Test
    fun payClicked_doubleClickPrevented_processesPaymentOnlyOnce() = runTest(testDispatcher) {
        fakePaymentGateway.delayMillis = 2000L

        viewModel.onAction(CheckoutAction.PayClicked)
        // Immediately trigger second pay click while processing
        viewModel.onAction(CheckoutAction.PayClicked)

        testScheduler.advanceTimeBy(3000)

        // Gateway should only be called once
        assertEquals(1, fakePaymentGateway.processCallCount)
    }

    @Test
    fun payClicked_seatOccupiedError_setsFailureStateWithCanRetryFalse() = runTest(testDispatcher) {
        fakeCinemaRepository.shouldFailReserve = true

        viewModel.state.test {
            awaitItem() // Initial Idle

            viewModel.onAction(CheckoutAction.PayClicked)
            awaitItem() // Processing

            val failureState = awaitItem()
            assertIs<PaymentStatus.Failure>(failureState.paymentStatus)
            val failure = failureState.paymentStatus as PaymentStatus.Failure
            assertFalse(failure.canRetry, "When seat is occupied, canRetry must be false")
            assertTrue(failure.message.contains("sudah terisi"))
        }
    }

    @Test
    fun payClicked_paymentFailedError_setsFailureStateWithCanRetryTrue() = runTest(testDispatcher) {
        fakePaymentGateway.shouldFailPayment = true

        viewModel.state.test {
            awaitItem() // Initial Idle

            viewModel.onAction(CheckoutAction.PayClicked)
            awaitItem() // Processing

            val failureState = awaitItem()
            assertIs<PaymentStatus.Failure>(failureState.paymentStatus)
            val failure = failureState.paymentStatus as PaymentStatus.Failure
            assertTrue(failure.canRetry, "When payment fails, canRetry must be true")
            assertTrue(failure.message.contains("Pembayaran gagal"))
        }
    }

    @Test
    fun retryPaymentClicked_reinitiatesPayment() = runTest(testDispatcher) {
        fakePaymentGateway.shouldFailPayment = true
        viewModel.onAction(CheckoutAction.PayClicked)
        testScheduler.runCurrent()

        assertIs<PaymentStatus.Failure>(viewModel.state.value.paymentStatus)

        // Fix payment gateway and retry
        fakePaymentGateway.shouldFailPayment = false
        viewModel.onAction(CheckoutAction.RetryPaymentClicked)
        testScheduler.runCurrent()

        assertIs<PaymentStatus.Success>(viewModel.state.value.paymentStatus)
    }

    @Test
    fun debugSimulateFailureToggled_updatesDebugFlag() {
        assertFalse(viewModel.state.value.isDebugSimulateFailure)
        viewModel.onAction(CheckoutAction.DebugSimulateFailureToggled(true))
        assertTrue(viewModel.state.value.isDebugSimulateFailure)
        viewModel.onAction(CheckoutAction.DebugSimulateFailureToggled(false))
        assertFalse(viewModel.state.value.isDebugSimulateFailure)
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest(testDispatcher) {
        viewModel.effect.test {
            viewModel.onAction(CheckoutAction.BackClicked)
            assertEquals(CheckoutEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun backToHomeClicked_emitsNavigateToHomeEffect() = runTest(testDispatcher) {
        viewModel.effect.test {
            viewModel.onAction(CheckoutAction.BackToHomeClicked)
            assertEquals(CheckoutEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun viewTicketClicked_withConfirmedTicket_emitsNavigateToTicketDetailEffect() = runTest(testDispatcher) {
        viewModel.onAction(CheckoutAction.PayClicked)
        testScheduler.runCurrent()

        val ticket = viewModel.state.value.confirmedTicket
        assertTrue(ticket != null)

        viewModel.effect.test {
            viewModel.onAction(CheckoutAction.ViewTicketClicked)
            val effect = awaitItem()
            assertIs<CheckoutEffect.NavigateToTicketDetail>(effect)
            assertEquals(ticket.bookingCode, effect.bookingCode)
        }
    }
}
