package org.ariqhikari.movietix.presentation.booking.checkout

import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket

data class CheckoutState(
    val order: BookingOrder,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.BCA_VA,
    val isPaymentSheetVisible: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.Idle,
    val confirmedTicket: Ticket? = null,
    val errorMessage: String? = null,
    val isDebugSimulateFailure: Boolean = false,
    val isDebug: Boolean = false
) {
    val isProcessing: Boolean
        get() = paymentStatus is PaymentStatus.Processing
}

sealed interface PaymentStatus {
    data object Idle : PaymentStatus
    data object Processing : PaymentStatus
    data class Success(val ticket: Ticket) : PaymentStatus
    data class Failure(val message: String, val canRetry: Boolean = true) : PaymentStatus
}

sealed interface CheckoutAction {
    data object PayClicked : CheckoutAction
    data object PaymentMethodClicked : CheckoutAction
    data class PaymentMethodSelected(val method: PaymentMethod) : CheckoutAction
    data object DismissPaymentMethodSheet : CheckoutAction
    data object RetryPaymentClicked : CheckoutAction
    data object ChangePaymentMethodClicked : CheckoutAction
    data class DebugSimulateFailureToggled(val enabled: Boolean) : CheckoutAction
    data object BackClicked : CheckoutAction
    data object ViewTicketClicked : CheckoutAction
    data object BackToHomeClicked : CheckoutAction
}

sealed interface CheckoutEffect {
    data object NavigateBack : CheckoutEffect
    data class NavigateToTicketDetail(val bookingCode: String) : CheckoutEffect
    data object NavigateToHome : CheckoutEffect
    data class ShowSnackbar(val message: String) : CheckoutEffect
}

object CheckoutContract {
    typealias State = CheckoutState
    typealias Action = CheckoutAction
    typealias Effect = CheckoutEffect
}
