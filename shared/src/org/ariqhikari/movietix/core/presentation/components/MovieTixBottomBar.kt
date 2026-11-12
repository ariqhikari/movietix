package org.ariqhikari.movietix.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ariqhikari.movietix.core.presentation.theme.MovieTixColors
import org.ariqhikari.movietix.core.presentation.theme.Radius
import org.ariqhikari.movietix.core.presentation.theme.Spacing

enum class BottomNavTab {
    FILM,
    TIKET_SAYA
}

/**
 * Bottom navigation bar for top-level app destinations:
 * - [BottomNavTab.FILM]: Home movie catalog
 * - [BottomNavTab.TIKET_SAYA]: User's purchased e-tickets history
 */
@Composable
fun MovieTixBottomBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MovieTixColors.CinemaSurfaceCard,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MovieTixColors.CinemaSurfaceBorder
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "Film",
                glyph = "▣",
                isSelected = currentTab == BottomNavTab.FILM,
                onClick = { onTabSelected(BottomNavTab.FILM) },
                modifier = Modifier.weight(1f)
            )

            BottomBarItem(
                label = "Tiket Saya",
                glyph = "▤",
                isSelected = currentTab == BottomNavTab.TIKET_SAYA,
                onClick = { onTabSelected(BottomNavTab.TIKET_SAYA) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    glyph: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MovieTixColors.CinemaPrimary
    val inactiveColor = MovieTixColors.CinemaTextSecondary
    val contentColor = if (isSelected) activeColor else inactiveColor
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(Radius.md))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = activeColor.copy(alpha = 0.2f)),
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = isSelected
                this.role = Role.Tab
                this.contentDescription = "$label, ${if (isSelected) "terpilih" else "tidak terpilih"}"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = glyph,
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
