package org.ariqhikari.movietix.domain.ticket.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TicketStatusResolverTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private val timeZone = TimeZone.of("Asia/Jakarta")

    // 2026-11-12 15:30:00 Asia/Jakarta is 2026-11-12T08:30:00Z
    private val showtimeIso = "2026-11-12T15:30:00"

    private fun createTicket(
        showtime: String = showtimeIso,
        status: TicketStatus = TicketStatus.ACTIVE
    ): Ticket = Ticket(
        bookingCode = "MTX-123456",
        movieId = 101,
        movieTitle = "Dune: Part Two",
        posterUrl = null,
        backdropUrl = null,
        cinemaName = "Grand Indonesia XXI",
        studioName = "Studio 1",
        studioType = "IMAX 2D",
        showtimeIso = showtime,
        seats = listOf("C5", "C6"),
        pricePerSeat = 65000L,
        serviceFee = 3000L,
        totalPrice = 133000L,
        paymentMethod = PaymentMethod.BCA_VA,
        transactionTimeEpochMs = 1731400000000L,
        status = status,
        qrPayload = "MOVIETIX:v1:MTX-123456:sch_1:C5,C6"
    )

    @Test
    fun resolve_cancelledTicket_alwaysReturnsCancelledRegardlessOfTime() {
        val fixedInstant = Instant.parse("2026-11-12T05:00:00Z") // way before showtime
        val resolver = TicketStatusResolver(clock = FixedClock(fixedInstant), timeZone = timeZone)
        val ticket = createTicket(status = TicketStatus.CANCELLED)

        val result = resolver.resolve(ticket)
        assertEquals(TicketStatus.CANCELLED, result)

        val display = resolver.toDisplayItem(ticket)
        assertTrue(display.isCancelled)
        assertFalse(display.isActive)
        assertFalse(display.isExpired)
    }

    @Test
    fun resolve_beforeShowtime_returnsActive() {
        // 14:00 Jakarta (07:00 UTC) -> 1.5 hours before 15:30
        val fixedInstant = Instant.parse("2026-11-12T07:00:00Z")
        val resolver = TicketStatusResolver(clock = FixedClock(fixedInstant), timeZone = timeZone)
        val ticket = createTicket(status = TicketStatus.ACTIVE)

        val result = resolver.resolve(ticket)
        assertEquals(TicketStatus.ACTIVE, result)

        val display = resolver.toDisplayItem(ticket)
        assertTrue(display.isActive)
        assertFalse(display.isExpired)
        assertFalse(display.isCancelled)
    }

    @Test
    fun resolve_exactShowtime_returnsActive() {
        // 15:30 Jakarta (08:30 UTC) -> exactly equal to showtime
        val fixedInstant = Instant.parse("2026-11-12T08:30:00Z")
        val resolver = TicketStatusResolver(clock = FixedClock(fixedInstant), timeZone = timeZone)
        val ticket = createTicket(status = TicketStatus.ACTIVE)

        val result = resolver.resolve(ticket)
        assertEquals(TicketStatus.ACTIVE, result)

        val display = resolver.toDisplayItem(ticket)
        assertTrue(display.isActive)
    }

    @Test
    fun resolve_afterShowtime_returnsExpired() {
        // 15:30:01 Jakarta (08:30:01 UTC) -> strictly after showtime
        val fixedInstant = Instant.parse("2026-11-12T08:30:01Z")
        val resolver = TicketStatusResolver(clock = FixedClock(fixedInstant), timeZone = timeZone)
        val ticket = createTicket(status = TicketStatus.ACTIVE)

        val result = resolver.resolve(ticket)
        assertEquals(TicketStatus.EXPIRED, result)

        val display = resolver.toDisplayItem(ticket)
        assertTrue(display.isExpired)
        assertFalse(display.isActive)
    }

    @Test
    fun resolve_utcIsoInstantShowtime_resolvesCorrectly() {
        val utcShowtime = "2026-11-12T15:30:00Z"
        val fixedInstantBefore = Instant.parse("2026-11-12T15:29:00Z")
        val fixedInstantAfter = Instant.parse("2026-11-12T15:31:00Z")

        val resolverBefore = TicketStatusResolver(clock = FixedClock(fixedInstantBefore), timeZone = timeZone)
        val resolverAfter = TicketStatusResolver(clock = FixedClock(fixedInstantAfter), timeZone = timeZone)

        assertEquals(TicketStatus.ACTIVE, resolverBefore.resolve(createTicket(showtime = utcShowtime)))
        assertEquals(TicketStatus.EXPIRED, resolverAfter.resolve(createTicket(showtime = utcShowtime)))
    }
}
