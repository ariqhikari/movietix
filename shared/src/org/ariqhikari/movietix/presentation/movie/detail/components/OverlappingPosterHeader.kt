package org.ariqhikari.movietix.presentation.movie.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ariqhikari.movietix.domain.movie.model.MovieDetail

@Composable
fun OverlappingPosterHeader(
    movie: MovieDetail,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-40).dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Poster Card
        Card(
            modifier = Modifier
                .width(116.dp)
                .aspectRatio(2f / 3f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = "Poster film ${movie.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Metadata column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (movie.tagline.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = movie.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating & Vote count
            if (movie.voteAverage > 0.0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val ratingStr = ((movie.voteAverage * 10).toInt() / 10.0).toString()
                    Text(
                        text = "$ratingStr/10",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (movie.voteCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${formatVoteCount(movie.voteCount)} ulasan)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Duration & Release Year
            val runtimeStr = formatRuntime(movie.runtime)
            val releaseYear = movie.releaseDate.take(4).ifBlank { "-" }
            Text(
                text = "$releaseYear • $runtimeStr",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            if (movie.genres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    movie.genres.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatRuntime(minutes: Int): String {
    if (minutes <= 0) return "-"
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours > 0 && remaining > 0 -> "${hours}j ${remaining}m"
        hours > 0 -> "${hours}j"
        else -> "${remaining}m"
    }
}

private fun formatVoteCount(count: Int): String {
    return when {
        count >= 1000 -> "${((count / 100).toDouble() / 10)}k"
        else -> count.toString()
    }
}
