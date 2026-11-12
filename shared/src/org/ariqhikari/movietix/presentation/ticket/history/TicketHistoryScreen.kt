package org.ariqhikari.movietix.presentation.ticket.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.ariqhikari.movietix.core.presentation.components.BottomNavTab
import org.ariqhikari.movietix.core.presentation.components.EmptyStateView
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.core.presentation.components.MovieTixBottomBar
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing
import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.Action
import org.ariqhikari.movietix.presentation.ticket.history.TicketHistoryContract.State

@Composable
fun TicketHistoryScreen(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Tiket Saya",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MovieTixColors.CinemaPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            MovieTixBottomBar(
                currentTab = BottomNavTab.TIKET_SAYA,
                onTabSelected = { tab ->
                    if (tab == BottomNavTab.FILM) {
                        onAction(Action.ExploreMoviesClicked)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MovieTixColors.CinemaPrimary)
                    }
                }

                state.errorMessage != null && state.isEmpty -> {
                    ErrorStateView(
                        title = "Gagal Memuat Tiket",
                        message = state.errorMessage,
                        onRetry = { onAction(Action.RetryClicked) }
                    )
                }

                state.isEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyStateView(
                            title = "Belum Ada Tiket Bioskop",
                            message = "Tiket bioskop yang berhasil Anda beli akan tersimpan dan dapat diakses di sini secara offline."
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Button(
                            onClick = { onAction(Action.ExploreMoviesClicked) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MovieTixColors.CinemaPrimary,
                                contentColor = MovieTixColors.CinemaOnPrimary
                            ),
                            shape = RoundedCornerShape(Radius.md),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Eksplor Film Sekarang",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.md,
                            end = Spacing.md,
                            top = Spacing.sm,
                            bottom = Spacing.lg
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        if (state.upcomingTickets.isNotEmpty()) {
                            item(key = "header_upcoming") {
                                SectionHeader(
                                    title = "Akan Datang",
                                    count = state.upcomingTickets.size
                                )
                            }

                            items(
                                items = state.upcomingTickets,
                                key = { it.ticket.bookingCode }
                            ) { item ->
                                TicketHistoryCard(
                                    item = item,
                                    onClick = { onAction(Action.TicketClicked(item.ticket.bookingCode)) }
                                )
                            }
                        }

                        if (state.completedTickets.isNotEmpty()) {
                            item(key = "header_completed") {
                                SectionHeader(
                                    title = "Riwayat Selesai",
                                    count = state.completedTickets.size
                                )
                            }

                            items(
                                items = state.completedTickets,
                                key = { it.ticket.bookingCode }
                            ) { item ->
                                TicketHistoryCard(
                                    item = item,
                                    onClick = { onAction(Action.TicketClicked(item.ticket.bookingCode)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(MovieTixColors.CinemaSurfaceSubtle)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MovieTixColors.CinemaTextSecondary
            )
        }
    }
}

@Composable
private fun TicketHistoryCard(
    item: TicketDisplayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticket = item.ticket

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .border(
                width = 1.dp,
                color = MovieTixColors.CinemaSurfaceBorder,
                shape = RoundedCornerShape(Radius.lg)
            )
            .semantics {
                role = Role.Button
                contentDescription = "Tiket ${ticket.movieTitle}, kode booking ${ticket.bookingCode}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MovieTixColors.CinemaSurfaceCard
        ),
        shape = RoundedCornerShape(Radius.lg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster Thumbnail
            if (!ticket.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ticket.posterUrl,
                    contentDescription = "Poster film ${ticket.movieTitle}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 72.dp, height = 104.dp)
                        .clip(RoundedCornerShape(Radius.sm))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 104.dp)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(MovieTixColors.CinemaSurfaceSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FILM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticket.bookingCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MovieTixColors.CinemaPrimary
                    )

                    StatusChip(status = item.derivedStatus)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${ticket.cinemaName} • ${ticket.studioName} (${ticket.studioType})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieTixColors.CinemaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = ticket.showtimeIso,
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieTixColors.CinemaTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ticket.seats.size} Kursi (${ticket.seats.joinToString(", ")})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    Text(
                        text = formatRupiah(ticket.totalPrice),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MovieTixColors.CinemaPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: TicketStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        TicketStatus.ACTIVE -> Triple(
            MovieTixColors.StateSuccess.copy(alpha = 0.15f),
            MovieTixColors.StateSuccess,
            "Aktif"
        )
        TicketStatus.EXPIRED -> Triple(
            MovieTixColors.CinemaSurfaceSubtle,
            MovieTixColors.CinemaTextSecondary,
            "Selesai"
        )
        TicketStatus.CANCELLED -> Triple(
            MovieTixColors.StateErrorDark.copy(alpha = 0.15f),
            MovieTixColors.StateErrorDark,
            "Dibatalkan"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val formatted = amount.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "Rp $formatted"
}
