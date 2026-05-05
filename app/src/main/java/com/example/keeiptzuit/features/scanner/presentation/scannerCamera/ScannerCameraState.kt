package com.example.keeiptzuit.features.scanner.presentation.scannerCamera

import android.graphics.Bitmap
import android.graphics.Point
import android.util.Size
import androidx.camera.core.SurfaceRequest

data class ScannerCameraState(
    val surfaceRequest: SurfaceRequest? = null,
    val detectedPoints: List<Point>? = null,
    val imageSize: Size? = null,
    val scannedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
