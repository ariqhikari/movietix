package org.ariqhikari.movietix.presentation.movie.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieCategory
import org.ariqhikari.movietix.domain.movie.usecase.GetNowPlayingMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetPopularMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetUpcomingMoviesUseCase

class HomeViewModel(
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val categoryCache = mutableMapOf<MovieCategory, CachedCategoryData>()

    private data class CachedCategoryData(
        val movies: List<Movie>,
        val currentPage: Int,
        val canLoadMore: Boolean
    )

    init {
        loadHeroMovies()
        loadCatalogMovies(category = _state.value.selectedCategory, page = 1, isInitial = true)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SelectCategory -> selectCategory(action.category)
            is HomeAction.LoadMoreMovies -> loadMoreMovies()
            is HomeAction.RetryHero -> loadHeroMovies()
            is HomeAction.RetryCatalog -> retryCatalog()
            is HomeAction.MovieClicked -> sendEffect(HomeEffect.NavigateToDetail(action.movieId))
            is HomeAction.HeroMovieClicked -> sendEffect(HomeEffect.NavigateToDetail(action.movieId))
        }
    }

    private fun loadHeroMovies() {
        _state.update { it.copy(isLoadingHero = true, heroError = null) }
        viewModelScope.launch {
            when (val result = getNowPlayingMoviesUseCase(page = 1)) {
                is AppResult.Success -> {
                    val topMovies = result.data.take(5)
                    _state.update {
                        it.copy(
                            isLoadingHero = false,
                            heroMovies = topMovies,
                            heroError = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingHero = false,
                            heroError = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun selectCategory(category: MovieCategory) {
        if (_state.value.selectedCategory == category && _state.value.catalogMovies.isNotEmpty()) {
            return
        }

        val cached = categoryCache[category]
        if (cached != null && cached.movies.isNotEmpty()) {
            _state.update {
                it.copy(
                    selectedCategory = category,
                    catalogMovies = cached.movies,
                    currentPage = cached.currentPage,
                    canLoadMore = cached.canLoadMore,
                    isLoadingCatalog = false,
                    catalogError = null
                )
            }
            return
        }

        _state.update {
            it.copy(
                selectedCategory = category,
                isLoadingCatalog = true,
                catalogMovies = emptyList(),
                catalogError = null,
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadCatalogMovies(category = category, page = 1, isInitial = true)
    }

    private fun retryCatalog() {
        val currentCategory = _state.value.selectedCategory
        _state.update {
            it.copy(
                isLoadingCatalog = true,
                catalogError = null,
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadCatalogMovies(category = currentCategory, page = 1, isInitial = true)
    }

    private fun loadCatalogMovies(category: MovieCategory, page: Int, isInitial: Boolean) {
        viewModelScope.launch {
            val result = fetchMoviesForCategory(category, page)
            when (result) {
                is AppResult.Success -> {
                    val newMovies = result.data
                    val canLoadMore = newMovies.isNotEmpty()
                    val updatedList = if (isInitial) newMovies else _state.value.catalogMovies + newMovies

                    categoryCache[category] = CachedCategoryData(
                        movies = updatedList,
                        currentPage = page,
                        canLoadMore = canLoadMore
                    )

                    _state.update {
                        it.copy(
                            isLoadingCatalog = false,
                            isLoadingMore = false,
                            catalogMovies = updatedList,
                            currentPage = page,
                            canLoadMore = canLoadMore,
                            catalogError = null
                        )
                    }
                }
                is AppResult.Error -> {
                    val errorMsg = formatErrorMessage(result.error)
                    if (isInitial) {
                        _state.update {
                            it.copy(
                                isLoadingCatalog = false,
                                isLoadingMore = false,
                                catalogError = errorMsg
                            )
                        }
                    } else {
                        _state.update { it.copy(isLoadingMore = false) }
                        sendEffect(HomeEffect.ShowSnackbar(errorMsg))
                    }
                }
            }
        }
    }

    private fun loadMoreMovies() {
        val current = _state.value
        if (current.isLoadingMore || !current.canLoadMore || current.isLoadingCatalog) {
            return
        }

        val nextPage = current.currentPage + 1
        _state.update { it.copy(isLoadingMore = true) }
        loadCatalogMovies(category = current.selectedCategory, page = nextPage, isInitial = false)
    }

    private suspend fun fetchMoviesForCategory(category: MovieCategory, page: Int): AppResult<List<Movie>> {
        return when (category) {
            MovieCategory.NOW_PLAYING -> getNowPlayingMoviesUseCase(page)
            MovieCategory.POPULAR -> getPopularMoviesUseCase(page)
            MovieCategory.UPCOMING -> getUpcomingMoviesUseCase(page)
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun formatErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.Network.NoInternet -> "Periksa koneksi internet Anda dan coba lagi."
            is AppError.Network.Timeout -> "Koneksi terputus. Silakan coba lagi."
            is AppError.Network.ServerError -> "Terjadi kendala pada server (${error.code})."
            is AppError.Network.Unauthorized -> "Akses tidak diizinkan. Periksa konfigurasi token TMDB Anda."
            is AppError.Storage.ReadFailed,
            is AppError.Storage.WriteFailed -> "Gagal memuat data lokal."
            is AppError.Booking.SeatAlreadyOccupied,
            is AppError.Booking.PaymentFailedSimulated,
            is AppError.Booking.SaveTicketFailed,
            is AppError.Booking.RollbackFailed,
            is AppError.Booking.InvalidSelection -> "Kendala pada pemesanan."
            is AppError.Unknown -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
