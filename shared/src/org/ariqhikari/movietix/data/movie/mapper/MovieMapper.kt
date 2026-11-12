package org.ariqhikari.movietix.data.movie.mapper

import org.ariqhikari.movietix.data.movie.remote.dto.MovieDto
import org.ariqhikari.movietix.domain.movie.model.Movie

object MovieMapper {
    private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
    private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w1280"

    fun toDomain(dto: MovieDto, genreMap: Map<Int, String> = emptyMap()): Movie {
        val posterUrl = dto.posterPath?.takeIf { it.isNotBlank() }?.let { path ->
            val formatted = if (path.startsWith("/")) path else "/$path"
            "$POSTER_BASE_URL$formatted"
        }

        val backdropUrl = dto.backdropPath?.takeIf { it.isNotBlank() }?.let { path ->
            val formatted = if (path.startsWith("/")) path else "/$path"
            "$BACKDROP_BASE_URL$formatted"
        }

        val genreNames = dto.genreIds?.mapNotNull { genreMap[it] } ?: emptyList()

        return Movie(
            id = dto.id,
            title = dto.title?.trim()?.ifBlank { "Tanpa Judul" } ?: "Tanpa Judul",
            overview = dto.overview?.trim() ?: "",
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            releaseDate = dto.releaseDate?.trim() ?: "",
            voteAverage = dto.voteAverage ?: 0.0,
            voteCount = dto.voteCount ?: 0,
            genres = genreNames
        )
    }

    fun toDomainList(dtos: List<MovieDto>, genreMap: Map<Int, String> = emptyMap()): List<Movie> {
        return dtos.map { toDomain(it, genreMap) }
    }
}

fun MovieDto.toDomain(genreMap: Map<Int, String> = emptyMap()): Movie =
    MovieMapper.toDomain(this, genreMap)
