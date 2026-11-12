package org.ariqhikari.movietix.domain.ticket.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.util.TicketStatusResolver
import org.ariqhikari.movietix.fake.FakeTicketRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTicketByBookingCodeUseCaseTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val timeZone = TimeZone.of("Asia/Jakarta")
    private val clockInstant = Instant.parse("2026-11-12T07:00:00Z")
    private val resolver = TicketStatusResolver(clock = FixedClock(clockInstant), timeZone = timeZone)

    private val sampleTicket = Ticket(
        bookingCode = "MTX-999888",
        movieId = 101,
        movieTitle = "Dune: Part Two",
        posterUrl = null,
        backdropUrl = null,
        cinemaName = "Grand Indonesia XXI",
        studioName = "Studio 1",
        studioType = "IMAX 2D",
        showtimeIso = "2026-11-12T15:30:00",
        seats = listOf("C5"),
        pricePerSeat = 65000L,
        serviceFee = 3000L,
        totalPrice = 68000L,
        paymentMethod = PaymentMethod.BCA_VA,
        transactionTimeEpochMs = 1731400000000L,
        status = TicketStatus.ACTIVE,
        qrPayload = "MOVIETIX:v1:MTX-999888:sch_1:C5"
    )

    @Test
    fun getTicketByBookingCode_existingCode_returnsSuccessWithDerivedStatus() = runTest {
        val repo = FakeTicketRepository()
        repo.saveTicket(sampleTicket)
        val useCase = GetTicketByBookingCodeUseCase(repo, resolver)

        val result = useCase("MTX-999888")
        assertTrue(result is AppResult.Success)
        assertEquals("MTX-999888", result.data.ticket.bookingCode)
        assertEquals(TicketStatus.ACTIVE, result.data.derivedStatus)
    }

    @Test
    fun getTicketByBookingCode_notFound_returnsError() = runTest {
        val repo = FakeTicketRepository()
        val useCase = GetTicketByBookingCodeUseCase(repo, resolver)

        val result = useCase("MTX-000000")
        assertTrue(result is AppResult.Error)
    }
}
