package org.ariqhikari.movietix.fake

import kotlinx.coroutines.delay
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.gateway.PaymentGateway
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.model.PaymentResult

class FakePaymentGateway : PaymentGateway {
    var shouldFailPayment: Boolean = false
    var processCallCount: Int = 0
    var delayMillis: Long = 0L

    override suspend fun processPayment(
        order: BookingOrder,
        paymentMethod: PaymentMethod,
        simulateFailure: Boolean
    ): AppResult<PaymentResult> {
        processCallCount++
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailPayment || simulateFailure) {
            return AppResult.Error(AppError.Booking.PaymentFailedSimulated)
        }
        return AppResult.Success(
            PaymentResult(
                transactionId = "TX-FAKE-123456",
                paymentMethod = paymentMethod,
                amount = order.totalPrice,
                timestampEpochMs = 1731330000000L
            )
        )
    }
}
