package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun OneActionModal(
    title: String,
    mainText: String,
    actionButtonText: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
) {
    val dimensions = LocalDimensions.current

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenHorizontalPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    mainText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    CustomButton(
                        onClick = onAction,
                        buttonText = actionButtonText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CustomButton(
                        onClick = onDismiss,
                        buttonText = stringResource(R.string.cancel),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}