package org.ariqhikari.movietix.data.booking.remote

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.gateway.PaymentGateway
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.model.PaymentResult
import kotlin.random.Random

/**
 * Simulated payment gateway for portfolio and demonstration.
 * Injects delay, clock, dispatcher, and random for testability.
 */
class DummyPaymentGateway(
    private val delayMs: Long = 1500L,
    private val clock: Clock = Clock.System,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val random: Random = Random.Default
) : PaymentGateway {

    override suspend fun processPayment(
        order: BookingOrder,
        paymentMethod: PaymentMethod,
        simulateFailure: Boolean
    ): AppResult<PaymentResult> = withContext(dispatcher) {
        if (delayMs > 0) {
            delay(delayMs)
        }

        if (simulateFailure) {
            return@withContext AppResult.Error(AppError.Booking.PaymentFailedSimulated)
        }

        val txId = "TXN-${random.nextLong(1000000000L, 9999999999L)}"
        val result = PaymentResult(
            transactionId = txId,
            paymentMethod = paymentMethod,
            amount = order.totalPrice,
            timestampEpochMs = clock.now().toEpochMilliseconds()
        )
        AppResult.Success(result)
    }
}
