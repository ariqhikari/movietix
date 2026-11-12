package org.ariqhikari.movietix.data.movie.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.ariqhikari.movietix.data.movie.remote.dto.CreditsDto
import org.ariqhikari.movietix.data.movie.remote.dto.GenreListResponseDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieDetailDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieResponseDto
import org.ariqhikari.movietix.data.movie.remote.dto.VideosDto

class TmdbApiService(private val client: HttpClient) {

    suspend fun getNowPlayingMovies(
        page: Int = 1,
        language: String = "id-ID",
        region: String = "ID"
    ): MovieResponseDto {
        return client.get("movie/now_playing") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()
    }

    suspend fun getPopularMovies(
        page: Int = 1,
        language: String = "id-ID",
        region: String = "ID"
    ): MovieResponseDto {
        return client.get("movie/popular") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()
    }

    suspend fun getUpcomingMovies(
        page: Int = 1,
        language: String = "id-ID",
        region: String = "ID"
    ): MovieResponseDto {
        return client.get("movie/upcoming") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()
    }

    suspend fun getGenres(language: String = "id-ID"): GenreListResponseDto {
        return client.get("genre/movie/list") {
            parameter("language", language)
        }.body()
    }

    suspend fun getMovieDetail(
        movieId: Int,
        language: String = "id-ID"
    ): MovieDetailDto {
        return client.get("movie/$movieId") {
            parameter("language", language)
        }.body()
    }

    suspend fun getMovieCredits(
        movieId: Int,
        language: String = "id-ID"
    ): CreditsDto {
        return client.get("movie/$movieId/credits") {
            parameter("language", language)
        }.body()
    }

    suspend fun getMovieVideos(
        movieId: Int,
        language: String = "id-ID"
    ): VideosDto {
        return client.get("movie/$movieId/videos") {
            parameter("language", language)
        }.body()
    }
}
