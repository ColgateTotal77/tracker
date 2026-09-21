package com.colgateTotal77.tracker.screens.dashboard.Camera

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

private const val TAG = "Camera"

@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun scanProcessImage(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onQRLinkFound: (String) -> Unit,
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
                val qrLink = barcode.rawValue ?: return@addOnSuccessListener
                Log.d(TAG, "QR found: $qrLink")
                onQRLinkFound(qrLink)
                break
            }
        }
        .addOnFailureListener { e -> Log.e(TAG, "scan error", e) }
        .addOnCompleteListener { imageProxy.close() }
}
