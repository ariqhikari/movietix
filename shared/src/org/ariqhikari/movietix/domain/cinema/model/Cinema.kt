package org.ariqhikari.movietix.domain.cinema.model

data class Cinema(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val studios: List<Studio>
)
