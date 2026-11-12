package org.ariqhikari.movietix.domain.movie.usecase

import kotlinx.coroutines.test.runTest
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.fake.FakeMovieRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovieCatalogUseCasesTest {

    private val repository = FakeMovieRepository()

    private val getNowPlayingUseCase = GetNowPlayingMoviesUseCase(repository)
    private val getPopularUseCase = GetPopularMoviesUseCase(repository)
    private val getUpcomingUseCase = GetUpcomingMoviesUseCase(repository)

    @Test
    fun getNowPlaying_invokesRepositoryWithCorrectPageAndReturnsSuccess() = runTest {
        val dummyMovies = listOf(FakeMovieRepository.createDummyMovie(1, "Now Playing Movie"))
        repository.nowPlayingResult = AppResult.Success(dummyMovies)

        val result = getNowPlayingUseCase(page = 2)

        assertEquals(2, repository.lastRequestedNowPlayingPage)
        assertTrue(result is AppResult.Success)
        assertEquals(dummyMovies, result.data)
    }

    @Test
    fun getPopular_invokesRepositoryWithCorrectPageAndReturnsSuccess() = runTest {
        val dummyMovies = listOf(FakeMovieRepository.createDummyMovie(2, "Popular Movie"))
        repository.popularResult = AppResult.Success(dummyMovies)

        val result = getPopularUseCase(page = 1)

        assertEquals(1, repository.lastRequestedPopularPage)
        assertTrue(result is AppResult.Success)
        assertEquals(dummyMovies, result.data)
    }

    @Test
    fun getUpcoming_invokesRepositoryWithCorrectPageAndReturnsSuccess() = runTest {
        val dummyMovies = listOf(FakeMovieRepository.createDummyMovie(3, "Upcoming Movie"))
        repository.upcomingResult = AppResult.Success(dummyMovies)

        val result = getUpcomingUseCase(page = 3)

        assertEquals(3, repository.lastRequestedUpcomingPage)
        assertTrue(result is AppResult.Success)
        assertEquals(dummyMovies, result.data)
    }

    @Test
    fun useCases_propagateNetworkErrorsFromRepository() = runTest {
        val networkError = AppError.Network.NoInternet
        repository.nowPlayingResult = AppResult.Error(networkError)

        val result = getNowPlayingUseCase(page = 1)

        assertTrue(result is AppResult.Error)
        assertEquals(networkError, result.error)
    }
}
