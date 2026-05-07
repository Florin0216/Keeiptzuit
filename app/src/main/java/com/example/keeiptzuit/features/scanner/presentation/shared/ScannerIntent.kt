package com.example.keeiptzuit.features.scanner.presentation.shared

import androidx.camera.core.ImageProxy
import androidx.camera.core.SurfaceRequest

sealed class ScannerIntent {
    data class SurfaceRequestUpdate(val request: SurfaceRequest?) : ScannerIntent()
    data class AnalyzeImage(val image: ImageProxy) : ScannerIntent()
    data class CaptureImage(val image: ImageProxy) : ScannerIntent()
}