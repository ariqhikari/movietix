package org.ariqhikari.movietix.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.ariqhikari.movietix.presentation.movie.detail.DetailEffect
import org.ariqhikari.movietix.presentation.movie.detail.DetailScreenContent
import org.ariqhikari.movietix.presentation.movie.detail.DetailViewModel
import org.ariqhikari.movietix.presentation.movie.home.HomeEffect
import org.ariqhikari.movietix.presentation.movie.home.HomeScreen
import org.ariqhikari.movietix.presentation.movie.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Top-level application navigation graph.
 * Connects HomeScreen and DetailScreen with Koin-injected ViewModels.
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
                            val currentRoute = navController.currentDestination?.route
                            if (currentRoute == null || !currentRoute.contains("Detail")) {
                                navController.navigate(ScreenDestination.Detail(effect.movieId)) {
                                    launchSingleTop = true
                                }
                            }
                        }
                        is HomeEffect.ShowSnackbar -> {
                            // Handled inside HomeScreen snackbar host
                        }
                    }
                }
            }

            HomeScreen(
                state = state,
                onAction = viewModel::onAction
            )
        }

        composable<ScreenDestination.Detail> { backStackEntry ->
            val detailDestination = backStackEntry.toRoute<ScreenDestination.Detail>()
            val movieId = detailDestination.movieId
            val viewModel: DetailViewModel = koinViewModel(parameters = { parametersOf(movieId) })
            val state by viewModel.state.collectAsStateWithLifecycle()
            val uriHandler = LocalUriHandler.current
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is DetailEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is DetailEffect.OpenUrl -> {
                            try {
                                uriHandler.openUri(effect.url)
                            } catch (_: Throwable) {
                                snackbarHostState.showSnackbar("Tidak dapat membuka tautan trailer.")
                            }
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
                snackbarHostState = snackbarHostState
            )
        }
    }
}
