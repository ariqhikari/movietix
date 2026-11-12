package org.ariqhikari.movietix.presentation.cinema.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import org.ariqhikari.movietix.core.presentation.components.EmptyStateView
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.presentation.cinema.schedule.components.CinemaShowtimeCard
import org.ariqhikari.movietix.presentation.cinema.schedule.components.HorizontalDatePicker

@Composable
fun ScheduleScreen(
    state: ScheduleState,
    onAction: (ScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ScheduleTopBar(
                movieTitle = state.movieTitle,
                onBackClick = { onAction(ScheduleAction.BackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Date Picker
            HorizontalDatePicker(
                selectedDate = state.selectedDate,
                availableDates = state.availableDates,
                onDateSelected = { onAction(ScheduleAction.SelectDate(it)) }
            )

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
                        title = "Gagal Memuat Jadwal",
                        message = state.errorMessage,
                        onRetry = { onAction(ScheduleAction.Retry) }
                    )
                }
                state.cinemas.isEmpty() -> {
                    EmptyStateView(
                        title = "Jadwal Tidak Tersedia",
                        message = "Belum ada bioskop yang menayangkan film ini pada tanggal yang dipilih."
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.cinemas, key = { it.id }) { cinema ->
                            CinemaShowtimeCard(
                                cinema = cinema,
                                onShowtimeSelected = { c, s, st ->
                                    onAction(ScheduleAction.SelectShowtime(c, s, st))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTopBar(
    movieTitle: String,
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
                        contentDescription = "Kembali ke detail film"
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
                    text = "Pilih Jadwal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (movieTitle.isNotBlank()) {
                    Text(
                        text = movieTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MovieTixColors.CinemaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
