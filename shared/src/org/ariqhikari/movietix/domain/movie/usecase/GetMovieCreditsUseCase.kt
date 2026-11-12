package org.ariqhikari.movietix.domain.movie.usecase

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class GetMovieCreditsUseCase(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): AppResult<List<CastMember>> {
        return movieRepository.getMovieCredits(movieId)
    }
}
