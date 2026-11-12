package org.ariqhikari.movietix.domain.cinema.model

data class Showtime(
    val id: String,
    val time: String,
    val price: Long,
    val occupiedSeatCount: Int,
    val totalSeatCount: Int = 80,
    val isAlmostFull: Boolean = occupiedSeatCount.toFloat() / totalSeatCount >= 0.70f
)
