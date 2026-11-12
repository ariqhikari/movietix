package org.ariqhikari.movietix.domain.cinema.usecase

/**
 * Breakdown of ticket booking calculations.
 */
data class BookingPriceBreakdown(
    val seatCount: Int,
    val pricePerSeat: Long,
    val subtotal: Long,
    val serviceFee: Long,
    val total: Long
)

/**
 * Use case to compute subtotal, application service fee, and total amount.
 * Fixed service fee per ticket = Rp 3.000.
 */
class CalculateBookingPriceUseCase {

    companion object {
        const val SERVICE_FEE_PER_SEAT = 3_000L
    }

    operator fun invoke(seatCount: Int, pricePerSeat: Long): BookingPriceBreakdown {
        if (seatCount <= 0) {
            return BookingPriceBreakdown(
                seatCount = 0,
                pricePerSeat = pricePerSeat,
                subtotal = 0L,
                serviceFee = 0L,
                total = 0L
            )
        }
        val subtotal = seatCount * pricePerSeat
        val serviceFee = seatCount * SERVICE_FEE_PER_SEAT
        val total = subtotal + serviceFee
        return BookingPriceBreakdown(
            seatCount = seatCount,
            pricePerSeat = pricePerSeat,
            subtotal = subtotal,
            serviceFee = serviceFee,
            total = total
        )
    }
}
