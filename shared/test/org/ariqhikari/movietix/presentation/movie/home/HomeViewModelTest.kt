package org.ariqhikari.movietix.presentation.movie.home

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
import org.ariqhikari.movietix.domain.movie.model.MovieCategory
import org.ariqhikari.movietix.domain.movie.usecase.GetNowPlayingMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetPopularMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetUpcomingMoviesUseCase
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeMovieRepository()

    private val getNowPlayingUseCase = GetNowPlayingMoviesUseCase(repository)
    private val getPopularUseCase = GetPopularMoviesUseCase(repository)
    private val getUpcomingUseCase = GetUpcomingMoviesUseCase(repository)

    private fun createViewModel() = HomeViewModel(
        getNowPlayingMoviesUseCase = getNowPlayingUseCase,
        getPopularMoviesUseCase = getPopularUseCase,
        getUpcomingMoviesUseCase = getUpcomingUseCase
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
    fun initialLoad_loadsHeroAndNowPlayingCatalogSuccessfully() = runTest(testDispatcher) {
        val movies = (1..6).map { FakeMovieRepository.createDummyMovie(it, "Movie $it") }
        repository.nowPlayingResult = AppResult.Success(movies)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingHero)
        assertFalse(state.isLoadingCatalog)
        assertNull(state.heroError)
        assertNull(state.catalogError)
        assertEquals(5, state.heroMovies.size)
        assertEquals(movies, state.catalogMovies)
        assertEquals(MovieCategory.NOW_PLAYING, state.selectedCategory)
    }

    @Test
    fun selectCategory_switchesCategoryAndLoadsMovies() = runTest(testDispatcher) {
        val nowPlayingMovies = listOf(FakeMovieRepository.createDummyMovie(1, "Now Playing"))
        val popularMovies = listOf(FakeMovieRepository.createDummyMovie(2, "Popular Movie"))
        repository.nowPlayingResult = AppResult.Success(nowPlayingMovies)
        repository.popularResult = AppResult.Success(popularMovies)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.SelectCategory(MovieCategory.POPULAR))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(MovieCategory.POPULAR, state.selectedCategory)
        assertEquals(popularMovies, state.catalogMovies)
        assertFalse(state.isLoadingCatalog)
    }

    @Test
    fun selectCategory_usesInMemoryCacheWhenSwitchingBack() = runTest(testDispatcher) {
        val nowPlayingMovies = listOf(FakeMovieRepository.createDummyMovie(1, "Now Playing"))
        val popularMovies = listOf(FakeMovieRepository.createDummyMovie(2, "Popular"))
        repository.nowPlayingResult = AppResult.Success(nowPlayingMovies)
        repository.popularResult = AppResult.Success(popularMovies)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.SelectCategory(MovieCategory.POPULAR))
        advanceUntilIdle()

        // Clear nowPlayingResult to verify switching back does not trigger fetch
        repository.nowPlayingResult = AppResult.Error(AppError.Network.NoInternet)
        viewModel.onAction(HomeAction.SelectCategory(MovieCategory.NOW_PLAYING))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(MovieCategory.NOW_PLAYING, state.selectedCategory)
        assertEquals(nowPlayingMovies, state.catalogMovies)
        assertNull(state.catalogError)
    }

    @Test
    fun loadMoreMovies_appendsMoviesToCatalog() = runTest(testDispatcher) {
        val page1Movies = listOf(FakeMovieRepository.createDummyMovie(1, "Page 1 Movie"))
        val page2Movies = listOf(FakeMovieRepository.createDummyMovie(2, "Page 2 Movie"))
        repository.nowPlayingResult = AppResult.Success(page1Movies)

        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.nowPlayingResult = AppResult.Success(page2Movies)
        viewModel.onAction(HomeAction.LoadMoreMovies)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.currentPage)
        assertEquals(page1Movies + page2Movies, state.catalogMovies)
        assertFalse(state.isLoadingMore)
        assertTrue(state.canLoadMore)
    }

    @Test
    fun initialCatalogError_setsCatalogErrorState() = runTest(testDispatcher) {
        repository.nowPlayingResult = AppResult.Error(AppError.Network.NoInternet)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingCatalog)
        assertNotNull(state.catalogError)
        assertTrue(state.catalogMovies.isEmpty())
    }

    @Test
    fun paginationError_emitsShowSnackbarEffect() = runTest(testDispatcher) {
        val page1 = listOf(FakeMovieRepository.createDummyMovie(1))
        repository.nowPlayingResult = AppResult.Success(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.nowPlayingResult = AppResult.Error(AppError.Network.ServerError(500, "Server Error"))

        viewModel.effect.test {
            viewModel.onAction(HomeAction.LoadMoreMovies)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HomeEffect.ShowSnackbar)
            assertTrue(effect.message.contains("500"))
        }
    }

    @Test
    fun movieClick_emitsNavigateToDetailEffect() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(HomeAction.MovieClicked(42))
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.NavigateToDetail)
            assertEquals(42, effect.movieId)
        }
    }
}
