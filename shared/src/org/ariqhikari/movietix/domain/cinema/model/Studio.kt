package org.ariqhikari.movietix.domain.cinema.model

data class Studio(
    val id: String,
    val name: String,
    val type: StudioType,
    val showtimes: List<Showtime>
)
