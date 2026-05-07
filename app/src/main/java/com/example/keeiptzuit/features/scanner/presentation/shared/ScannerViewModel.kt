package com.example.keeiptzuit.features.scanner.presentation.shared

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.example.keeiptzuit.features.scanner.utils.ImageManipulationUtils
import com.example.keeiptzuit.features.scanner.utils.toMat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    fun processIntent(intent: ScannerIntent) {
        when (intent) {
            is ScannerIntent.SurfaceRequestUpdate -> {
                _state.update { it.copy(surfaceRequest = intent.request) }
            }
            is ScannerIntent.AnalyzeImage -> analyzeImage(intent.image)
            is ScannerIntent.CaptureImage -> captureImage(intent.image)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val mediaImage = imageProxy.image ?: return
            val currentSize = if (rotation == 90 || rotation == 270) {
                Size(imageProxy.height, imageProxy.width)
            } else {
                Size(imageProxy.width, imageProxy.height)
            }
            val mat = imageProxy.toMat(mediaImage) ?: return
            val rotatedMat = Mat()

            when (rotation) {
                90 -> Core.rotate(mat, rotatedMat, Core.ROTATE_90_CLOCKWISE)
                180 -> Core.rotate(mat, rotatedMat, Core.ROTATE_180)
                270 -> Core.rotate(mat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
                else -> mat.copyTo(rotatedMat)
            }
            val detectedPoints = ImageManipulationUtils.detectEdges(rotatedMat)
            _state.update { currentState ->
                currentState.copy(
                    points = detectedPoints,
                    imageSize = currentSize
                )
            }
            mat.release()
            rotatedMat.release()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Detection Error: ${e.localizedMessage}") }
        } finally {
            imageProxy.close()
        }
    }

    private fun captureImage(imageProxy: ImageProxy) {
        try {
            var bitmap = imageProxy.toBitmap();
            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            val scaleX = bitmap.width.toFloat() / _state.value.imageSize!!.width
            val scaleY = bitmap.height.toFloat() / _state.value.imageSize!!.height

            val scaledPoints = _state.value.points!!.map { p ->
                Point(p.x * scaleX, p.y * scaleY)
            }
            val finalReceipt =
                ImageManipulationUtils.warpPerspective(bitmap, scaledPoints)

            if (finalReceipt != null) {
                _state.update { it.copy(scannedBitmap = finalReceipt) }
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Capture Error: ${e.localizedMessage}") }
        } finally {
            imageProxy.close()
        }
    }

}