package org.ariqhikari.movietix.presentation.movie.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun BackdropHeroHeader(
    backdropUrl: String?,
    posterFallbackUrl: String?,
    movieTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = backdropUrl?.ifBlank { null } ?: posterFallbackUrl

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Backdrop film $movieTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient scrim from bottom to top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.surface
                        ),
                        startY = 0f
                    )
                )
        )

        // Floating Back Button
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .clickable(role = Role.Button, onClick = onBackClick)
                .semantics {
                    contentDescription = "Kembali ke beranda"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
