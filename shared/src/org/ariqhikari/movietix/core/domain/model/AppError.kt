package org.ariqhikari.movietix.core.domain.model

/**
 * Standard error hierarchy for MovieTix.
 * Follows docs/ARCHITECTURE.md section 4.
 */
sealed interface AppError {
    sealed interface Network : AppError {
        data object NoInternet : Network
        data object Timeout : Network
        data class ServerError(val code: Int, val message: String?) : Network
        data object Unauthorized : Network
    }

    sealed interface Storage : AppError {
        data class ReadFailed(val cause: String) : Storage
        data class WriteFailed(val cause: String) : Storage
    }

    sealed interface Booking : AppError {
        data object SeatAlreadyOccupied : Booking
        data object PaymentFailedSimulated : Booking
        data class SaveTicketFailed(val cause: String) : Booking
        data object InvalidSelection : Booking
        data class RollbackFailed(val cause: String, val originalError: AppError) : Booking
    }

    data class Unknown(val throwable: Throwable) : AppError
}
