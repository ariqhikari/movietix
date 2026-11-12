package org.ariqhikari.movietix.domain.movie.usecase

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class GetMovieDetailUseCase(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): AppResult<MovieDetail> {
        return movieRepository.getMovieDetail(movieId)
    }
}
