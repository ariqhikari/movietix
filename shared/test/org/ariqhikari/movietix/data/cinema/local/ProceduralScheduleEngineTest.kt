package org.ariqhikari.movietix.data.cinema.local

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProceduralScheduleEngineTest {

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    // Fixed instant: Wednesday 2026-11-11 08:00:00 UTC (Weekday morning)
    private val fixedClockMorning = FixedClock(Instant.parse("2026-11-11T08:00:00Z"))
    private val timeZoneUtc = TimeZone.UTC

    private val engineMorning = ProceduralScheduleEngine(
        clock = fixedClockMorning,
        timeZone = timeZoneUtc
    )

    @Test
    fun goldenTest_deterministicScheduleGeneration() {
        val movieId = 550
        val date = LocalDate(2026, 11, 11) // Wednesday

        val cinemas = engineMorning.generateCinemas(movieId, date)

        // 4 CGV Cinemas generated
        assertEquals(4, cinemas.size)
        assertEquals("cgv-gi", cinemas[0].id)
        assertEquals("CGV Grand Indonesia", cinemas[0].name)
        assertEquals(4, cinemas[0].studios.size)

        // Verify Regular 2D studio at CGV GI
        val regularStudio = cinemas[0].studios.first { it.type == StudioType.REGULAR }
        assertEquals(5, regularStudio.showtimes.size)
        assertEquals("10:30", regularStudio.showtimes[0].time)
        assertEquals(45_000L, regularStudio.showtimes[0].price) // Weekday price

        // Golden verification of occupied seat count
        val firstShowtime = regularStudio.showtimes[0]
        assertTrue(firstShowtime.occupiedSeatCount in 20..32)

        // Generating a second time with identical inputs must be 100% equal
        val cinemasSecondRun = engineMorning.generateCinemas(movieId, date)
        assertEquals(cinemas, cinemasSecondRun)
    }

    @Test
    fun goldenTest_deterministicOccupiedSeats() {
        val movieId = 693134
        val cinemaId = "cgv-gi"
        val studioId = "std-reg"
        val date = "2026-11-15"
        val time = "16:00"
        val scheduleId = "$movieId-$cinemaId-$studioId-$date-$time"

        val occupiedRun1 = engineMorning.generateOccupiedSeats(scheduleId, movieId, cinemaId, studioId, date, time)
        val occupiedRun2 = engineMorning.generateOccupiedSeats(scheduleId, movieId, cinemaId, studioId, date, time)

        // Exact deterministic set
        assertEquals(occupiedRun1, occupiedRun2)
        assertTrue(occupiedRun1.size in 20..32)

        // Check specific seats are deterministic
        val layout = engineMorning.generateSeatLayout(scheduleId, movieId, cinemaId, studioId, date, time)
        assertEquals(80, layout.seats.size)
        assertEquals(8, layout.rows.size)
        assertEquals(10, layout.totalColumns)
        assertEquals(4, layout.aisleAfterColumn)

        val occupiedInLayout = layout.seats.filter { it.status == SeatStatus.OCCUPIED }.map { it.id }.toSet()
        assertEquals(occupiedRun1, occupiedInLayout)
    }

    @Test
    fun pricing_weekdayVsWeekend() {
        val movieId = 100
        val weekdayDate = LocalDate(2026, 11, 11) // Wednesday
        val weekendDate = LocalDate(2026, 11, 14) // Saturday

        val weekdayCinemas = engineMorning.generateCinemas(movieId, weekdayDate)
        val weekendCinemas = engineMorning.generateCinemas(movieId, weekendDate)

        val weekdayRegularPrice = weekdayCinemas[0].studios.first { it.type == StudioType.REGULAR }.showtimes[0].price
        val weekendRegularPrice = weekendCinemas[0].studios.first { it.type == StudioType.REGULAR }.showtimes[0].price

        assertEquals(45_000L, weekdayRegularPrice)
        assertEquals(55_000L, weekendRegularPrice)

        val weekdayVelvetPrice = weekdayCinemas[0].studios.first { it.type == StudioType.VELVET }.showtimes[0].price
        val weekendVelvetPrice = weekendCinemas[0].studios.first { it.type == StudioType.VELVET }.showtimes[0].price

        assertEquals(100_000L, weekdayVelvetPrice)
        assertEquals(125_000L, weekendVelvetPrice)
    }

    @Test
    fun pastShowtimes_filteredOutWhenDateIsToday() {
        // Today is 2026-11-11 at 14:00 (2:00 PM)
        val fixedClockAfternoon = FixedClock(Instant.parse("2026-11-11T14:00:00Z"))
        val engineAfternoon = ProceduralScheduleEngine(
            clock = fixedClockAfternoon,
            timeZone = timeZoneUtc
        )

        val today = LocalDate(2026, 11, 11)
        val cinemasToday = engineAfternoon.generateCinemas(movieId = 200, date = today)
        val regularStudioToday = cinemasToday[0].studios.first { it.type == StudioType.REGULAR }

        // Standard slots for regular are: 10:30, 13:15, 16:00, 18:45, 21:30
        // At 14:00, 10:30 and 13:15 are in the past and should be filtered out
        val availableTimes = regularStudioToday.showtimes.map { it.time }
        assertFalse(availableTimes.contains("10:30"))
        assertFalse(availableTimes.contains("13:15"))
        assertTrue(availableTimes.contains("16:00"))
        assertTrue(availableTimes.contains("18:45"))
        assertTrue(availableTimes.contains("21:30"))
    }

    @Test
    fun futureDate_allShowtimesRetained() {
        // Even if now is 14:00 on Nov 11, tomorrow (Nov 12) must retain all morning showtimes
        val fixedClockAfternoon = FixedClock(Instant.parse("2026-11-11T14:00:00Z"))
        val engineAfternoon = ProceduralScheduleEngine(
            clock = fixedClockAfternoon,
            timeZone = timeZoneUtc
        )

        val tomorrow = LocalDate(2026, 11, 12)
        val cinemasTomorrow = engineAfternoon.generateCinemas(movieId = 200, date = tomorrow)
        val regularStudioTomorrow = cinemasTomorrow[0].studios.first { it.type == StudioType.REGULAR }

        val availableTimes = regularStudioTomorrow.showtimes.map { it.time }
        assertTrue(availableTimes.contains("10:30"))
        assertTrue(availableTimes.contains("13:15"))
        assertTrue(availableTimes.contains("16:00"))
    }
}
