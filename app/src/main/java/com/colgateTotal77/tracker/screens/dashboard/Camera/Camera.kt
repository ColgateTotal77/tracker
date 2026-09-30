package com.colgateTotal77.tracker.screens.dashboard.Camera

import android.Manifest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.core.database.transaction.TransactionDraft
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Camera(
    onAdd: (TransactionDraft) -> Unit,
    onClose: () -> Unit,
) {
    val dimensions = LocalDimensions.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (permissionState.status.isGranted) {
        ScannerPreview(onAdd = onAdd, onClose = onClose)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing),
            ) {
                CustomButton(
                    onClick = { permissionState.launchPermissionRequest() },
                    buttonText = "Grant Camera Permission",
                )
                CustomButton(
                    onClick = onClose,
                    buttonText = "Close",
                )
            }
        }
    }
}