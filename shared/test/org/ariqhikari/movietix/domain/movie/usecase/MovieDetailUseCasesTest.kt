package org.ariqhikari.movietix.domain.movie.usecase

import kotlinx.coroutines.test.runTest
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.fake.FakeMovieRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovieDetailUseCasesTest {

    private val repository = FakeMovieRepository()

    private val getMovieDetailUseCase = GetMovieDetailUseCase(repository)
    private val getMovieCreditsUseCase = GetMovieCreditsUseCase(repository)
    private val getMovieTrailersUseCase = GetMovieTrailersUseCase(repository)

    @Test
    fun getMovieDetail_invokesRepositoryWithCorrectIdAndReturnsSuccess() = runTest {
        val dummyDetail = FakeMovieRepository.createDummyMovieDetail(movieId = 42)
        repository.movieDetailResult = AppResult.Success(dummyDetail)

        val result = getMovieDetailUseCase(movieId = 42)

        assertEquals(42, repository.lastRequestedMovieDetailId)
        assertTrue(result is AppResult.Success)
        assertEquals(dummyDetail, result.data)
    }

    @Test
    fun getMovieDetail_propagatesErrorFromRepository() = runTest {
        val networkError = AppError.Network.NoInternet
        repository.movieDetailResult = AppResult.Error(networkError)

        val result = getMovieDetailUseCase(movieId = 99)

        assertTrue(result is AppResult.Error)
        assertEquals(networkError, result.error)
    }

    @Test
    fun getMovieCredits_invokesRepositoryAndReturnsSuccess() = runTest {
        val castList = listOf(FakeMovieRepository.createDummyCastMember(1, "Actor One"))
        repository.movieCreditsResult = AppResult.Success(castList)

        val result = getMovieCreditsUseCase(movieId = 42)

        assertEquals(42, repository.lastRequestedCreditsMovieId)
        assertTrue(result is AppResult.Success)
        assertEquals(castList, result.data)
    }

    @Test
    fun getMovieCredits_propagatesErrorFromRepository() = runTest {
        val serverError = AppError.Network.ServerError(500, "Internal Server Error")
        repository.movieCreditsResult = AppResult.Error(serverError)

        val result = getMovieCreditsUseCase(movieId = 42)

        assertTrue(result is AppResult.Error)
        assertEquals(serverError, result.error)
    }

    @Test
    fun getMovieTrailers_invokesRepositoryAndReturnsSuccess() = runTest {
        val trailerList = listOf(FakeMovieRepository.createDummyTrailerVideo("t1", "trailerKey"))
        repository.movieTrailersResult = AppResult.Success(trailerList)

        val result = getMovieTrailersUseCase(movieId = 42)

        assertEquals(42, repository.lastRequestedTrailersMovieId)
        assertTrue(result is AppResult.Success)
        assertEquals(trailerList, result.data)
    }

    @Test
    fun getMovieTrailers_propagatesErrorFromRepository() = runTest {
        val timeoutError = AppError.Network.Timeout
        repository.movieTrailersResult = AppResult.Error(timeoutError)

        val result = getMovieTrailersUseCase(movieId = 42)

        assertTrue(result is AppResult.Error)
        assertEquals(timeoutError, result.error)
    }
}
