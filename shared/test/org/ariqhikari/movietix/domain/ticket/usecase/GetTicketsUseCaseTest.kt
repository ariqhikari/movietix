package org.ariqhikari.movietix.domain.ticket.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import org.ariqhikari.movietix.fake.FakeTicketRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTicketsUseCaseTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val timeZone = TimeZone.of("Asia/Jakarta")
    // Reference clock: 2026-11-12 14:00 Jakarta (07:00 UTC)
    private val clockInstant = Instant.parse("2026-11-12T07:00:00Z")
    private val resolver = TicketStatusResolver(clock = FixedClock(clockInstant), timeZone = timeZone)

    private fun createTicket(
        bookingCode: String,
        showtimeIso: String,
        status: TicketStatus = TicketStatus.ACTIVE
    ): Ticket = Ticket(
        bookingCode = bookingCode,
        movieId = 101,
        movieTitle = "Dune: Part Two",
        posterUrl = null,
        backdropUrl = null,
        cinemaName = "Grand Indonesia XXI",
        studioName = "Studio 1",
        studioType = "IMAX 2D",
        showtimeIso = showtimeIso,
        seats = listOf("C5"),
        pricePerSeat = 65000L,
        serviceFee = 3000L,
        totalPrice = 68000L,
        paymentMethod = PaymentMethod.BCA_VA,
        transactionTimeEpochMs = 1731400000000L,
        status = status,
        qrPayload = "MOVIETIX:v1:$bookingCode:sch_1:C5"
    )

    @Test
    fun getTickets_partitionsIntoUpcomingAndCompletedCorrectly() = runTest {
        val fakeRepo = FakeTicketRepository()
        val useCase = GetTicketsUseCase(fakeRepo, resolver)

        // Today is 2026-11-12 14:00
        // Ticket 1: 2026-11-12 18:00 (Active, upcoming)
        // Ticket 2: 2026-11-12 15:00 (Active, upcoming)
        // Ticket 3: 2026-11-12 10:00 (Expired, completed)
        // Ticket 4: 2026-11-13 12:00 (Active, but cancelled -> completed)
        val t1 = createTicket("MTX-1", "2026-11-12T18:00:00")
        val t2 = createTicket("MTX-2", "2026-11-12T15:00:00")
        val t3 = createTicket("MTX-3", "2026-11-12T10:00:00")
        val t4 = createTicket("MTX-4", "2026-11-13T12:00:00", status = TicketStatus.CANCELLED)

        fakeRepo.saveTicket(t1)
        fakeRepo.saveTicket(t2)
        fakeRepo.saveTicket(t3)
        fakeRepo.saveTicket(t4)

        useCase().test {
            val result = awaitItem()
            assertTrue(result is AppResult.Success)
            val groups = result.data

            // Upcoming: t2 (15:00) then t1 (18:00) (ascending order)
            assertEquals(2, groups.upcoming.size)
            assertEquals("MTX-2", groups.upcoming[0].ticket.bookingCode)
            assertEquals("MTX-1", groups.upcoming[1].ticket.bookingCode)

            // Completed: t4 (2026-11-13) then t3 (2026-11-12) (descending order)
            assertEquals(2, groups.completed.size)
            assertEquals("MTX-4", groups.completed[0].ticket.bookingCode)
            assertEquals("MTX-3", groups.completed[1].ticket.bookingCode)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTickets_whenEmpty_returnsEmptyGroups() = runTest {
        val fakeRepo = FakeTicketRepository()
        val useCase = GetTicketsUseCase(fakeRepo, resolver)

        useCase().test {
            val result = awaitItem()
            assertTrue(result is AppResult.Success)
            assertTrue(result.data.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
