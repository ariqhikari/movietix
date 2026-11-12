package org.ariqhikari.movietix.data.cinema.local

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.Seat
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import org.ariqhikari.movietix.domain.cinema.model.Showtime
import org.ariqhikari.movietix.domain.cinema.model.Studio
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import kotlin.random.Random

/**
 * Pure, deterministic procedural schedule engine.
 * Generates cinema schedules, showtimes, dynamic pricing, and pre-occupied seats.
 * Free of platform dependencies.
 */
class ProceduralScheduleEngine(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    val totalColumns = 10
    val aisleAfterColumn = 4
    val totalSeats = rows.size * totalColumns // 80 seats

    private val allSeatIds: List<String> = rows.flatMap { r ->
        (1..totalColumns).map { c -> "$r$c" }
    }

    private data class CinemaTemplate(
        val id: String,
        val name: String,
        val address: String,
        val city: String,
        val studios: List<StudioTemplate>
    )

    private data class StudioTemplate(
        val id: String,
        val name: String,
        val type: StudioType,
        val standardTimeSlots: List<String>
    )

    private val cinemaTemplates = listOf(
        CinemaTemplate(
            id = "cgv-gi",
            name = "CGV Grand Indonesia",
            address = "Grand Indonesia West Mall Lt. 8, Jl. M.H. Thamrin No. 1",
            city = "Jakarta Pusat",
            studios = listOf(
                StudioTemplate("std-reg", "Regular 2D", StudioType.REGULAR, listOf("10:30", "13:15", "16:00", "18:45", "21:30")),
                StudioTemplate("std-velvet", "Velvet Class", StudioType.VELVET, listOf("11:45", "14:30", "17:15", "20:00")),
                StudioTemplate("std-4dx", "4DX 2D", StudioType.FOUR_DX, listOf("12:00", "15:00", "18:00", "21:00")),
                StudioTemplate("std-star", "Starium 2D", StudioType.STARIUM, listOf("11:00", "14:00", "17:00", "20:15"))
            )
        ),
        CinemaTemplate(
            id = "cgv-cp",
            name = "CGV Central Park",
            address = "Central Park Mall Lt. 8, Jl. Letjen S. Parman No. 28",
            city = "Jakarta Barat",
            studios = listOf(
                StudioTemplate("std-reg", "Regular 2D", StudioType.REGULAR, listOf("10:45", "13:30", "16:15", "19:00", "21:45")),
                StudioTemplate("std-4dx", "4DX 2D", StudioType.FOUR_DX, listOf("12:15", "15:15", "18:15", "21:15")),
                StudioTemplate("std-velvet", "Velvet Class", StudioType.VELVET, listOf("12:30", "15:30", "18:30", "21:30"))
            )
        ),
        CinemaTemplate(
            id = "cgv-pp",
            name = "CGV Pacific Place",
            address = "Pacific Place Mall Lt. 6, Jl. Jend. Sudirman Kav. 52-53",
            city = "Jakarta Selatan",
            studios = listOf(
                StudioTemplate("std-reg", "Regular 2D", StudioType.REGULAR, listOf("11:15", "14:00", "16:45", "19:30", "22:00")),
                StudioTemplate("std-velvet", "Velvet Class", StudioType.VELVET, listOf("12:00", "15:00", "18:00", "20:45"))
            )
        ),
        CinemaTemplate(
            id = "cgv-fx",
            name = "CGV FX Sudirman",
            address = "fX Sudirman Lt. 7, Jl. Jend. Sudirman, Pintu Satu Senayan",
            city = "Jakarta Pusat",
            studios = listOf(
                StudioTemplate("std-reg", "Regular 2D", StudioType.REGULAR, listOf("10:15", "13:00", "15:45", "18:30", "21:15")),
                StudioTemplate("std-star", "Starium 2D", StudioType.STARIUM, listOf("11:30", "14:30", "17:30", "20:30"))
            )
        )
    )

    fun generateCinemas(movieId: Int, date: LocalDate): List<Cinema> {
        val nowDateTime = clock.now().toLocalDateTime(timeZone)
        val today = nowDateTime.date
        val currentMinutes = nowDateTime.hour * 60 + nowDateTime.minute

        val isWeekend = date.dayOfWeek in listOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        return cinemaTemplates.mapNotNull { template ->
            val studios = template.studios.mapNotNull { studioTemplate ->
                val showtimes = studioTemplate.standardTimeSlots.mapNotNull { timeStr ->
                    // Filter out past showtimes if date is today
                    if (date == today) {
                        val (h, m) = timeStr.split(":").map { it.toInt() }
                        val slotMinutes = h * 60 + m
                        if (slotMinutes <= currentMinutes) {
                            return@mapNotNull null
                        }
                    } else if (date < today) {
                        return@mapNotNull null
                    }

                    val scheduleId = createScheduleId(movieId, template.id, studioTemplate.id, date.toString(), timeStr)
                    val price = calculatePrice(studioTemplate.type, isWeekend)
                    val occupiedSeats = generateOccupiedSeats(scheduleId, movieId, template.id, studioTemplate.id, date.toString(), timeStr)

                    Showtime(
                        id = scheduleId,
                        time = timeStr,
                        price = price,
                        occupiedSeatCount = occupiedSeats.size,
                        totalSeatCount = totalSeats
                    )
                }

                if (showtimes.isNotEmpty()) {
                    Studio(
                        id = studioTemplate.id,
                        name = studioTemplate.name,
                        type = studioTemplate.type,
                        showtimes = showtimes
                    )
                } else null
            }

            if (studios.isNotEmpty()) {
                Cinema(
                    id = template.id,
                    name = template.name,
                    address = template.address,
                    city = template.city,
                    studios = studios
                )
            } else null
        }
    }

    fun generateSeatLayout(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: String,
        time: String,
        userBookedSeats: Set<String> = emptySet()
    ): SeatLayout {
        val engineOccupied = generateOccupiedSeats(scheduleId, movieId, cinemaId, studioId, date, time)
        val allOccupied = engineOccupied + userBookedSeats

        val seats = rows.flatMap { rowLetter ->
            (1..totalColumns).map { colNumber ->
                val seatId = "$rowLetter$colNumber"
                val status = if (allOccupied.contains(seatId)) {
                    SeatStatus.OCCUPIED
                } else {
                    SeatStatus.AVAILABLE
                }
                Seat(
                    id = seatId,
                    row = rowLetter,
                    column = colNumber,
                    status = status
                )
            }
        }

        return SeatLayout(
            scheduleId = scheduleId,
            rows = rows,
            totalColumns = totalColumns,
            aisleAfterColumn = aisleAfterColumn,
            seats = seats
        )
    }

    fun generateOccupiedSeats(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: String,
        time: String
    ): Set<String> {
        val seedInput = "$movieId-$date-$cinemaId-$studioId-$time"
        // Avoid abs(Int.MIN_VALUE) overflow bug by bitwise AND with Int.MAX_VALUE
        val seed = seedInput.hashCode() and Int.MAX_VALUE
        val rng = Random(seed)

        // 25% to 40% of 80 seats = 20 to 32 seats
        val count = 20 + rng.nextInt(13) // [20, 32]
        val shuffled = allSeatIds.shuffled(rng)
        return shuffled.take(count).toSet()
    }

    fun createScheduleId(movieId: Int, cinemaId: String, studioId: String, date: String, time: String): String {
        return "$movieId-$cinemaId-$studioId-$date-$time"
    }

    data class ParsedScheduleId(
        val movieId: Int,
        val cinemaId: String,
        val studioId: String,
        val date: String,
        val time: String
    )

    fun parseScheduleId(scheduleId: String): ParsedScheduleId? {
        return try {
            val parts = scheduleId.split("-")
            if (parts.size < 6) return null
            val movieId = parts[0].toIntOrNull() ?: return null
            val time = parts.last()
            val day = parts[parts.size - 2]
            val month = parts[parts.size - 3]
            val year = parts[parts.size - 4]
            val date = "$year-$month-$day"
            val studioId = parts[parts.size - 5]
            val cinemaId = parts.subList(1, parts.size - 5).joinToString("-")
            ParsedScheduleId(movieId, cinemaId, studioId, date, time)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculatePrice(studioType: StudioType, isWeekend: Boolean): Long {
        return when (studioType) {
            StudioType.REGULAR -> if (isWeekend) 55_000L else 45_000L
            StudioType.FOUR_DX -> if (isWeekend) 80_000L else 65_000L
            StudioType.STARIUM -> if (isWeekend) 70_000L else 55_000L
            StudioType.VELVET -> if (isWeekend) 125_000L else 100_000L
        }
    }
}
