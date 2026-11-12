package org.ariqhikari.movietix.presentation.cinema.seatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import org.ariqhikari.movietix.domain.cinema.usecase.CalculateBookingPriceUseCase
import org.ariqhikari.movietix.domain.cinema.usecase.GetSeatLayoutUseCase

class SeatMapViewModel(
    private val scheduleId: String,
    private val movieId: Int,
    private val movieTitle: String,
    private val cinemaId: String,
    private val cinemaName: String,
    private val studioId: String,
    private val studioName: String,
    studioTypeStr: String,
    private val dateStr: String,
    private val timeStr: String,
    private val pricePerSeat: Long,
    private val getSeatLayoutUseCase: GetSeatLayoutUseCase,
    private val calculateBookingPriceUseCase: CalculateBookingPriceUseCase
) : ViewModel() {

    private val studioType: StudioType = try {
        StudioType.valueOf(studioTypeStr)
    } catch (_: Exception) {
        StudioType.REGULAR
    }

    private val _state = MutableStateFlow(
        SeatMapState(
            scheduleId = scheduleId,
            movieId = movieId,
            movieTitle = movieTitle,
            cinemaId = cinemaId,
            cinemaName = cinemaName,
            studioId = studioId,
            studioName = studioName,
            studioType = studioType,
            dateStr = dateStr,
            timeStr = timeStr,
            pricePerSeat = pricePerSeat,
            priceBreakdown = calculateBookingPriceUseCase(0, pricePerSeat),
            isLoading = true
        )
    )
    val state: StateFlow<SeatMapState> = _state.asStateFlow()

    private val _effect = Channel<SeatMapEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadSeatLayout()
    }

    fun onAction(action: SeatMapAction) {
        when (action) {
            is SeatMapAction.ToggleSeat -> handleToggleSeat(action.seatId)
            is SeatMapAction.ProceedToCheckout -> handleProceedToCheckout()
            is SeatMapAction.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(SeatMapEffect.NavigateBack)
                }
            }
        }
    }

    private fun handleToggleSeat(seatId: String) {
        val currentSelected = _state.value.selectedSeats
        if (seatId in currentSelected) {
            val updated = currentSelected - seatId
            val breakdown = calculateBookingPriceUseCase(updated.size, pricePerSeat)
            _state.update {
                it.copy(
                    selectedSeats = updated,
                    priceBreakdown = breakdown
                )
            }
        } else {
            if (currentSelected.size >= 6) {
                viewModelScope.launch {
                    _effect.send(SeatMapEffect.ShowWarning("Maksimal pemesanan adalah 6 kursi"))
                }
                return
            }

            val seat = _state.value.seatLayout?.seats?.find { it.id == seatId }
            if (seat == null || seat.status == SeatStatus.OCCUPIED) {
                // Occupied or unknown seat cannot be selected
                return
            }

            val updated = currentSelected + seatId
            val breakdown = calculateBookingPriceUseCase(updated.size, pricePerSeat)
            _state.update {
                it.copy(
                    selectedSeats = updated,
                    priceBreakdown = breakdown
                )
            }
        }
    }

    private fun handleProceedToCheckout() {
        val currentSelected = _state.value.selectedSeats
        if (currentSelected.isEmpty()) {
            viewModelScope.launch {
                _effect.send(SeatMapEffect.ShowWarning("Silakan pilih minimal 1 kursi"))
            }
            return
        }

        val breakdown = _state.value.priceBreakdown
        viewModelScope.launch {
            _effect.send(
                SeatMapEffect.NavigateToCheckout(
                    scheduleId = scheduleId,
                    movieId = movieId,
                    movieTitle = movieTitle,
                    cinemaName = cinemaName,
                    studioName = studioName,
                    studioType = studioType.name,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    selectedSeats = currentSelected.sorted(),
                    pricePerSeat = pricePerSeat,
                    subtotal = breakdown.subtotal,
                    serviceFee = breakdown.serviceFee,
                    totalPrice = breakdown.total
                )
            )
        }
    }

    private fun loadSeatLayout() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val date = try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                LocalDate(2026, 11, 11)
            }

            getSeatLayoutUseCase(
                scheduleId = scheduleId,
                movieId = movieId,
                cinemaId = cinemaId,
                studioId = studioId,
                date = date,
                time = timeStr
            ).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                seatLayout = result.data,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal memuat denah kursi. Silakan coba lagi."
                            )
                        }
                    }
                }
            }
        }
    }
}
