package org.ariqhikari.movietix.presentation.ticket.detail

import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem

object TicketDetailContract {

    data class State(
        val isLoading: Boolean = true,
        val ticketItem: TicketDisplayItem? = null,
        val isQrEnlarged: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface Action {
        data object BackClicked : Action
        data object QrClicked : Action
        data object DismissQrDialog : Action
        data object RetryClicked : Action
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
