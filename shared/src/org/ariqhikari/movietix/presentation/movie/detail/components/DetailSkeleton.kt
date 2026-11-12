package org.ariqhikari.movietix.presentation.movie.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ariqhikari.movietix.core.presentation.components.shimmerPlaceholder

@Composable
fun DetailSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Backdrop skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .shimmerPlaceholder(RoundedCornerShape(0.dp))
        )

        // Overlapping poster & metadata
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .aspectRatio(2f / 3f)
                    .shimmerPlaceholder(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(24.dp)
                        .shimmerPlaceholder(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .shimmerPlaceholder(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .shimmerPlaceholder(RoundedCornerShape(4.dp))
                )
            }
        }

        // Synopsis skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .shimmerPlaceholder(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmerPlaceholder(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .shimmerPlaceholder(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .shimmerPlaceholder(RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cast skeleton row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .shimmerPlaceholder(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shimmerPlaceholder(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(10.dp)
                                .shimmerPlaceholder(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
