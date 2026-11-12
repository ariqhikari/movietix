package org.ariqhikari.movietix.presentation.ticket.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ariqhikari.movietix.core.presentation.components.ErrorStateView
import org.ariqhikari.movietix.core.presentation.theme.Dimensions
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing
import org.ariqhikari.movietix.domain.ticket.model.TicketDisplayItem
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import org.ariqhikari.movietix.presentation.ticket.components.OfflineQrCode
import org.ariqhikari.movietix.presentation.ticket.components.PerforatedTicketCard
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.Action
import org.ariqhikari.movietix.presentation.ticket.detail.TicketDetailContract.State

@Composable
fun TicketDetailScreen(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MovieTixColors.CinemaSurfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MovieTixColors.CinemaSurfaceCard)
                            .clickable(
                                role = Role.Button,
                                onClick = { onAction(Action.BackClicked) }
                            )
                            .semantics {
                                contentDescription = "Kembali ke riwayat tiket"
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

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Text(
                        text = "Detail E-Tiket",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                state.errorMessage != null && state.ticketItem == null -> {
                    ErrorStateView(
                        title = "Tiket Tidak Ditemukan",
                        message = state.errorMessage,
                        onRetry = { onAction(Action.RetryClicked) }
                    )
                }

                state.ticketItem != null -> {
                    val item = state.ticketItem
                    val ticket = item.ticket

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Perforated Ticket Card
                        PerforatedTicketCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MovieTixColors.CinemaSurfaceCard,
                            borderColor = MovieTixColors.CinemaSurfaceBorder,
                            topContent = {
                                TicketTopSection(item = item)
                            },
                            bottomContent = {
                                TicketBottomSection(
                                    item = item,
                                    onQrClick = { onAction(Action.QrClicked) }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Future Work Notice Card
                        Card(
                            shape = RoundedCornerShape(Radius.md),
                            colors = CardDefaults.cardColors(
                                containerColor = MovieTixColors.CinemaSurfaceSubtle
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MovieTixColors.CinemaSurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Catatan: Fitur simpan tiket ke galeri dan bagikan tiket akan tersedia pada pembaruan mendatang.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MovieTixColors.CinemaTextTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.md)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }

                    // Enlarge QR Dialog
                    if (state.isQrEnlarged && item.isActive) {
                        Dialog(onDismissRequest = { onAction(Action.DismissQrDialog) }) {
                            Card(
                                shape = RoundedCornerShape(Radius.lg),
                                colors = CardDefaults.cardColors(
                                    containerColor = MovieTixColors.CinemaSurfaceCard
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MovieTixColors.CinemaSurfaceBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.sm)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Kode QR E-Tiket",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.xs))

                                    Text(
                                        text = ticket.bookingCode,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieTixColors.CinemaPrimary
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.md))

                                    OfflineQrCode(
                                        payload = ticket.qrPayload,
                                        bookingCode = ticket.bookingCode,
                                        size = 220.dp
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.md))

                                    Text(
                                        text = "Arahkan layar ponsel ke scanner gerbang masuk studio bioskop.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MovieTixColors.CinemaTextSecondary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.lg))

                                    Button(
                                        onClick = { onAction(Action.DismissQrDialog) },
                                        shape = RoundedCornerShape(Radius.md),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MovieTixColors.CinemaPrimary,
                                            contentColor = MovieTixColors.CinemaOnPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "Tutup",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketTopSection(
    item: TicketDisplayItem,
    modifier: Modifier = Modifier
) {
    val ticket = item.ticket

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MOVIETIX OFFICIAL PASS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = MovieTixColors.CinemaTextSecondary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.xs))

            DetailStatusChip(status = item.derivedStatus)
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(Spacing.md))

        DetailRow(label = "Bioskop", value = ticket.cinemaName)
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Studio", value = "${ticket.studioName} (${ticket.studioType})")
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Waktu Tayang", value = ticket.showtimeIso)
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Kursi", value = "${ticket.seats.joinToString(", ")} (${ticket.seats.size} Kursi)")
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Metode Bayar", value = ticket.paymentMethod.displayName)
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Waktu Transaksi", value = formatEpochTime(ticket.transactionTimeEpochMs))
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Total Bayar", value = formatRupiah(ticket.totalPrice), isHighlight = true)
        Spacer(modifier = Modifier.height(Spacing.xs))
        DetailRow(label = "Kode Booking", value = ticket.bookingCode, isMonospace = true, isHighlight = true)
    }
}

@Composable
private fun TicketBottomSection(
    item: TicketDisplayItem,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticket = item.ticket

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            item.isActive -> {
                Text(
                    text = "PINTU MASUK STUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = MovieTixColors.CinemaTextSecondary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                OfflineQrCode(
                    payload = ticket.qrPayload,
                    bookingCode = ticket.bookingCode,
                    size = Dimensions.qrCodeSize,
                    onClick = onQrClick
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Ketuk QR code untuk memperbesar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MovieTixColors.CinemaTextSecondary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Tunjukkan kode QR ini kepada petugas bioskop saat masuk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieTixColors.CinemaTextTertiary,
                    textAlign = TextAlign.Center
                )
            }

            item.isExpired -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(MovieTixColors.CinemaSurfaceSubtle)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tiket Telah Selesai",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Waktu penayangan film ini telah berlalu. Tiket tidak dapat digunakan lagi untuk masuk ke studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MovieTixColors.CinemaTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item.isCancelled -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(MovieTixColors.StateErrorDark.copy(alpha = 0.15f))
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tiket Dibatalkan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MovieTixColors.StateErrorDark
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Pemesanan tiket bioskop ini telah dibatalkan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MovieTixColors.CinemaTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isMonospace: Boolean = false,
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
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = if (isHighlight) MovieTixColors.CinemaPrimary else Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailStatusChip(
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
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

private fun formatEpochTime(epochMs: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = ldt.dayOfMonth.toString().padStart(2, '0')
        val month = ldt.monthNumber.toString().padStart(2, '0')
        val year = ldt.year
        val hour = ldt.hour.toString().padStart(2, '0')
        val min = ldt.minute.toString().padStart(2, '0')
        "$day/$month/$year $hour:$min"
    } catch (_: Exception) {
        "-"
    }
}
