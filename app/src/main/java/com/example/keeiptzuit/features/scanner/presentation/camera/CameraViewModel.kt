package com.example.keeiptzuit.features.scanner.presentation.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.ViewModel
import com.example.keeiptzuit.features.scanner.utils.ImageManipulationUtils
import com.example.keeiptzuit.features.scanner.utils.toMat
import com.example.keeiptzuit.features.scanner.utils.ImageEnhancer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import org.opencv.android.Utils

data class DetectionResult(
    val points: List<Point>,
    val size: Size
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    fun setSurfaceRequest(request: SurfaceRequest?) {
        _surfaceRequest.value = request
    }

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy, onResult: (DetectionResult) -> Unit) {
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

            val gray = Mat()
            Imgproc.cvtColor(rotatedMat, gray, Imgproc.COLOR_BGR2GRAY)

            val blurred = Mat()
            Imgproc.GaussianBlur(gray, blurred, org.opencv.core.Size(9.0, 9.0), 0.0)

            val edges = Mat()
            Imgproc.Canny(blurred, edges, 50.0, 150.0)

            val detectedPoints = ImageManipulationUtils.detectEdges(rotatedMat)

            if (detectedPoints.isNotEmpty()) {
                onResult(DetectionResult(detectedPoints, currentSize))
            }

            mat.release()
            rotatedMat.release()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    fun rotateImage(imageProxy: ImageProxy): Bitmap {
        var bitmap = imageProxy.toBitmap()
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }

    fun processImage(bitmap: Bitmap, points: List<Point>?, size: Size?): Bitmap? {
        return try {
            val currentSize = size ?: return null
            val scaleX = bitmap.width.toFloat() / currentSize.width
            val scaleY = bitmap.height.toFloat() / currentSize.height

            val scaledPoints = points?.map { p ->
                Point(p.x * scaleX, p.y * scaleY)
            }

            val warped = ImageManipulationUtils.warpPerspective(bitmap, scaledPoints) ?: return null
            ImageEnhancer.enhanceImage(warped)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}