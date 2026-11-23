package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.TimeRange
import com.colgateTotal77.tracker.core.enums.label
import com.colgateTotal77.tracker.core.enums.yearLabel
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun DateRangePickerModal(
    timeRange: TimeRange,
    onConfirm: (TimeRange) -> Unit,
    onDismiss: () -> Unit,
) {
    val dimensions = LocalDimensions.current

    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = timeRange.start?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = timeRange.end?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    )

    val start = state.selectedStartDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
    val end = state.selectedEndDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    val pickedRange = TimeRange(start, end ?: start)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            ) {
                CustomButton(
                    onClick = onDismiss,
                    buttonText = stringResource(R.string.cancel)
                )
                CustomButton(
                    onClick = {
                        onConfirm(TimeRange(start, end ?: start))
                        onDismiss()
                    },
                    buttonText = stringResource(R.string.apply),
                )
            }
        },
        dismissButton = null,
        modifier = Modifier.padding(horizontal = dimensions.screenHorizontalPadding),
        shape = RoundedCornerShape(LocalDimensions.current.cornerRadius)
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
            showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            title = {
                DateRangePickerDefaults.DateRangePickerTitle(
                    displayMode = state.displayMode,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                )
            },
            headline = {
                Column(Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)) {
                    Text(pickedRange.label(), style = MaterialTheme.typography.titleLarge, maxLines = 1)
                    pickedRange.yearLabel()?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
        )
    }
}