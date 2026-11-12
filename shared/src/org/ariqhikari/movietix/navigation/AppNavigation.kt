package org.ariqhikari.movietix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.ariqhikari.movietix.presentation.movie.home.HomeEffect
import org.ariqhikari.movietix.presentation.movie.home.HomeScreen
import org.ariqhikari.movietix.presentation.movie.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top-level application navigation graph.
 * Connects HomeScreen with Koin-injected ViewModel and handles single-shot effects.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ScreenDestination.Home,
        modifier = modifier
    ) {
        composable<ScreenDestination.Home> {
            val viewModel: HomeViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is HomeEffect.NavigateToDetail -> {
                            navController.navigate(ScreenDestination.Detail(effect.movieId))
                        }
                        is HomeEffect.ShowSnackbar -> {
                            // Can be consumed by snackbar host
                        }
                    }
                }
            }

            HomeScreen(
                state = state,
                onAction = viewModel::onAction
            )
        }

        composable<ScreenDestination.Detail> {
            // Reserved for Phase 2: Movie Detail Screen
        }
    }
}
