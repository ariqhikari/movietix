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
import org.ariqhikari.movietix.core.presentation.components.BottomNavTab
import org.ariqhikari.movietix.core.presentation.components.MovieTixBottomBar
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutEffect
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutScreen
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutViewModel
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleEffect
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleScreen
import org.ariqhikari.movietix.presentation.cinema.schedule.ScheduleViewModel
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapEffect
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapNavParams
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapScreen
import org.ariqhikari.movietix.presentation.cinema.seatmap.SeatMapViewModel
import org.ariqhikari.movietix.presentation.movie.detail.DetailEffect
import org.ariqhikari.movietix.presentation.movie.detail.DetailScreenContent
import org.ariqhikari.movietix.presentation.movie.detail.DetailViewModel
import org.ariqhikari.movietix.presentation.movie.home.HomeEffect
import org.ariqhikari.movietix.presentation.movie.home.HomeScreen
import org.ariqhikari.movietix.presentation.movie.home.HomeViewModel
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailScreen
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailViewModel
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryScreen
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryViewModel
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
                onAction = viewModel::onAction,
                bottomBar = {
                    MovieTixBottomBar(
                        currentTab = BottomNavTab.FILM,
                        onTabSelected = { tab ->
                            if (tab == BottomNavTab.TIKET_SAYA) {
                                navController.navigate(ScreenDestination.TicketHistory) {
                                    popUpTo(ScreenDestination.Home) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            )
        }

        composable<ScreenDestination.TicketHistory> {
            val viewModel: TicketHistoryViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is TicketHistoryContract.Effect.NavigateToDetail -> {
                            navController.navigate(ScreenDestination.TicketDetail(effect.bookingCode)) {
                                launchSingleTop = true
                            }
                        }
                        is TicketHistoryContract.Effect.NavigateToHome -> {
                            navController.navigate(ScreenDestination.Home) {
                                popUpTo(ScreenDestination.Home) {
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }

            TicketHistoryScreen(
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
            val navParams = SeatMapNavParams(
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
                            navController.navigate(ScreenDestination.TicketHistory) {
                                popUpTo(ScreenDestination.Home) { inclusive = false }
                            }
                            navController.navigate(ScreenDestination.TicketDetail(effect.bookingCode)) {
                                launchSingleTop = true
                            }
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

        composable<ScreenDestination.TicketDetail> { backStackEntry ->
            val destination = backStackEntry.toRoute<ScreenDestination.TicketDetail>()
            val viewModel: TicketDetailViewModel = koinViewModel(
                parameters = { parametersOf(destination.bookingCode) }
            )
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is TicketDetailContract.Effect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is TicketDetailContract.Effect.ShowSnackbar -> {
                            // Handled inside TicketDetailScreen snackbar host
                        }
                    }
                }
            }

            TicketDetailScreen(
                state = state,
                onAction = viewModel::onAction
            )
        }
    }
}
