package org.ariqhikari.movietix.integration

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
import org.ariqhikari.movietix.data.booking.remote.DummyPaymentGateway
import org.ariqhikari.movietix.data.cinema.local.ProceduralScheduleEngine
import org.ariqhikari.movietix.data.cinema.repository.CinemaScheduleRepositoryImpl
import org.ariqhikari.movietix.data.ticket.repository.TicketRepositoryImpl
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.usecase.ConfirmBookingUseCase
import org.ariqhikari.movietix.domain.booking.util.BookingCodeGenerator
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import org.ariqhikari.movietix.domain.cinema.usecase.CalculateBookingPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetCinemasWithSchedulesUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetSeatLayoutUseCase
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketsUseCase
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-End Booking Flow Integration Test.
 * Verifies full interaction across:
 * - Real ProceduralScheduleEngine
 * - Real CinemaScheduleRepositoryImpl with DataStore backing
 * - Real TicketRepositoryImpl with DataStore backing
 * - Real DummyPaymentGateway & BookingCodeGenerator
 * - Use cases: GetCinemasWithSchedules, GetSeatLayout, CalculateBookingPrice, ConfirmBooking, GetTickets.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookingFlowIntegrationTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val fixedInstant = Instant.parse("2026-11-12T10:00:00Z")
    private val fixedClock = FixedClock(fixedInstant)
    private val timeZone = TimeZone.of("Asia/Jakarta")
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var scheduleEngine: ProceduralScheduleEngine
    private lateinit var cinemaRepository: CinemaScheduleRepositoryImpl
    private lateinit var ticketRepository: TicketRepositoryImpl
    private lateinit var paymentGateway: DummyPaymentGateway
    private lateinit var bookingCodeGenerator: BookingCodeGenerator

    private lateinit var getCinemasWithSchedulesUseCase: GetCinemasWithSchedulesUseCase
    private lateinit var getSeatLayoutUseCase: GetSeatLayoutUseCase
    private lateinit var calculateBookingPriceUseCase: CalculateBookingPriceUseCase
    private lateinit var confirmBookingUseCase: ConfirmBookingUseCase
    private lateinit var getTicketsUseCase: GetTicketsUseCase

    private fun createTestDataStore(prefix: String): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        val randomSuffix = Random.nextLong()
        val tempPath = "${okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/${prefix}_${randomSuffix}.preferences_pb".toPath()
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempPath }
        )
    }

    @BeforeTest
    fun setup() {
        scheduleEngine = ProceduralScheduleEngine(
            clock = fixedClock,
            timeZone = timeZone
        )

        val cinemaDataStore = createTestDataStore("int_cinema")
        val ticketDataStore = createTestDataStore("int_ticket")

        cinemaRepository = CinemaScheduleRepositoryImpl(
            proceduralEngine = scheduleEngine,
            dataStore = cinemaDataStore,
            dispatcher = testDispatcher
        )

        ticketRepository = TicketRepositoryImpl(
            dataStore = ticketDataStore,
            dispatcher = testDispatcher
        )

        paymentGateway = DummyPaymentGateway()
        bookingCodeGenerator = BookingCodeGenerator()

        getCinemasWithSchedulesUseCase = GetCinemasWithSchedulesUseCase(cinemaRepository)
        getSeatLayoutUseCase = GetSeatLayoutUseCase(cinemaRepository)
        calculateBookingPriceUseCase = CalculateBookingPriceUseCase()

        confirmBookingUseCase = ConfirmBookingUseCase(
            cinemaScheduleRepository = cinemaRepository,
            paymentGateway = paymentGateway,
            ticketRepository = ticketRepository,
            bookingCodeGenerator = bookingCodeGenerator,
            clock = fixedClock
        )

        val statusResolver = TicketStatusResolver(clock = fixedClock, timeZone = timeZone)
        getTicketsUseCase = GetTicketsUseCase(ticketRepository, statusResolver)
    }

    @Test
    fun fullEndToEndBookingFlow_success_conflict_and_rollback() = runTest(testDispatcher) {
        val movieId = 550
        val targetDate = LocalDate(2026, 11, 12)

        // Step 1: Query schedules for movie 550
        var targetScheduleId = ""
        var targetCinemaName = ""
        var targetStudioName = ""
        var targetStudioType = ""
        var targetShowtime = ""
        var pricePerSeat = 0L

        getCinemasWithSchedulesUseCase(movieId, targetDate).test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val cinemas = (result as AppResult.Success).data
            assertTrue(cinemas.isNotEmpty())

            val firstCinema = cinemas.first()
            val firstStudio = firstCinema.studios.first()
            val firstShowtime = firstStudio.showtimes.first()

            targetScheduleId = firstShowtime.id
            targetCinemaName = firstCinema.name
            targetStudioName = firstStudio.name
            targetStudioType = firstStudio.type.name
            targetShowtime = firstShowtime.time
            pricePerSeat = firstShowtime.price
            awaitComplete()
        }

        assertTrue(targetScheduleId.isNotBlank())

        // Step 2: Inspect Seat Layout
        var availableSeats = listOf<String>()
        getSeatLayoutUseCase(
            scheduleId = targetScheduleId,
            movieId = movieId,
            cinemaId = "cn_gi",
            studioId = "st_1",
            date = targetDate,
            time = targetShowtime
        ).test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val layout = (result as AppResult.Success).data
            availableSeats = layout.seats.filter { it.status == SeatStatus.AVAILABLE }.map { it.id }
            assertTrue(availableSeats.size >= 2, "Expected at least 2 available seats")
            cancelAndIgnoreRemainingEvents()
        }

        val chosenSeats = availableSeats.take(2)
        assertEquals(2, chosenSeats.size)

        // Step 3: Calculate Price Breakdown
        val priceBreakdown = calculateBookingPriceUseCase(
            seatCount = chosenSeats.size,
            pricePerSeat = pricePerSeat
        )
        assertEquals(pricePerSeat * 2, priceBreakdown.subtotal)
        assertEquals(6000L, priceBreakdown.serviceFee)
        assertEquals((pricePerSeat * 2) + 6000L, priceBreakdown.total)

        // Step 4: Confirm Booking (Happy Path)
        val validOrder = BookingOrder(
            scheduleId = targetScheduleId,
            movieId = movieId,
            movieTitle = "Fight Club",
            cinemaId = "cn_gi",
            cinemaName = targetCinemaName,
            studioName = targetStudioName,
            studioType = targetStudioType,
            showtime = targetShowtime,
            date = targetDate.toString(),
            selectedSeats = chosenSeats,
            pricePerSeat = pricePerSeat,
            serviceFee = priceBreakdown.serviceFee,
            totalPrice = priceBreakdown.total,
            posterUrl = "https://image.tmdb.org/t/p/w500/sample.jpg"
        )

        val confirmResult = confirmBookingUseCase(
            order = validOrder,
            paymentMethod = PaymentMethod.BCA_VA,
            simulateFailure = false
        )

        assertIs<AppResult.Success<*>>(confirmResult)
        val confirmedTicket = (confirmResult as AppResult.Success).data
        assertNotNull(confirmedTicket.bookingCode)
        assertEquals(TicketStatus.ACTIVE, confirmedTicket.status)
        assertTrue(confirmedTicket.qrPayload.startsWith("MOVIETIX:v1:${confirmedTicket.bookingCode}:"))

        // Step 5: Verify Seat Map shows booked seats as OCCUPIED
        getSeatLayoutUseCase(
            scheduleId = targetScheduleId,
            movieId = movieId,
            cinemaId = "cn_gi",
            studioId = "st_1",
            date = targetDate,
            time = targetShowtime
        ).test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val layout = (result as AppResult.Success).data
            val newlyOccupied = layout.seats.filter { it.id in chosenSeats }
            assertEquals(2, newlyOccupied.size)
            assertTrue(newlyOccupied.all { it.status == SeatStatus.OCCUPIED })
            cancelAndIgnoreRemainingEvents()
        }

        // Step 6: Verify Conflict Scenario (Attempting to book already-reserved seats)
        val conflictingOrder = validOrder.copy(
            selectedSeats = chosenSeats
        )
        val conflictResult = confirmBookingUseCase(
            order = conflictingOrder,
            paymentMethod = PaymentMethod.GOPAY,
            simulateFailure = false
        )
        assertIs<AppResult.Error>(conflictResult)
        assertEquals(AppError.Booking.SeatAlreadyOccupied, conflictResult.error)

        // Step 7: Verify Simulated Failure and Automatic Rollback
        // Pick 2 other available seats
        var remainingAvailable = listOf<String>()
        getSeatLayoutUseCase(
            scheduleId = targetScheduleId,
            movieId = movieId,
            cinemaId = "cn_gi",
            studioId = "st_1",
            date = targetDate,
            time = targetShowtime
        ).test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val layout = (result as AppResult.Success).data
            remainingAvailable = layout.seats.filter { it.status == SeatStatus.AVAILABLE }.map { it.id }
            cancelAndIgnoreRemainingEvents()
        }

        val rollbackTestSeats = remainingAvailable.take(2)
        assertTrue(rollbackTestSeats.isNotEmpty())

        val failingOrder = validOrder.copy(
            selectedSeats = rollbackTestSeats
        )

        val failureResult = confirmBookingUseCase(
            order = failingOrder,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            simulateFailure = true // Gateway will simulate failure
        )

        assertIs<AppResult.Error>(failureResult)
        assertEquals(AppError.Booking.PaymentFailedSimulated, failureResult.error)

        // Verify seats were released back to AVAILABLE after rollback
        getSeatLayoutUseCase(
            scheduleId = targetScheduleId,
            movieId = movieId,
            cinemaId = "cn_gi",
            studioId = "st_1",
            date = targetDate,
            time = targetShowtime
        ).test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val layout = (result as AppResult.Success).data
            val rolledBackSeats = layout.seats.filter { it.id in rollbackTestSeats }
            assertTrue(rolledBackSeats.all { it.status == SeatStatus.AVAILABLE })
            cancelAndIgnoreRemainingEvents()
        }

        // Step 8: Verify Ticket appears in GetTicketsUseCase
        getTicketsUseCase().test {
            val result = awaitItem()
            assertIs<AppResult.Success<*>>(result)
            val groups = (result as AppResult.Success).data
            assertEquals(1, groups.upcoming.size)
            assertEquals(confirmedTicket.bookingCode, groups.upcoming.first().ticket.bookingCode)
            assertEquals(0, groups.completed.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
