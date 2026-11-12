package org.ariqhikari.movietix.domain.booking.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.booking.gateway.PaymentGateway
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.booking.util.BookingCodeGenerator
import org.ariqhikari.movietix.domain.booking.util.QrPayloadFactory
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository

/**
 * Orchestrates atomic ticket confirmation across cinema seats, payment gateway, and ticket storage.
 * Follows 4-step sequence:
 * 1. reserveSeats (atomic, fails without touching payment/tickets)
 * 2. processPayment (fails -> rolls back reserved seats)
 * 3. saveTicket (fails -> rolls back reserved seats)
 * 4. returns confirmed Ticket
 *
 * All compensation/rollback logic is guaranteed via withContext(NonCancellable).
 */
class ConfirmBookingUseCase(
    private val cinemaScheduleRepository: CinemaScheduleRepository,
    private val paymentGateway: PaymentGateway,
    private val ticketRepository: TicketRepository,
    private val bookingCodeGenerator: BookingCodeGenerator = BookingCodeGenerator(),
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke(
        order: BookingOrder,
        paymentMethod: PaymentMethod,
        simulateFailure: Boolean = false
    ): AppResult<Ticket> {
        // Step 1: Atomically reserve seats in cinema repository
        val reserveResult = cinemaScheduleRepository.reserveSeats(order.scheduleId, order.selectedSeats)
        if (reserveResult is AppResult.Error) {
            return reserveResult
        }

        try {
            // Step 2: Simulated payment processing
            val paymentResult = paymentGateway.processPayment(order, paymentMethod, simulateFailure)
            if (paymentResult is AppResult.Error) {
                val rollbackError = executeRollback(order.scheduleId, order.selectedSeats, paymentResult.error)
                return if (rollbackError != null) AppResult.Error(rollbackError) else paymentResult
            }

            // Step 3: Issue and persist confirmed ticket
            val bookingCode = bookingCodeGenerator.generate()
            val qrPayload = QrPayloadFactory.create(bookingCode, order.scheduleId, order.selectedSeats)
            val showtimeIso = "${order.date}T${order.showtime}:00"
            val ticket = Ticket(
                bookingCode = bookingCode,
                movieId = order.movieId,
                movieTitle = order.movieTitle,
                posterUrl = order.posterUrl,
                backdropUrl = null,
                cinemaName = order.cinemaName,
                studioName = order.studioName,
                studioType = order.studioType,
                showtimeIso = showtimeIso,
                seats = order.selectedSeats.sorted(),
                pricePerSeat = order.pricePerSeat,
                serviceFee = order.serviceFee,
                totalPrice = order.totalPrice,
                paymentMethod = paymentMethod,
                transactionTimeEpochMs = clock.now().toEpochMilliseconds(),
                status = TicketStatus.ACTIVE,
                qrPayload = qrPayload
            )

            val saveTicketResult = withContext(NonCancellable) {
                ticketRepository.saveTicket(ticket)
            }
            if (saveTicketResult is AppResult.Error) {
                val cause = when (val err = saveTicketResult.error) {
                    is AppError.Storage.WriteFailed -> err.cause
                    is AppError.Storage.ReadFailed -> err.cause
                    else -> "Gagal menyimpan tiket ke penyimpanan lokal"
                }
                val saveError = AppError.Booking.SaveTicketFailed(cause)
                val rollbackError = executeRollback(order.scheduleId, order.selectedSeats, saveError)
                return if (rollbackError != null) AppResult.Error(rollbackError) else AppResult.Error(saveError)
            }

            // Step 4: Success
            return AppResult.Success(ticket)
        } catch (cancellation: CancellationException) {
            // Job was cancelled after seats were reserved: release seats non-cancellably before propagating
            withContext(NonCancellable) {
                cinemaScheduleRepository.releaseSeats(order.scheduleId, order.selectedSeats)
            }
            throw cancellation
        }
    }

    private suspend fun executeRollback(
        scheduleId: String,
        seats: List<String>,
        originalError: AppError
    ): AppError? = withContext(NonCancellable) {
        val releaseResult = cinemaScheduleRepository.releaseSeats(scheduleId, seats)
        if (releaseResult is AppResult.Error) {
            AppError.Booking.RollbackFailed("Gagal melepaskan kursi saat rollback", originalError)
        } else null
    }
}
