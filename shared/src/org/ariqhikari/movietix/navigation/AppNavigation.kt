package org.ariqhikari.movietix.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutEffect
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutScreen
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutViewModel
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleEffect
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleScreen
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleViewModel
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapEffect
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapScreen
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapViewModel
import org.ariqhikari.movietix.presentation.movie.detail.DetailEffect
import org.ariqhikari.movietix.presentation.movie.detail.DetailScreenContent
import org.ariqhikari.movietix.presentation.movie.detail.DetailViewModel
import org.ariqhikari.movietix.presentation.movie.home.HomeEffect
import org.ariqhikari.movietix.presentation.movie.home.HomeScreen
import org.ariqhikari.movietix.presentation.movie.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
                        is DetailEffect.NavigateToSchedule -> {
                            navController.navigate(
                                ScreenDestination.Schedule(
                                    movieId = effect.movieId,
                                    movieTitle = effect.movieTitle,
                                    posterUrl = effect.posterUrl
                                )
                            ) {
                                launchSingleTop = true
                            }
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

        composable<ScreenDestination.Schedule> { backStackEntry ->
            val destination = backStackEntry.toRoute<ScreenDestination.Schedule>()
            val viewModel: ScheduleViewModel = koinViewModel(
                parameters = {
                    parametersOf(destination.movieId, destination.movieTitle, destination.posterUrl)
                }
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is ScheduleEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is ScheduleEffect.NavigateToSeatMap -> {
                            navController.navigate(
                                ScreenDestination.SeatMap(
                                    scheduleId = effect.scheduleId,
                                    movieId = effect.movieId,
                                    movieTitle = effect.movieTitle,
                                    cinemaId = effect.cinemaId,
                                    cinemaName = effect.cinemaName,
                                    studioId = effect.studioId,
                                    studioName = effect.studioName,
                                    studioType = effect.studioType,
                                    dateStr = effect.dateStr,
                                    timeStr = effect.timeStr,
                                    pricePerSeat = effect.pricePerSeat
                                )
                            ) {
                                launchSingleTop = true
                            }
                        }
                        is ScheduleEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            ScheduleScreen(
                state = state,
                onAction = viewModel::onAction,
                snackbarHostState = snackbarHostState
            )
        }

        composable<ScreenDestination.SeatMap> { backStackEntry ->
            val destination = backStackEntry.toRoute<ScreenDestination.SeatMap>()
            val navParams = org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapNavParams(
                scheduleId = destination.scheduleId,
                movieId = destination.movieId,
                movieTitle = destination.movieTitle,
                cinemaId = destination.cinemaId,
                cinemaName = destination.cinemaName,
                studioId = destination.studioId,
                studioName = destination.studioName,
                studioTypeStr = destination.studioType,
                dateStr = destination.dateStr,
                timeStr = destination.timeStr,
                pricePerSeat = destination.pricePerSeat
            )
            val viewModel: SeatMapViewModel = koinViewModel(
                parameters = {
                    parametersOf(navParams)
                }
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is SeatMapEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is SeatMapEffect.NavigateToCheckout -> {
                            navController.navigate(
                                ScreenDestination.Checkout(
                                    scheduleId = effect.scheduleId,
                                    movieId = effect.movieId,
                                    movieTitle = effect.movieTitle,
                                    cinemaName = effect.cinemaName,
                                    studioName = effect.studioName,
                                    studioType = effect.studioType,
                                    dateStr = effect.dateStr,
                                    timeStr = effect.timeStr,
                                    seats = effect.selectedSeats,
                                    pricePerSeat = effect.pricePerSeat,
                                    subtotal = effect.subtotal,
                                    serviceFee = effect.serviceFee,
                                    totalPrice = effect.totalPrice
                                )
                            ) {
                                launchSingleTop = true
                            }
                        }
                        is SeatMapEffect.ShowWarning -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                        is SeatMapEffect.ShowError -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            SeatMapScreen(
                state = state,
                onAction = viewModel::onAction,
                snackbarHostState = snackbarHostState
            )
        }

        composable<ScreenDestination.Checkout> { backStackEntry ->
            val destination = backStackEntry.toRoute<ScreenDestination.Checkout>()
            val order = BookingOrder(
                scheduleId = destination.scheduleId,
                movieId = destination.movieId,
                movieTitle = destination.movieTitle,
                cinemaId = destination.cinemaId,
                cinemaName = destination.cinemaName,
                studioName = destination.studioName,
                studioType = destination.studioType,
                showtime = destination.timeStr,
                date = destination.dateStr,
                selectedSeats = destination.seats,
                pricePerSeat = destination.pricePerSeat,
                serviceFee = destination.serviceFee,
                totalPrice = destination.totalPrice,
                posterUrl = destination.posterUrl
            )
            val viewModel: CheckoutViewModel = koinViewModel(
                parameters = { parametersOf(order) }
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is CheckoutEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is CheckoutEffect.NavigateToTicketDetail -> {
                            navController.popBackStack(ScreenDestination.Home, inclusive = false)
                        }
                        is CheckoutEffect.NavigateToHome -> {
                            navController.popBackStack(ScreenDestination.Home, inclusive = false)
                        }
                        is CheckoutEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                }
            }

            CheckoutScreen(
                state = state,
                onAction = viewModel::onAction,
                snackbarHostState = snackbarHostState
            )
        }
    }
}
