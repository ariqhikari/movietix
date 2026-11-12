package org.ariqhikari.movietix.presentation.booking.checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.domain.ticket.model.Ticket

@Composable
fun CheckoutSuccessContent(
    ticket: Ticket,
    onViewTicketClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    fun formatRupiah(amount: Long): String {
        return "Rp ${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon Circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MovieTixColors.StateSuccessContainer)
                .border(2.dp, MovieTixColors.StateSuccess, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = MovieTixColors.StateSuccess,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pembayaran Berhasil!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tiket Anda telah terbit dan tersimpan secara offline di perangkat ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MovieTixColors.CinemaTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Booking Code Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MovieTixColors.CinemaSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KODE BOOKING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MovieTixColors.CinemaTextSecondary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = ticket.bookingCode,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MovieTixColors.CinemaPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Items
                DetailRow(label = "Film", value = ticket.movieTitle)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Bioskop", value = ticket.cinemaName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Studio", value = "${ticket.studioName} (${ticket.studioType})")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Waktu", value = ticket.showtimeIso)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Kursi", value = ticket.seats.joinToString(", "))
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Metode", value = ticket.paymentMethod.displayName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Total Dibayar", value = formatRupiah(ticket.totalPrice), isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Button(
            onClick = onViewTicketClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MovieTixColors.CinemaPrimary,
                contentColor = MovieTixColors.CinemaOnPrimary
            )
        ) {
            Text(
                text = "Lihat Tiket",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Kembali ke Beranda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MovieTixColors.CinemaTextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MovieTixColors.CinemaPrimary else Color.White,
            textAlign = TextAlign.End
        )
    }
}
