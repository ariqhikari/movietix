package org.ariqhikari.movietix.presentation.cinema.schedule.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun HorizontalDatePicker(
    selectedDate: LocalDate,
    availableDates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(availableDates, key = { it.toString() }) { date ->
            val isSelected = date == selectedDate
            val dayName = date.dayOfWeek.toIndonesianAbbreviation()
            val dayNumber = date.dayOfMonth.toString()
            val monthName = date.month.toIndonesianAbbreviation()

            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFF1E2028)
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                Color(0xFFE4E5EB)
            }
            val border = if (isSelected) {
                null
            } else {
                BorderStroke(1.dp, Color(0xFF2E313D))
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 0.dp
                ),
                modifier = Modifier
                    .width(62.dp)
                    .height(80.dp)
                    .semantics {
                        role = Role.Button
                        selected = isSelected
                        contentDescription = "Tanggal $dayName, $dayNumber $monthName"
                    }
                    .clickable { onDateSelected(date) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) contentColor.copy(alpha = 0.85f) else Color(0xFF9EA3B0),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dayNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        fontSize = 20.sp
                    )
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) contentColor.copy(alpha = 0.85f) else Color(0xFF9EA3B0),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun DayOfWeek.toIndonesianAbbreviation(): String = when (this) {
    DayOfWeek.MONDAY -> "SEN"
    DayOfWeek.TUESDAY -> "SEL"
    DayOfWeek.WEDNESDAY -> "RAB"
    DayOfWeek.THURSDAY -> "KAM"
    DayOfWeek.FRIDAY -> "JUM"
    DayOfWeek.SATURDAY -> "SAB"
    DayOfWeek.SUNDAY -> "MIN"
}

private fun kotlinx.datetime.Month.toIndonesianAbbreviation(): String = when (this) {
    kotlinx.datetime.Month.JANUARY -> "Jan"
    kotlinx.datetime.Month.FEBRUARY -> "Feb"
    kotlinx.datetime.Month.MARCH -> "Mar"
    kotlinx.datetime.Month.APRIL -> "Apr"
    kotlinx.datetime.Month.MAY -> "Mei"
    kotlinx.datetime.Month.JUNE -> "Jun"
    kotlinx.datetime.Month.JULY -> "Jul"
    kotlinx.datetime.Month.AUGUST -> "Agu"
    kotlinx.datetime.Month.SEPTEMBER -> "Sep"
    kotlinx.datetime.Month.OCTOBER -> "Okt"
    kotlinx.datetime.Month.NOVEMBER -> "Nov"
    kotlinx.datetime.Month.DECEMBER -> "Des"
}
