package org.ariqhikari.movietix.presentation.cinema.seatmap

import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.model.StudioType
import org.ariqhikari.movietix.domain.cinema.usecase.BookingPriceBreakdown

data class SeatMapNavParams(
    val scheduleId: String,
    val movieId: Int,
    val movieTitle: String,
    val cinemaId: String,
    val cinemaName: String,
    val studioId: String,
    val studioName: String,
    val studioTypeStr: String,
    val dateStr: String,
    val timeStr: String,
    val pricePerSeat: Long
)

data class SeatMapState(
    val scheduleId: String,
    val movieId: Int,
    val movieTitle: String,
    val cinemaId: String,
    val cinemaName: String,
    val studioId: String,
    val studioName: String,
    val studioType: StudioType,
    val dateStr: String,
    val timeStr: String,
    val pricePerSeat: Long,
    val seatLayout: SeatLayout? = null,
    val selectedSeats: List<String> = emptyList(),
    val priceBreakdown: BookingPriceBreakdown = BookingPriceBreakdown(0, 0L, 0L, 0L, 0L),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface SeatMapAction {
    data class ToggleSeat(val seatId: String) : SeatMapAction
    data object ProceedToCheckout : SeatMapAction
    data object BackClicked : SeatMapAction
}

sealed interface SeatMapEffect {
    data object NavigateBack : SeatMapEffect
    data class NavigateToCheckout(
        val scheduleId: String,
        val movieId: Int,
        val movieTitle: String,
        val cinemaName: String,
        val studioName: String,
        val studioType: String,
        val dateStr: String,
        val timeStr: String,
        val selectedSeats: List<String>,
        val pricePerSeat: Long,
        val subtotal: Long,
        val serviceFee: Long,
        val totalPrice: Long
    ) : SeatMapEffect
    data class ShowWarning(val message: String) : SeatMapEffect
    data class ShowError(val message: String) : SeatMapEffect
}

object SeatMapContract {
    typealias State = SeatMapState
    typealias Action = SeatMapAction
    typealias Effect = SeatMapEffect
}
