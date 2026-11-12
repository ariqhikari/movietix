package org.ariqhikari.movietix.presentation.ticket.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketByBookingCodeUseCase
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.Action
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.Effect
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.State

/**
 * ViewModel managing the ticket detail screen state.
 * Loads a ticket by booking code and controls QR enlargement dialog.
 */
class TicketDetailViewModel(
    private val bookingCode: String,
    private val getTicketByBookingCodeUseCase: GetTicketByBookingCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    private var lastClickTime = 0L

    init {
        loadTicket()
    }

    fun onAction(action: Action) {
        when (action) {
            Action.BackClicked -> {
                if (preventDoubleClick()) return
                viewModelScope.launch {
                    _effect.emit(Effect.NavigateBack)
                }
            }
            Action.QrClicked -> {
                _state.update { it.copy(isQrEnlarged = true) }
            }
            Action.DismissQrDialog -> {
                _state.update { it.copy(isQrEnlarged = false) }
            }
            Action.RetryClicked -> {
                loadTicket()
            }
        }
    }

    private fun loadTicket() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getTicketByBookingCodeUseCase(bookingCode)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            ticketItem = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Tiket dengan kode booking $bookingCode tidak ditemukan."
                        )
                    }
                }
            }
        }
    }

    private fun preventDoubleClick(): Boolean {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return if (now - lastClickTime < 500L) {
            true
        } else {
            lastClickTime = now
            false
        }
    }
}
