package org.ariqhikari.movietix.fake

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class FakeMovieRepository : MovieRepository {

    var nowPlayingResult: AppResult<List<Movie>> = AppResult.Success(emptyList())
    var popularResult: AppResult<List<Movie>> = AppResult.Success(emptyList())
    var upcomingResult: AppResult<List<Movie>> = AppResult.Success(emptyList())

    var lastRequestedNowPlayingPage: Int? = null
    var lastRequestedPopularPage: Int? = null
    var lastRequestedUpcomingPage: Int? = null

    override suspend fun getNowPlayingMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedNowPlayingPage = page
        return nowPlayingResult
    }

    override suspend fun getPopularMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedPopularPage = page
        return popularResult
    }

    override suspend fun getUpcomingMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedUpcomingPage = page
        return upcomingResult
    }

    companion object {
        fun createDummyMovie(
            id: Int = 1,
            title: String = "Test Movie $id",
            voteAverage: Double = 8.5,
            genres: List<String> = listOf("Action", "Drama")
        ): Movie {
            return Movie(
                id = id,
                title = title,
                overview = "Overview of $title",
                posterUrl = "https://image.tmdb.org/t/p/w500/poster_$id.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop_$id.jpg",
                releaseDate = "2026-05-15",
                voteAverage = voteAverage,
                voteCount = 100,
                genres = genres
            )
        }
    }
}
