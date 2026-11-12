package org.ariqhikari.movietix.presentation.movie.detail

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieCreditsUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieDetailUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieTrailersUseCase
import org.ariqhikari.movietix.fake.FakeMovieRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeMovieRepository()

    private val getMovieDetailUseCase = GetMovieDetailUseCase(repository)
    private val getMovieCreditsUseCase = GetMovieCreditsUseCase(repository)
    private val getMovieTrailersUseCase = GetMovieTrailersUseCase(repository)

    private fun createViewModel(movieId: Int = 42) = DetailViewModel(
        movieId = movieId,
        getMovieDetailUseCase = getMovieDetailUseCase,
        getMovieCreditsUseCase = getMovieCreditsUseCase,
        getMovieTrailersUseCase = getMovieTrailersUseCase
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_allRequestsSuccessful() = runTest(testDispatcher) {
        val detail = FakeMovieRepository.createDummyMovieDetail(movieId = 42)
        val cast = listOf(FakeMovieRepository.createDummyCastMember(1, "Actor"))
        val trailers = listOf(FakeMovieRepository.createDummyTrailerVideo("t1", "trailerKey"))

        repository.movieDetailResult = AppResult.Success(detail)
        repository.movieCreditsResult = AppResult.Success(cast)
        repository.movieTrailersResult = AppResult.Success(trailers)

        val viewModel = createViewModel(42)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingDetail)
        assertFalse(state.isLoadingCredits)
        assertFalse(state.isLoadingTrailers)
        assertNull(state.detailError)
        assertEquals(detail, state.movieDetail)
        assertEquals(cast, state.castMembers)
        assertEquals(trailers, state.trailers)
    }

    @Test
    fun initialLoad_detailFails_showsErrorState() = runTest(testDispatcher) {
        repository.movieDetailResult = AppResult.Error(AppError.Network.NoInternet)

        val viewModel = createViewModel(42)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingDetail)
        assertNull(state.movieDetail)
        val errorMsg = state.detailError
        assertNotNull(errorMsg)
        assertTrue(errorMsg.contains("internet", ignoreCase = true))
    }

    @Test
    fun initialLoad_castFails_detailStillSucceeds() = runTest(testDispatcher) {
        val detail = FakeMovieRepository.createDummyMovieDetail(movieId = 42)
        repository.movieDetailResult = AppResult.Success(detail)
        repository.movieCreditsResult = AppResult.Error(AppError.Network.ServerError(500, "Error"))

        val viewModel = createViewModel(42)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingDetail)
        assertFalse(state.isLoadingCredits)
        assertEquals(detail, state.movieDetail)
        assertTrue(state.castMembers.isEmpty())
        assertNull(state.detailError)
    }

    @Test
    fun initialLoad_trailersEmpty_handledGracefully() = runTest(testDispatcher) {
        val detail = FakeMovieRepository.createDummyMovieDetail(movieId = 42)
        repository.movieDetailResult = AppResult.Success(detail)
        repository.movieTrailersResult = AppResult.Success(emptyList())

        val viewModel = createViewModel(42)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingTrailers)
        assertTrue(state.trailers.isEmpty())
        assertNull(state.detailError)
    }

    @Test
    fun retryAll_reloadsAllData() = runTest(testDispatcher) {
        repository.movieDetailResult = AppResult.Error(AppError.Network.NoInternet)

        val viewModel = createViewModel(42)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.detailError)

        val detail = FakeMovieRepository.createDummyMovieDetail(movieId = 42)
        repository.movieDetailResult = AppResult.Success(detail)
        viewModel.onAction(DetailAction.RetryAll)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingDetail)
        assertNull(state.detailError)
        assertEquals(detail, state.movieDetail)
    }

    @Test
    fun toggleSynopsis_togglesExpansionState() = runTest(testDispatcher) {
        val viewModel = createViewModel(42)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSynopsisExpanded)

        viewModel.onAction(DetailAction.ToggleSynopsis)
        assertTrue(viewModel.state.value.isSynopsisExpanded)

        viewModel.onAction(DetailAction.ToggleSynopsis)
        assertFalse(viewModel.state.value.isSynopsisExpanded)
    }

    @Test
    fun playTrailer_emitsOpenUrlEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel(42)
        val trailer = FakeMovieRepository.createDummyTrailerVideo("t1", "trailerKey")

        viewModel.effect.test {
            viewModel.onAction(DetailAction.PlayTrailer(trailer))
            val effect = awaitItem()
            assertTrue(effect is DetailEffect.OpenUrl)
            assertEquals(trailer.youtubeWatchUrl, effect.url)
        }
    }

    @Test
    fun selectScheduleClicked_emitsNavigateToScheduleEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel(42)

        viewModel.effect.test {
            viewModel.onAction(DetailAction.SelectScheduleClicked)
            val effect = awaitItem()
            assertTrue(effect is DetailEffect.NavigateToSchedule)
            assertEquals(42, effect.movieId)
        }
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel(42)

        viewModel.effect.test {
            viewModel.onAction(DetailAction.BackClicked)
            val effect = awaitItem()
            assertTrue(effect is DetailEffect.NavigateBack)
        }
    }
}
