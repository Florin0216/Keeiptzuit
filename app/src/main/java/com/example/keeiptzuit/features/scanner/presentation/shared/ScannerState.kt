package com.example.keeiptzuit.features.scanner.presentation.shared

import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.SurfaceRequest
import org.opencv.core.Point

data class ScannerState(
    val error: String? = null,
    val surfaceRequest: SurfaceRequest? = null,
    val points: List<Point>? = null,
    val imageSize: Size? = null,
    val imageCapture: ImageCapture? = null,
    val imageAnalysis: ImageAnalysis? = null,
    val scannedBitmap: Bitmap? = null
)