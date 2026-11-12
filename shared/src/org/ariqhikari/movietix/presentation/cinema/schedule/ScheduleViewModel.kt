package org.ariqhikari.movietix.presentation.cinema.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.usecase.GetCinemasWithSchedulesUseCase

class ScheduleViewModel(
    private val movieId: Int,
    private val movieTitle: String,
    private val posterUrl: String?,
    private val getCinemasWithSchedulesUseCase: GetCinemasWithSchedulesUseCase,
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) : ViewModel() {

    private val today: LocalDate = clock.now().toLocalDateTime(timeZone).date
    private val dateRange: List<LocalDate> = (0..6).map { today.plus(DatePeriod(days = it)) }

    private val _state = MutableStateFlow(
        ScheduleState(
            movieId = movieId,
            movieTitle = movieTitle,
            posterUrl = posterUrl,
            selectedDate = today,
            availableDates = dateRange,
            isLoading = true
        )
    )
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    private val _effect = Channel<ScheduleEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadSchedules(today)
    }

    fun onAction(action: ScheduleAction) {
        when (action) {
            is ScheduleAction.SelectDate -> {
                if (action.date != _state.value.selectedDate) {
                    _state.update { it.copy(selectedDate = action.date) }
                    loadSchedules(action.date)
                }
            }
            is ScheduleAction.SelectShowtime -> {
                viewModelScope.launch {
                    _effect.send(
                        ScheduleEffect.NavigateToSeatMap(
                            scheduleId = action.showtime.id,
                            movieId = movieId,
                            movieTitle = movieTitle,
                            cinemaId = action.cinema.id,
                            cinemaName = action.cinema.name,
                            studioId = action.studio.id,
                            studioName = action.studio.name,
                            studioType = action.studio.type.name,
                            dateStr = _state.value.selectedDate.toString(),
                            timeStr = action.showtime.time,
                            pricePerSeat = action.showtime.price
                        )
                    )
                }
            }
            is ScheduleAction.Retry -> {
                loadSchedules(_state.value.selectedDate)
            }
            is ScheduleAction.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(ScheduleEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadSchedules(date: LocalDate) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getCinemasWithSchedulesUseCase(movieId, date).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                cinemas = result.data,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal memuat jadwal tayang. Periksa koneksi internet Anda."
                            )
                        }
                    }
                }
            }
        }
    }
}
