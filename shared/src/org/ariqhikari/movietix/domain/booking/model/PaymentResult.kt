package org.ariqhikari.movietix.domain.booking.model

/**
 * Result data class returned upon successful payment processing.
 */
data class PaymentResult(
    val transactionId: String,
    val paymentMethod: PaymentMethod,
    val amount: Long,
    val timestampEpochMs: Long
)
