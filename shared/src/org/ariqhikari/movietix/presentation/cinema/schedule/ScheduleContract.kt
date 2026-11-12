package org.ariqhikari.movietix.presentation.cinema.schedule

import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.Showtime
import org.ariqhikari.movietix.domain.cinema.model.Studio

data class ScheduleState(
    val movieId: Int = 0,
    val movieTitle: String = "",
    val posterUrl: String? = null,
    val selectedDate: LocalDate,
    val availableDates: List<LocalDate> = emptyList(),
    val cinemas: List<Cinema> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface ScheduleAction {
    data class SelectDate(val date: LocalDate) : ScheduleAction
    data class SelectShowtime(
        val cinema: Cinema,
        val studio: Studio,
        val showtime: Showtime
    ) : ScheduleAction
    data object Retry : ScheduleAction
    data object BackClicked : ScheduleAction
}

sealed interface ScheduleEffect {
    data object NavigateBack : ScheduleEffect
    data class NavigateToSeatMap(
        val scheduleId: String,
        val movieId: Int,
        val movieTitle: String,
        val cinemaId: String,
        val cinemaName: String,
        val studioId: String,
        val studioName: String,
        val studioType: String,
        val dateStr: String,
        val timeStr: String,
        val pricePerSeat: Long
    ) : ScheduleEffect
    data class ShowSnackbar(val message: String) : ScheduleEffect
}

object ScheduleContract {
    typealias State = ScheduleState
    typealias Action = ScheduleAction
    typealias Effect = ScheduleEffect
}
