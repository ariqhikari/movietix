package org.ariqhikari.movietix.domain.movie.repository

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.Movie

interface MovieRepository {
    suspend fun getNowPlayingMovies(page: Int = 1): AppResult<List<Movie>>
    suspend fun getPopularMovies(page: Int = 1): AppResult<List<Movie>>
    suspend fun getUpcomingMovies(page: Int = 1): AppResult<List<Movie>>
}
