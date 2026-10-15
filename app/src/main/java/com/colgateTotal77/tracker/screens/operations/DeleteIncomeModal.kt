package com.colgateTotal77.tracker.screens.operations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun DeleteIncomeModal(onDismiss: () -> Unit, onDelete: () -> Unit) {
    val dimensions = LocalDimensions.current

    Dialog(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(stringResource(R.string.delete_income), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.confirm_delete_income), style = MaterialTheme.typography.bodyMedium)
                CustomButton(
                    onClick = onDelete,
                    buttonText = stringResource(R.string.delete_income),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
