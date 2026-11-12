package org.ariqhikari.movietix.domain.booking.gateway

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.model.PaymentResult

/**
 * Domain payment gateway contract.
 */
interface PaymentGateway {
    suspend fun processPayment(
        order: BookingOrder,
        paymentMethod: PaymentMethod,
        simulateFailure: Boolean = false
    ): AppResult<PaymentResult>
}
