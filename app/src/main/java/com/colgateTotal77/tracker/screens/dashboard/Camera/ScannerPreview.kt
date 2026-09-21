package com.colgateTotal77.tracker.screens.dashboard.Camera

import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.TaxApi.fetchFiscalCheck
import com.colgateTotal77.tracker.screens.dashboard.TransactionDraft
import com.colgateTotal77.tracker.screens.dashboard.toDraft
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors

private const val TAG = "Camera"

@Composable
fun ScannerPreview(
    onAdd: (TransactionDraft) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    var hasScanned by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val previewView = remember { PreviewView(context) }
    previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(1280, 720))
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                if (!hasScanned) {
                    scanProcessImage(imageProxy, scanner) { qrLink ->
                        if (hasScanned) return@scanProcessImage
                        hasScanned = true

                        coroutineScope.launch {
                            try {
                                val parsedCheck = fetchFiscalCheck(qrLink)
                                Log.d(TAG, "parsedCheck $parsedCheck")
                                onAdd(parsedCheck.toDraft())
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to fetch fiscal check", e)
                                errorMessage = if (e is IOException) { //toast?
                                    "Network error loading check — check connection and try again"
                                } else {
                                    "Failed to load check: ${e.message}"
                                }
                                hasScanned = false
                            }
                        }
                    }
                } else imageProxy.close()
            }

            try {
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )

                // Receipt QR codes are tiny — zoom in by default so they fill the frame.
                // zoomState may not be ready at bind time, so observe it.
                var zoomApplied = false
                camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
                    if (!zoomApplied && state.maxZoomRatio > 1f) {
                        zoomApplied = true
                        camera?.cameraControl?.setZoomRatio(minOf(state.maxZoomRatio * 0.5f, 2f))
                    }
                }
                Log.d(TAG, "Camera bound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "bindToLifecycle failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            executor.shutdown()
            scanner.close()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        ScanCornersOverlay(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        )

        Text(
            errorMessage ?: "Point at a QR code",
            color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        IconButton(
            onClick = {
                isTorchOn = !isTorchOn
                camera?.cameraControl?.enableTorch(isTorchOn)
            },
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Toggle flashlight",
                tint = Color.White,
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}