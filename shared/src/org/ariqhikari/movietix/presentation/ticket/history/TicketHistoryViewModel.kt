package org.ariqhikari.movietix.presentation.ticket.history

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
import org.ariqhikari.movietix.domain.ticket.usecase.GetTicketsUseCase
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.Action
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.Effect
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.State

/**
 * ViewModel managing the ticket history screen state.
 * Observes [GetTicketsUseCase] flow which partitions tickets into upcoming and completed groups.
 */
class TicketHistoryViewModel(
    private val getTicketsUseCase: GetTicketsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    private var lastClickTime = 0L

    init {
        loadTickets()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.TicketClicked -> {
                if (preventDoubleClick()) return
                viewModelScope.launch {
                    _effect.emit(Effect.NavigateToDetail(action.bookingCode))
                }
            }
            Action.ExploreMoviesClicked -> {
                if (preventDoubleClick()) return
                viewModelScope.launch {
                    _effect.emit(Effect.NavigateToHome)
                }
            }
            Action.RetryClicked -> {
                loadTickets()
            }
        }
    }

    private fun loadTickets() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getTicketsUseCase().collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                upcomingTickets = result.data.upcoming,
                                completedTickets = result.data.completed,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal memuat riwayat tiket. Silakan coba lagi."
                            )
                        }
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
