package org.ariqhikari.movietix.presentation.cinema.seatmap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

@Composable
fun SeatMapLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Available
        LegendItem(
            label = "Tersedia",
            symbol = null,
            backgroundColor = MovieTixColors.CinemaSeatAvailable,
            borderColor = MovieTixColors.CinemaSeatBorder,
            textColor = MovieTixColors.CinemaTextSecondary
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Selected
        LegendItem(
            label = "Dipilih",
            symbol = "✓",
            backgroundColor = MovieTixColors.CinemaSeatSelected,
            borderColor = null,
            textColor = MovieTixColors.CinemaOnPrimary
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Occupied
        LegendItem(
            label = "Terisi",
            symbol = "✕",
            backgroundColor = MovieTixColors.CinemaSeatOccupied,
            borderColor = MovieTixColors.CinemaSurfaceBorder,
            textColor = MovieTixColors.CinemaTextTertiary
        )
    }
}

@Composable
private fun LegendItem(
    label: String,
    symbol: String?,
    backgroundColor: Color,
    borderColor: Color?,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val boxModifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .let { base ->
                if (borderColor != null) {
                    base.border(1.dp, borderColor, RoundedCornerShape(4.dp))
                } else base
            }

        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.Center
        ) {
            if (symbol != null) {
                Text(
                    text = symbol,
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MovieTixColors.CinemaTextSecondary,
            fontSize = 12.sp
        )
    }
}
