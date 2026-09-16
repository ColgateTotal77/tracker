package com.colgateTotal77.tracker.screens.dashboard

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "CameraScan"

/** Fraction of the frame (0f..1f) that the scan window occupies. */
private const val SCAN_WINDOW_FRACTION = 0.65f
/** Length of each corner bracket as a fraction of the scan window size. */
private const val CORNER_LENGTH_FRACTION = 0.18f

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Camera(
    onQRCodeFound: (String) -> Unit,
    onClose: () -> Unit,
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (permissionState.status.isGranted) {
        ScannerPreview(onQRCodeFound = onQRCodeFound, onClose = onClose)
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

@Composable
fun ScannerPreview(
    onQRCodeFound: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    // Fires only once per successful scan.
    var hasScanned by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

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
                .setTargetResolution(android.util.Size(1280, 720))
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                if (!hasScanned) {
                    processImage(imageProxy, scanner) { value ->
                        // Prevent duplicate fires from subsequent frames.
                        if (!hasScanned) {
                            hasScanned = true
                            onQRCodeFound(value)
                        }
                    }
                } else {
                    imageProxy.close()
                }
            }

            try {
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
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

        // Corner brackets showing where to place the QR code.
        ScanCornersOverlay(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        )

        Text(
            "Point at a QR code",
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Torch toggle
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

/**
 * Draws four corner brackets around a centered square scan window,
 * plus a subtle dim outside it, so the user knows where to place the code.
 */
@Composable
fun ScanCornersOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val side = minOf(size.width, size.height) * SCAN_WINDOW_FRACTION
        val windowSize = Size(side, side)
        val topLeft = Offset(
            (size.width - side) / 2f,
            (size.height - side) / 2f
        )
        val rect = Rect(topLeft, windowSize)
        val cornerLen = side * CORNER_LENGTH_FRACTION

        val stroke = Stroke(
            width = 4.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val accentColor = Color.White

        val paths = listOf(
            // top-left
            Path().apply {
                moveTo(rect.left, rect.top + cornerLen)
                lineTo(rect.left, rect.top)
                lineTo(rect.left + cornerLen, rect.top)
            },
            // top-right
            Path().apply {
                moveTo(rect.right - cornerLen, rect.top)
                lineTo(rect.right, rect.top)
                lineTo(rect.right, rect.top + cornerLen)
            },
            // bottom-right
            Path().apply {
                moveTo(rect.right, rect.bottom - cornerLen)
                lineTo(rect.right, rect.bottom)
                lineTo(rect.right - cornerLen, rect.bottom)
            },
            // bottom-left
            Path().apply {
                moveTo(rect.left + cornerLen, rect.bottom)
                lineTo(rect.left, rect.bottom)
                lineTo(rect.left, rect.bottom - cornerLen)
            },
        )
        paths.forEach { drawPath(it, color = accentColor, style = stroke) }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQRCodeFound: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                val value = barcode.rawValue
                if (value != null) {
                    Log.d(TAG, "QR found: $value")
                    onQRCodeFound(value)
                    break
                }
            }
        }
        .addOnFailureListener { e -> Log.e(TAG, "scan error", e) }
        .addOnCompleteListener { imageProxy.close() }
}
