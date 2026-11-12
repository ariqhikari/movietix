package org.ariqhikari.movietix.domain.cinema.model

enum class SeatStatus {
    AVAILABLE,
    SELECTED,
    OCCUPIED
}

data class Seat(
    val id: String,
    val row: String,
    val column: Int,
    val status: SeatStatus = SeatStatus.AVAILABLE
)

data class SeatLayout(
    val scheduleId: String,
    val rows: List<String>,
    val totalColumns: Int,
    val aisleAfterColumn: Int,
    val seats: List<Seat>
)
