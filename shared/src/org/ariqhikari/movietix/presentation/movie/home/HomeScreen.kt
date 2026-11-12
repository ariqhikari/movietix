package org.ariqhikari.movietix.presentation.movie.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.core.presentation.components.EmptyStateView
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.presentation.movie.home.components.CategoryFilterChips
import org.ariqhikari.movietix.presentation.movie.home.components.HeroBannerCarousel
import org.ariqhikari.movietix.presentation.movie.home.components.HeroBannerSkeleton
import org.ariqhikari.movietix.presentation.movie.home.components.MovieCard
import org.ariqhikari.movietix.presentation.movie.home.components.MovieCardSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.canLoadMore && !state.isLoadingMore && !state.isLoadingCatalog) {
            onAction(HomeAction.LoadMoreMovies)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MovieTix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero section
            item(span = { GridItemSpan(maxLineSpan) }) {
                ColumnHeaderSection(
                    state = state,
                    onAction = onAction
                )
            }

            // Catalog items
            if (state.isLoadingCatalog) {
                items(6) {
                    MovieCardSkeleton()
                }
            } else if (state.catalogError != null && state.catalogMovies.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorStateView(
                        title = "Gagal Memuat Katalog",
                        message = state.catalogError,
                        onRetry = { onAction(HomeAction.RetryCatalog) }
                    )
                }
            } else if (state.catalogMovies.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateView(
                        title = "Tidak Ada Film",
                        message = "Belum ada film untuk kategori ini."
                    )
                }
            } else {
                items(
                    items = state.catalogMovies,
                    key = { it.id }
                ) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onAction(HomeAction.MovieClicked(movie.id)) }
                    )
                }

                if (state.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnHeaderSection(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        if (state.isLoadingHero) {
            HeroBannerSkeleton()
        } else if (state.heroError != null && state.heroMovies.isEmpty()) {
            ErrorStateView(
                title = "Gagal Memuat Film Unggulan",
                message = state.heroError,
                onRetry = { onAction(HomeAction.RetryHero) }
            )
        } else {
            HeroBannerCarousel(
                movies = state.heroMovies,
                onMovieClick = { onAction(HomeAction.HeroMovieClicked(it)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kategori",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        CategoryFilterChips(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onAction(HomeAction.SelectCategory(it)) }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}
