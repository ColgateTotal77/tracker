package com.colgateTotal77.tracker.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.TimeRange
import com.colgateTotal77.tracker.core.enums.label
import com.colgateTotal77.tracker.core.enums.move
import com.colgateTotal77.tracker.core.enums.yearLabel
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun SearchBar(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (onClose != null) focusRequester.requestFocus() }

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onValueChange,
        modifier = modifier.focusRequester(focusRequester),
        placeholder = { Text(placeholder) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
        ),
        leadingIcon = {
            CustomIconButton(onClick = { onClose?.invoke() }, enabled = onClose != null) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_accessibility),
                    tint = if (onClose != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
    )
}

@Composable
fun DateRangeSelector(
    timeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    var isDatePickerOpen by remember { mutableStateOf(false) }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        CustomIconButton(onClick = { onTimeRangeChange(timeRange.move(-1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = stringResource(R.string.previous_period))
        }

        TextButton(
            onClick = {isDatePickerOpen = !isDatePickerOpen},
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp),
            shape = RoundedCornerShape(dimensions.cornerRadius),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    text = timeRange.label(),
                    style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = LocalTextStyle.current.fontSize),
                )
                timeRange.yearLabel()?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        CustomIconButton(onClick = { onTimeRangeChange(timeRange.move(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = stringResource(R.string.next_period))
        }
    }

    if (isDatePickerOpen) {
        DateRangePickerModal(
            timeRange = timeRange,
            onConfirm = { onTimeRangeChange(it) },
            onDismiss = { isDatePickerOpen = false }
        )
    }
}

@Composable
fun Toolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    timeRange: TimeRange? = null,
    onTimeRangeChange: ((TimeRange) -> Unit)? = null,
    sortMenu: (@Composable () -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
) {
    val dimensions = LocalDimensions.current
    var isSearchOpen by remember { mutableStateOf(onTimeRangeChange == null && timeRange == null) }

    Row(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(dimensions.cornerRadius))
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                RoundedCornerShape(dimensions.cornerRadius)
            )
            .height(OutlinedTextFieldDefaults.MinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onTimeRangeChange == null && timeRange == null) {
            SearchBar(
                searchQuery,
                onSearchChange,
                placeholder,
                Modifier.weight(1f),
            )
        } else {
            val offset = with(LocalDensity.current) { 16.dp.roundToPx() }
            AnimatedContent(
                targetState = isSearchOpen,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val dir = if (targetState) -1 else 1
                    (fadeIn(tween(200, delayMillis = 150)) + slideInHorizontally(tween(200, delayMillis = 150)) { dir * offset }) togetherWith
                            (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -dir * offset })
                },
                label = "search",
            ) { open ->
                if (open) {
                    SearchBar(
                        searchQuery,
                        onSearchChange,
                        placeholder,
                        Modifier.fillMaxWidth(),
                        onClose = { isSearchOpen = false },
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomIconButton(onClick = { isSearchOpen = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_accessibility)
                            )
                        }
                        DateRangeSelector(timeRange!!, onTimeRangeChange!!, Modifier.weight(1f))
                    }
                }
            }
        }

        if (sortMenu != null) {
            VerticalDivider(modifier = Modifier.height(dimensions.verticalDividerHeight))
            sortMenu()
        }

        if (onFilterClick != null) {
            VerticalDivider(modifier = Modifier.height(dimensions.verticalDividerHeight))
            CustomIconButton(onClick = { onFilterClick() }) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = stringResource(R.string.filter),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
