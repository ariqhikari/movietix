package org.ariqhikari.movietix.presentation.cinema.seatmap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.domain.cinema.usecase.BookingPriceBreakdown

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeatSummaryBottomBar(
    selectedSeats: List<String>,
    priceBreakdown: BookingPriceBreakdown,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MovieTixColors.CinemaSurfaceCard,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Selected Seats Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kursi Terpilih:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MovieTixColors.CinemaTextSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                if (selectedSeats.isEmpty()) {
                    Text(
                        text = "Maksimal 6 kursi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedSeats.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedSeats.sorted().forEach { seatId ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = seatId,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MovieTixColors.CinemaOnPrimary
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Silakan ketuk kursi yang tersedia di peta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieTixColors.CinemaTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Price and CTA Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Harga",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaTextSecondary
                    )
                    Text(
                        text = formatRupiah(priceBreakdown.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSeats.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.White
                    )
                    if (priceBreakdown.serviceFee > 0L) {
                        Text(
                            text = "Termasuk biaya layanan ${formatRupiah(priceBreakdown.serviceFee)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MovieTixColors.CinemaTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onProceed,
                    enabled = selectedSeats.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MovieTixColors.CinemaSurfaceSubtle,
                        disabledContentColor = MovieTixColors.CinemaTextTertiary
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Lanjutkan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
