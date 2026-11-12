package org.ariqhikari.movietix.domain.movie.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val genres: List<String>,
    val runtime: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val releaseDate: String,
    val posterUrl: String?,
    val backdropUrl: String?
)
