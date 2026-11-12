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
            backgroundColor = Color(0xFF1E2026),
            borderColor = Color(0xFF4A4D54),
            textColor = Color(0xFFD1D5DB)
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Selected
        LegendItem(
            label = "Dipilih",
            symbol = "✓",
            backgroundColor = MaterialTheme.colorScheme.primary,
            borderColor = null,
            textColor = Color(0xFF0D0D11)
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Occupied
        LegendItem(
            label = "Terisi",
            symbol = "✕",
            backgroundColor = Color(0xFF141519),
            borderColor = Color(0xFF282A30),
            textColor = Color(0xFF6B7280)
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
            color = Color(0xFFB0B4C0),
            fontSize = 12.sp
        )
    }
}
