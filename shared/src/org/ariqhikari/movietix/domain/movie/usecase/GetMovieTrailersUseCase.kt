package org.ariqhikari.movietix.domain.movie.usecase

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class GetMovieTrailersUseCase(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): AppResult<List<TrailerVideo>> {
        return movieRepository.getMovieTrailers(movieId)
    }
}
