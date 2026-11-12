package org.ariqhikari.movietix.presentation.cinema.schedule.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.Showtime
import org.ariqhikari.movietix.domain.cinema.model.Studio
import org.ariqhikari.movietix.domain.cinema.model.StudioType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CinemaShowtimeCard(
    cinema: Cinema,
    onShowtimeSelected: (cinema: Cinema, studio: Studio, showtime: Showtime) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MovieTixColors.CinemaSurfaceCard
        ),
        border = BorderStroke(1.dp, MovieTixColors.CinemaSurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cinema Name & Location
            Text(
                text = cinema.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = cinema.address,
                style = MaterialTheme.typography.bodySmall,
                color = MovieTixColors.CinemaTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Studios
            cinema.studios.forEachIndexed { index, studio ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Studio Header with badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = studio.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StudioBadge(type = studio.type)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Showtimes Grid
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    studio.showtimes.forEach { showtime ->
                        ShowtimeChip(
                            showtime = showtime,
                            onClick = { onShowtimeSelected(cinema, studio, showtime) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioBadge(
    type: StudioType,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, label) = when (type) {
        StudioType.REGULAR -> Triple(MovieTixColors.CinemaSurfaceSubtle, MovieTixColors.CinemaTextSecondary, "2D")
        StudioType.FOUR_DX -> Triple(MovieTixColors.CinemaPrimaryContainer, MovieTixColors.CinemaPrimary, "4DX")
        StudioType.STARIUM -> Triple(MovieTixColors.CinemaSurfaceSubtle, MovieTixColors.CinemaSecondary, "STARIUM")
        StudioType.VELVET -> Triple(MovieTixColors.CinemaPrimaryContainer, MovieTixColors.CinemaSecondary, "VELVET")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ShowtimeChip(
    showtime: Showtime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableSeats = showtime.totalSeatCount - showtime.occupiedSeatCount
    val formattedPrice = formatRupiah(showtime.price)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MovieTixColors.CinemaSurfaceSubtle)
            .border(1.dp, MovieTixColors.CinemaSurfaceBorder, RoundedCornerShape(10.dp))
            .semantics {
                role = Role.Button
                contentDescription = "Jam tayang ${showtime.time}, harga $formattedPrice, $availableSeats kursi tersedia"
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = showtime.time,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedPrice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$availableSeats sisa",
                style = MaterialTheme.typography.labelSmall,
                color = MovieTixColors.CinemaTextTertiary,
                fontSize = 10.sp
            )
        }
    }
}

private fun formatRupiah(amount: Long): String {
    val str = amount.toString()
    val reversed = str.reversed()
    val chunks = reversed.chunked(3)
    val formatted = chunks.joinToString(".").reversed()
    return "Rp $formatted"
}
