package org.ariqhikari.movietix.presentation.movie.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.presentation.movie.detail.components.BackdropHeroHeader
import org.ariqhikari.movietix.presentation.movie.detail.components.CastCarousel
import org.ariqhikari.movietix.presentation.movie.detail.components.DetailSkeleton
import org.ariqhikari.movietix.presentation.movie.detail.components.OverlappingPosterHeader
import org.ariqhikari.movietix.presentation.movie.detail.components.StickyBottomCtaBar
import org.ariqhikari.movietix.presentation.movie.detail.components.SynopsisSection
import org.ariqhikari.movietix.presentation.movie.detail.components.TrailerCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DetailScreen(
    movieId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = koinViewModel(parameters = { parametersOf(movieId) })
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailEffect.NavigateBack -> onNavigateBack()
                is DetailEffect.NavigateToSchedule -> {
                    // Handled at navigation level
                }
                is DetailEffect.OpenUrl -> {
                    // Handled at navigation level or uri handler
                }
                is DetailEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    DetailScreenContent(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun DetailScreenContent(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            if (state.movieDetail != null) {
                StickyBottomCtaBar(
                    onSelectSchedule = { onAction(DetailAction.SelectScheduleClicked) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoadingDetail -> {
                DetailSkeleton()
            }
            state.detailError != null && state.movieDetail == null -> {
                ErrorStateView(
                    title = "Gagal Memuat Detail Film",
                    message = state.detailError,
                    onRetry = { onAction(DetailAction.RetryAll) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.movieDetail != null -> {
                val detail = state.movieDetail
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    BackdropHeroHeader(
                        backdropUrl = detail.backdropUrl,
                        posterFallbackUrl = detail.posterUrl,
                        movieTitle = detail.title,
                        onBackClick = { onAction(DetailAction.BackClicked) }
                    )

                    OverlappingPosterHeader(
                        movie = detail
                    )

                    SynopsisSection(
                        overview = detail.overview,
                        isExpanded = state.isSynopsisExpanded,
                        onToggleExpand = { onAction(DetailAction.ToggleSynopsis) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CastCarousel(
                        castMembers = state.castMembers,
                        isLoading = state.isLoadingCredits
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TrailerCard(
                        trailers = state.trailers,
                        isLoading = state.isLoadingTrailers,
                        onPlayTrailer = { onAction(DetailAction.PlayTrailer(it)) }
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
