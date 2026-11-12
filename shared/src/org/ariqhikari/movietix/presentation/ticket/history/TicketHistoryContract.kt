package org.ariqhikari.movietix.presentation.ticket.history

import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem

object TicketHistoryContract {

    data class State(
        val isLoading: Boolean = true,
        val upcomingTickets: List<TicketDisplayItem> = emptyList(),
        val completedTickets: List<TicketDisplayItem> = emptyList(),
        val errorMessage: String? = null
    ) {
        val isEmpty: Boolean get() = !isLoading && errorMessage == null && upcomingTickets.isEmpty() && completedTickets.isEmpty()
    }

    sealed interface Action {
        data class TicketClicked(val bookingCode: String) : Action
        data object ExploreMoviesClicked : Action
        data object RetryClicked : Action
    }

    sealed interface Effect {
        data class NavigateToDetail(val bookingCode: String) : Effect
        data object NavigateToHome : Effect
    }
}
