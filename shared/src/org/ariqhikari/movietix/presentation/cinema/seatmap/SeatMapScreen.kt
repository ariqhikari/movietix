package org.ariqhikari.movietix.presentation.cinema.seatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.presentation.cinema.seatmap.components.CinemaScreenCurved
import org.ariqhikari.movietix.presentation.cinema.seatmap.components.SeatMapGrid
import org.ariqhikari.movietix.presentation.cinema.seatmap.components.SeatMapLegend
import org.ariqhikari.movietix.presentation.cinema.seatmap.components.SeatSummaryBottomBar

@Composable
fun SeatMapScreen(
    state: SeatMapState,
    onAction: (SeatMapAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SeatMapTopBar(
                cinemaName = state.cinemaName,
                studioName = state.studioName,
                onBackClick = { onAction(SeatMapAction.BackClicked) }
            )
        },
        bottomBar = {
            SeatSummaryBottomBar(
                selectedSeats = state.selectedSeats,
                priceBreakdown = state.priceBreakdown,
                onProceed = { onAction(SeatMapAction.ProceedToCheckout) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Showtime details banner pill
            ShowtimeInfoBanner(
                movieTitle = state.movieTitle,
                dateStr = state.dateStr,
                timeStr = state.timeStr
            )

            // Curved Cinema Screen
            CinemaScreenCurved()

            // Seat Legend
            SeatMapLegend()

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                state.errorMessage != null -> {
                    ErrorStateView(
                        title = "Gagal Memuat Denah",
                        message = state.errorMessage,
                        onRetry = { /* reload handled at VM init */ }
                    )
                }
                state.seatLayout != null -> {
                    SeatMapGrid(
                        seatLayout = state.seatLayout,
                        selectedSeats = state.selectedSeats,
                        onSeatClick = { onAction(SeatMapAction.ToggleSeat(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatMapTopBar(
    cinemaName: String,
    studioName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MovieTixColors.CinemaSurfaceCard)
                    .clickable(role = Role.Button, onClick = onBackClick)
                    .semantics {
                        contentDescription = "Kembali ke jadwal tayang"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Pilih Kursi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$cinemaName • $studioName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieTixColors.CinemaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShowtimeInfoBanner(
    movieTitle: String,
    dateStr: String,
    timeStr: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp) )
            .background(MovieTixColors.CinemaSurfaceCard)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$movieTitle • $dateStr, $timeStr WIB",
            style = MaterialTheme.typography.labelSmall,
            color = MovieTixColors.CinemaTextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
