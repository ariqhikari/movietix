package org.ariqhikari.movietix.core.presentation.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.core.presentation.theme.Dimensions
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.MovieTixTheme
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing

/**
 * [SEMENTARA - FASE 0 ONLY]
 * Screen placeholder untuk memverifikasi token warna, font Outfit & Plus Jakarta Sans,
 * serta komponen dasar di light dan dark theme.
 * Screen ini akan diganti oleh HomeScreen pada Fase 1.
 */
@Composable
fun ThemePreviewPlaceholder() {
    var isDark by remember { mutableStateOf(true) }

    MovieTixTheme(darkTheme = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md)
        ) {
            // Header & Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOVIETIX",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDark) "Dark" else "Light",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.size(Spacing.xs))
                    Switch(
                        checked = isDark,
                        onCheckedChange = { isDark = it }
                    )
                }
            }

            Text(
                text = "Tema & Desain Sistem — Fase 0",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Typo Scale Showcase
            CardContainer(title = "Skala Tipografi (Outfit & Plus Jakarta Sans)") {
                Text(
                    text = "Display Large — Sinema",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Display Medium — Premiere",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Headline Medium — Grand Cinema XXI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Title Medium — Studio 1 (IMAX 2D)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Body Large — Paul Atreides bersatu kembali dengan suku Fremen...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Body Medium — Kamis, 13 November 2026 • 15:30 WIB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Label Mono — KODE: MTX-948201 • KURSI: C5, C6",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Seat Status Showcase
            CardContainer(title = "Peta Kursi Multi-Modal") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SeatSample(
                        label = "C4",
                        statusText = "Tersedia",
                        bg = MovieTixColors.CinemaSeatAvailable,
                        border = MovieTixColors.CinemaSurfaceBorder,
                        glyph = null
                    )
                    SeatSample(
                        label = "C5",
                        statusText = "Dipilih",
                        bg = MovieTixColors.CinemaSeatSelected,
                        border = MovieTixColors.CinemaPrimary,
                        glyph = "v"
                    )
                    SeatSample(
                        label = "C6",
                        statusText = "Terisi",
                        bg = MovieTixColors.CinemaSeatOccupied,
                        border = MovieTixColors.CinemaSurfaceBorder,
                        glyph = "X"
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // CTA Button Showcase
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.minTouchTarget),
                shape = RoundedCornerShape(Radius.pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "PILIH JADWAL & KURSI",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun CardContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = Dimensions.strokeWidth,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(Radius.lg)
            )
            .padding(Spacing.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        content()
    }
}

@Composable
private fun SeatSample(
    label: String,
    statusText: String,
    bg: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glyph: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(Dimensions.seatBoxSize)
                .clip(RoundedCornerShape(Radius.sm))
                .background(bg)
                .border(Dimensions.seatStrokeWidth, border, RoundedCornerShape(Radius.sm)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = glyph ?: label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (glyph == "v") MovieTixColors.CinemaOnPrimary else MovieTixColors.CinemaTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MovieTixColors.CinemaTextSecondary
        )
    }
}
