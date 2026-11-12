package org.ariqhikari.movietix.presentation.booking.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.presentation.booking.checkout.components.CheckoutFailureContent
import org.ariqhikari.movietix.presentation.booking.checkout.components.CheckoutSuccessContent
import org.ariqhikari.movietix.presentation.booking.checkout.components.PaymentMethodBottomSheet

@Composable
fun CheckoutScreen(
    state: CheckoutState,
    onAction: (CheckoutAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    when (val status = state.paymentStatus) {
        is PaymentStatus.Success -> {
            CheckoutSuccessContent(
                ticket = status.ticket,
                onViewTicketClick = { onAction(CheckoutAction.ViewTicketClicked) },
                onBackToHomeClick = { onAction(CheckoutAction.BackToHomeClicked) },
                modifier = modifier
            )
        }
        is PaymentStatus.Failure -> {
            CheckoutFailureContent(
                errorMessage = status.message,
                canRetry = status.canRetry,
                onRetryClick = { onAction(CheckoutAction.RetryPaymentClicked) },
                onChangePaymentMethodClick = { onAction(CheckoutAction.ChangePaymentMethodClicked) },
                onBackToSeatMapClick = { onAction(CheckoutAction.BackClicked) },
                modifier = modifier
            )
        }
        is PaymentStatus.Idle, is PaymentStatus.Processing -> {
            CheckoutMainContent(
                state = state,
                onAction = onAction,
                snackbarHostState = snackbarHostState,
                modifier = modifier
            )
        }
    }

    if (state.isPaymentSheetVisible) {
        PaymentMethodBottomSheet(
            selectedMethod = state.selectedPaymentMethod,
            onMethodSelected = { onAction(CheckoutAction.PaymentMethodSelected(it)) },
            onDismissRequest = { onAction(CheckoutAction.DismissPaymentMethodSheet) }
        )
    }
}

@Composable
private fun CheckoutMainContent(
    state: CheckoutState,
    onAction: (CheckoutAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    fun formatRupiah(amount: Long): String {
        return "Rp ${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
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
                            .background(MovieTixColors.CinemaSurfaceSubtle)
                            .clickable(
                                enabled = !state.isProcessing,
                                role = Role.Button,
                                onClick = { onAction(CheckoutAction.BackClicked) }
                            )
                            .semantics {
                                contentDescription = "Kembali ke denah kursi"
                                role = Role.Button
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            color = if (state.isProcessing) Color.Gray else Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Konfirmasi Pesanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MovieTixColors.CinemaSurfaceCard,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MovieTixColors.CinemaTextSecondary
                        )
                        Text(
                            text = formatRupiah(state.order.totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MovieTixColors.CinemaPrimary
                        )
                    }

                    Button(
                        onClick = { onAction(CheckoutAction.PayClicked) },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MovieTixColors.CinemaPrimary,
                            contentColor = MovieTixColors.CinemaOnPrimary,
                            disabledContainerColor = MovieTixColors.CinemaPrimary.copy(alpha = 0.5f),
                            disabledContentColor = MovieTixColors.CinemaOnPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .width(170.dp)
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MovieTixColors.CinemaOnPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memproses...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Bayar Sekarang",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Card 1: Ringkasan Film & Jadwal
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MovieTixColors.CinemaSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DETAIL TIKET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val posterUrl = state.order.posterUrl
                        if (!posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = posterUrl,
                                contentDescription = "Poster ${state.order.movieTitle}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 60.dp, height = 85.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.order.movieTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.order.cinemaName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MovieTixColors.CinemaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MovieTixColors.CinemaPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${state.order.studioName} • ${state.order.studioType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MovieTixColors.CinemaPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem(label = "Waktu Tayang", value = "${state.order.date} ${state.order.showtime}")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem(label = "Nomor Kursi", value = state.order.selectedSeats.joinToString(", "))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Metode Pembayaran
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MovieTixColors.CinemaSurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !state.isProcessing,
                        onClick = { onAction(CheckoutAction.PaymentMethodClicked) }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "METODE PEMBAYARAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MovieTixColors.CinemaPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.selectedPaymentMethod.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MovieTixColors.CinemaSurfaceSubtle)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ubah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MovieTixColors.CinemaPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Rincian Pembayaran
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MovieTixColors.CinemaSurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RINCIAN BIAYA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MovieTixColors.CinemaPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val seatCount = state.order.selectedSeats.size
                    DetailItem(
                        label = "Tiket ($seatCount x ${formatRupiah(state.order.pricePerSeat)})",
                        value = formatRupiah(state.order.subtotal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem(
                        label = "Biaya Layanan",
                        value = formatRupiah(state.order.serviceFee)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MovieTixColors.CinemaSurfaceBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem(
                        label = "Total Biaya",
                        value = formatRupiah(state.order.totalPrice),
                        isHighlight = true
                    )
                }
            }

            // Card 4: Debug Toggle (Only visible in debug mode)
            if (state.isDebug) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MovieTixColors.CinemaSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MovieTixColors.CinemaSurfaceBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulasi Pembayaran Gagal (Debug)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MovieTixColors.StateErrorDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Memicu penolakan gateway untuk menguji rollback kursi otomatis.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MovieTixColors.CinemaTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = state.isDebugSimulateFailure,
                            onCheckedChange = { onAction(CheckoutAction.DebugSimulateFailureToggled(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MovieTixColors.StateErrorDark,
                                checkedTrackColor = MovieTixColors.StateErrorContainerDark,
                                uncheckedThumbColor = MovieTixColors.CinemaTextTertiary,
                                uncheckedTrackColor = MovieTixColors.CinemaSurfaceBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailItem(
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MovieTixColors.CinemaPrimary else Color.White
        )
    }
}
