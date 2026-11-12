package org.ariqhikari.movietix.presentation.movie.home

import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieCategory

data class HomeState(
    val isLoadingHero: Boolean = true,
    val heroMovies: List<Movie> = emptyList(),
    val heroError: String? = null,
    val selectedCategory: MovieCategory = MovieCategory.NOW_PLAYING,
    val isLoadingCatalog: Boolean = true,
    val catalogMovies: List<Movie> = emptyList(),
    val catalogError: String? = null,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1
)

sealed interface HomeAction {
    data class SelectCategory(val category: MovieCategory) : HomeAction
    data object LoadMoreMovies : HomeAction
    data object RetryHero : HomeAction
    data object RetryCatalog : HomeAction
    data class MovieClicked(val movieId: Int) : HomeAction
    data class HeroMovieClicked(val movieId: Int) : HomeAction
}

sealed interface HomeEffect {
    data class NavigateToDetail(val movieId: Int) : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}

object HomeContract {
    typealias State = HomeState
    typealias Action = HomeAction
    typealias Effect = HomeEffect
}
