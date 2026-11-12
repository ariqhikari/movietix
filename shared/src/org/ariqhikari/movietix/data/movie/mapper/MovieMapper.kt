package org.ariqhikari.movietix.data.movie.mapper

import org.ariqhikari.movietix.data.movie.remote.dto.CastDto
import org.ariqhikari.movietix.data.movie.remote.dto.CreditsDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieDetailDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieDto
import org.ariqhikari.movietix.data.movie.remote.dto.VideoDto
import org.ariqhikari.movietix.data.movie.remote.dto.VideosDto
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo

object MovieMapper {
    private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
    private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w1280"
    private const val PROFILE_BASE_URL = "https://image.tmdb.org/t/p/w185"

    private fun formatUrl(baseUrl: String, path: String): String {
        val formatted = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$formatted"
    }

    fun toDomain(dto: MovieDto, genreMap: Map<Int, String> = emptyMap()): Movie {
        val posterUrl = dto.posterPath?.takeIf { it.isNotBlank() }?.let { formatUrl(POSTER_BASE_URL, it) }
        val backdropUrl = dto.backdropPath?.takeIf { it.isNotBlank() }?.let { formatUrl(BACKDROP_BASE_URL, it) }
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

    fun toDetailDomain(dto: MovieDetailDto): MovieDetail {
        val posterUrl = dto.posterPath?.takeIf { it.isNotBlank() }?.let { formatUrl(POSTER_BASE_URL, it) }
        val backdropUrl = dto.backdropPath?.takeIf { it.isNotBlank() }?.let { formatUrl(BACKDROP_BASE_URL, it) }
        val genreNames = dto.genres?.map { it.name.trim() }?.filter { it.isNotBlank() } ?: emptyList()

        return MovieDetail(
            id = dto.id,
            title = dto.title?.trim()?.ifBlank { "Tanpa Judul" } ?: "Tanpa Judul",
            tagline = dto.tagline?.trim() ?: "",
            overview = dto.overview?.trim() ?: "",
            genres = genreNames,
            runtime = dto.runtime ?: 0,
            voteAverage = dto.voteAverage ?: 0.0,
            voteCount = dto.voteCount ?: 0,
            releaseDate = dto.releaseDate?.trim() ?: "",
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )
    }

    fun toCastDomain(dto: CastDto): CastMember {
        val profileUrl = dto.profilePath?.takeIf { it.isNotBlank() }?.let { formatUrl(PROFILE_BASE_URL, it) }
        return CastMember(
            id = dto.id,
            name = dto.name.trim().ifBlank { "Pemeran" },
            character = dto.character?.trim() ?: "",
            profileUrl = profileUrl,
            order = dto.order ?: 0
        )
    }

    fun toCastListDomain(dto: CreditsDto): List<CastMember> {
        return dto.cast
            ?.sortedBy { it.order ?: 0 }
            ?.map { toCastDomain(it) }
            ?: emptyList()
    }

    fun toTrailerDomain(dto: VideoDto): TrailerVideo {
        val site = dto.site?.trim() ?: ""
        val type = dto.type?.trim() ?: ""
        val name = dto.name?.trim()?.ifBlank { "Trailer Resmi" } ?: "Trailer Resmi"
        val key = dto.key.trim()
        val watchUrl = "https://www.youtube.com/watch?v=$key"

        return TrailerVideo(
            id = dto.id,
            key = key,
            name = name,
            site = site,
            type = type,
            youtubeWatchUrl = watchUrl
        )
    }

    fun toTrailerListDomain(dto: VideosDto): List<TrailerVideo> {
        return dto.results
            ?.filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
            ?.sortedBy { video ->
                when (video.type?.lowercase()) {
                    "trailer" -> 0
                    "teaser" -> 1
                    else -> 2
                }
            }
            ?.map { toTrailerDomain(it) }
            ?: emptyList()
    }
}

fun MovieDto.toDomain(genreMap: Map<Int, String> = emptyMap()): Movie =
    MovieMapper.toDomain(this, genreMap)

fun MovieDetailDto.toDomain(): MovieDetail =
    MovieMapper.toDetailDomain(this)

fun CreditsDto.toCastMembers(): List<CastMember> =
    MovieMapper.toCastListDomain(this)

fun VideosDto.toTrailers(): List<TrailerVideo> =
    MovieMapper.toTrailerListDomain(this)
