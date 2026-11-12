package org.ariqhikari.movietix.presentation.movie.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.domain.movie.model.MovieCategory

@Composable
fun CategoryFilterChips(
    selectedCategory: MovieCategory,
    onCategorySelected: (MovieCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = MovieCategory.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { categoryItem ->
            val isSelected = categoryItem == selectedCategory
            val label = when (categoryItem) {
                MovieCategory.NOW_PLAYING -> "Sedang Tayang"
                MovieCategory.POPULAR -> "Populer"
                MovieCategory.UPCOMING -> "Akan Datang"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(categoryItem) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = null
            )
        }
    }
}
