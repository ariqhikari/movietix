package org.ariqhikari.movietix.presentation.cinema.seatmap.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.model.SeatStatus

@Composable
fun SeatMapGrid(
    seatLayout: SeatLayout,
    selectedSeats: List<String>,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            seatLayout.rows.forEach { rowLetter ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Row Label
                    RowHeader(rowLetter)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Left Column Group (1..4)
                    (1..seatLayout.aisleAfterColumn).forEach { colNumber ->
                        val seatId = "$rowLetter$colNumber"
                        val baseSeat = seatLayout.seats.find { it.id == seatId }
                        val isOccupied = baseSeat?.status == SeatStatus.OCCUPIED
                        val isSelected = seatId in selectedSeats

                        SeatCell(
                            seatId = seatId,
                            colNumber = colNumber,
                            isOccupied = isOccupied,
                            isSelected = isSelected,
                            onSeatClick = {
                                if (!isOccupied) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSeatClick(seatId)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Aisle Gap
                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Column Group (5..10)
                    ((seatLayout.aisleAfterColumn + 1)..seatLayout.totalColumns).forEach { colNumber ->
                        val seatId = "$rowLetter$colNumber"
                        val baseSeat = seatLayout.seats.find { it.id == seatId }
                        val isOccupied = baseSeat?.status == SeatStatus.OCCUPIED
                        val isSelected = seatId in selectedSeats

                        SeatCell(
                            seatId = seatId,
                            colNumber = colNumber,
                            isOccupied = isOccupied,
                            isSelected = isSelected,
                            onSeatClick = {
                                if (!isOccupied) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSeatClick(seatId)
                                }
                            }
                        )
                        if (colNumber < seatLayout.totalColumns) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Row Label
                    RowHeader(rowLetter)
                }
            }
        }
    }
}

@Composable
private fun RowHeader(
    letter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MovieTixColors.CinemaTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SeatCell(
    seatId: String,
    colNumber: Int,
    isOccupied: Boolean,
    isSelected: Boolean,
    onSeatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    val (backgroundColor, borderColor, textColor, symbol) = when {
        isOccupied -> Quadruple(MovieTixColors.CinemaSeatOccupied, MovieTixColors.CinemaSurfaceBorder, MovieTixColors.CinemaTextTertiary, "✕")
        isSelected -> Quadruple(MovieTixColors.CinemaSeatSelected, null, MovieTixColors.CinemaOnPrimary, "✓")
        else -> Quadruple(MovieTixColors.CinemaSeatAvailable, MovieTixColors.CinemaSeatBorder, MovieTixColors.CinemaTextSecondary, "$colNumber")
    }

    val statusDesc = when {
        isOccupied -> "Terisi"
        isSelected -> "Terpilih"
        else -> "Tersedia"
    }

    val cellModifier = modifier
        .size(30.dp)
        .scale(scale)
        .clip(RoundedCornerShape(6.dp))
        .background(backgroundColor)
        .let { base ->
            if (borderColor != null) {
                base.border(1.dp, borderColor, RoundedCornerShape(6.dp))
            } else base
        }
        .semantics {
            role = Role.Checkbox
            selected = isSelected
            contentDescription = "Kursi $seatId, $statusDesc"
        }
        .clickable(enabled = !isOccupied, onClick = onSeatClick)

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = textColor,
            fontSize = if (isSelected || isOccupied) 11.sp else 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
