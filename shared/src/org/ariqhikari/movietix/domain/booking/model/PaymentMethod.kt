package org.ariqhikari.movietix.domain.booking.model

import kotlinx.serialization.Serializable

/**
 * Supported dummy payment methods for MovieTix.
 * Stored stably in TicketEntity.payment_method.
 */
@Serializable
enum class PaymentMethod(
    val code: String,
    val displayName: String,
    val description: String
) {
    BCA_VA(
        code = "BCA_VA",
        displayName = "BCA Virtual Account",
        description = "Transfer via ATM atau m-BCA"
    ),
    GOPAY(
        code = "GOPAY",
        displayName = "GoPay",
        description = "Pembayaran instan via saldo GoPay"
    ),
    OVO(
        code = "OVO",
        displayName = "OVO",
        description = "Pembayaran instan via saldo OVO"
    ),
    CREDIT_CARD(
        code = "CREDIT_CARD",
        displayName = "Kartu Debit/Kredit",
        description = "Visa, Mastercard, JCB"
    );

    companion object {
        fun fromCode(code: String): PaymentMethod {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BCA_VA
        }
    }
}
