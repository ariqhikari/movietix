package org.ariqhikari.movietix.domain.movie.repository

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo

interface MovieRepository {
    suspend fun getNowPlayingMovies(page: Int = 1): AppResult<List<Movie>>
    suspend fun getPopularMovies(page: Int = 1): AppResult<List<Movie>>
    suspend fun getUpcomingMovies(page: Int = 1): AppResult<List<Movie>>
    suspend fun getMovieDetail(movieId: Int): AppResult<MovieDetail>
    suspend fun getMovieCredits(movieId: Int): AppResult<List<CastMember>>
    suspend fun getMovieTrailers(movieId: Int): AppResult<List<TrailerVideo>>
}
