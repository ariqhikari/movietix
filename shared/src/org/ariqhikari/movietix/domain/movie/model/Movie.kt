package org.ariqhikari.movietix.domain.movie.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<String> = emptyList()
)
