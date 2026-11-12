package org.ariqhikari.movietix.presentation.booking.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.usecase.ConfirmBookingUseCase

class CheckoutViewModel(
    private val confirmBookingUseCase: ConfirmBookingUseCase,
    initialOrder: BookingOrder,
    isDebug: Boolean = false
) : ViewModel() {

    private val _state = MutableStateFlow(
        CheckoutState(
            order = initialOrder,
            selectedPaymentMethod = PaymentMethod.BCA_VA,
            isDebug = isDebug
        )
    )
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val _effect = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: CheckoutAction) {
        when (action) {
            is CheckoutAction.PayClicked -> initiatePayment()
            is CheckoutAction.PaymentMethodClicked -> {
                if (!_state.value.isProcessing) {
                    _state.update { it.copy(isPaymentSheetVisible = true) }
                }
            }
            is CheckoutAction.PaymentMethodSelected -> {
                _state.update {
                    it.copy(
                        selectedPaymentMethod = action.method,
                        isPaymentSheetVisible = false
                    )
                }
            }
            is CheckoutAction.DismissPaymentMethodSheet -> {
                _state.update { it.copy(isPaymentSheetVisible = false) }
            }
            is CheckoutAction.RetryPaymentClicked -> initiatePayment()
            is CheckoutAction.ChangePaymentMethodClicked -> {
                _state.update {
                    it.copy(
                        paymentStatus = PaymentStatus.Idle,
                        isPaymentSheetVisible = true,
                        errorMessage = null
                    )
                }
            }
            is CheckoutAction.DebugSimulateFailureToggled -> {
                _state.update { it.copy(isDebugSimulateFailure = action.enabled) }
            }
            is CheckoutAction.BackClicked -> {
                if (!_state.value.isProcessing) {
                    viewModelScope.launch {
                        _effect.send(CheckoutEffect.NavigateBack)
                    }
                }
            }
            is CheckoutAction.ViewTicketClicked -> {
                val ticket = _state.value.confirmedTicket
                viewModelScope.launch {
                    if (ticket != null) {
                        _effect.send(CheckoutEffect.NavigateToTicketDetail(ticket.bookingCode))
                    } else {
                        _effect.send(CheckoutEffect.NavigateToHome)
                    }
                }
            }
            is CheckoutAction.BackToHomeClicked -> {
                viewModelScope.launch {
                    _effect.send(CheckoutEffect.NavigateToHome)
                }
            }
        }
    }

    private fun initiatePayment() {
        if (_state.value.isProcessing) return

        _state.update {
            it.copy(
                paymentStatus = PaymentStatus.Processing,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = confirmBookingUseCase(
                order = _state.value.order,
                paymentMethod = _state.value.selectedPaymentMethod,
                simulateFailure = _state.value.isDebugSimulateFailure
            )

            when (result) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            paymentStatus = PaymentStatus.Success(result.data),
                            confirmedTicket = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    val userMessage = when (val error = result.error) {
                        is AppError.Booking.SeatAlreadyOccupied ->
                            "Kursi yang Anda pilih sudah terisi atau dipesan oleh pengguna lain. Silakan pilih kursi lain."
                        is AppError.Booking.PaymentFailedSimulated ->
                            "Pembayaran gagal diproses oleh gateway (simulasi). Silakan coba lagi atau ganti metode pembayaran."
                        is AppError.Booking.SaveTicketFailed ->
                            "Gagal menyimpan tiket: ${error.cause}. Transaksi dibatalkan dan kursi Anda telah dilepaskan."
                        is AppError.Booking.RollbackFailed ->
                            "Terjadi kesalahan saat memproses pesanan dan pelepasan kursi gagal. Mohon hubungi bantuan."
                        is AppError.Booking.InvalidSelection ->
                            "Data pesanan tidak valid. Silakan ulangi pemesanan."
                        is AppError.Storage.WriteFailed ->
                            "Penyimpanan tiket gagal: ${error.cause}. Kursi telah dilepaskan kembali."
                        else ->
                            "Terjadi kendala saat memproses pembayaran. Silakan coba kembali."
                    }
                    val canRetry = result.error !is AppError.Booking.SeatAlreadyOccupied

                    _state.update {
                        it.copy(
                            paymentStatus = PaymentStatus.Failure(
                                message = userMessage,
                                canRetry = canRetry
                            ),
                            errorMessage = userMessage
                        )
                    }
                }
            }
        }
    }
}
