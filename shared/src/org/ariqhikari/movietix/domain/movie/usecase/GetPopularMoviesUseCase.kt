package org.ariqhikari.movietix.domain.movie.usecase

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class GetPopularMoviesUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(page: Int = 1): AppResult<List<Movie>> {
        return repository.getPopularMovies(page)
    }
}
