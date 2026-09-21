package com.colgateTotal77.tracker.screens.dashboard.Camera

import android.Manifest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.screens.dashboard.TransactionDraft
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Camera(
    onAdd: (TransactionDraft) -> Unit,
    onClose: () -> Unit,
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (permissionState.status.isGranted) {
        ScannerPreview(onAdd = onAdd, onClose = onClose)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Grant Camera Permission")
                }
                Button(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}