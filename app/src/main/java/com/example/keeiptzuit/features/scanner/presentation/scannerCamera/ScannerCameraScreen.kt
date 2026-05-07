package com.example.keeiptzuit.features.scanner.presentation.scannerCamera

import android.Manifest
import android.util.Size
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.keeiptzuit.features.scanner.presentation.shared.ScannerIntent
import com.example.keeiptzuit.features.scanner.presentation.shared.ScannerViewModel
import com.example.keeiptzuit.features.scanner.utils.ImageManipulationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.opencv.core.Point
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerCameraScreen(
    onNavigateToResult: () -> Unit,
    viewModel: ScannerViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    if (!cameraPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
        Text(text = "Camera permission is required to scan receipts.")
        return
    }
    LaunchedEffect(state.scannedBitmap) {
        if (state.scannedBitmap != null) {
            onNavigateToResult()
        }
    }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                viewModel.processIntent(ScannerIntent.SurfaceRequestUpdate(request))
            }
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    viewModel.processIntent(ScannerIntent.AnalyzeImage(imageProxy))
                }
            }

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer,
            imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        state.surfaceRequest?.let { request ->
            CameraXViewfinder(surfaceRequest = request)
        }
        GuideOverlay(
            points = state.points,
            sourceSize = state.imageSize
        )
        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .padding(horizontal = 48.dp)
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    imageCapture.takePicture(ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                viewModel.processIntent(ScannerIntent.CaptureImage(image))
                            }
                        })
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
            ) {}
        }
    }
}

@Composable
fun GuideOverlay(
    points: List<Point>?,
    sourceSize: Size?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points == null || points.size < 4 || sourceSize == null) return@Canvas

        val sortedPoints = ImageManipulationUtils.sortCornersTLTRBRBL(points)

        val imageWidth = sourceSize.width.toFloat()
        val imageHeight = sourceSize.height.toFloat()

        val scale = maxOf(size.width / imageWidth, size.height / imageHeight)

        val offsetX = (size.width - imageWidth * scale) / 2f
        val offsetY = (size.height - imageHeight * scale) / 2f

        val offsets = sortedPoints.map { p ->
            Offset(
                (p.x.toFloat() * scale) + offsetX,
                (p.y.toFloat() * scale) + offsetY
            )
        }

        val path = Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            offsets.forEach { lineTo(it.x, it.y) }
            close()
        }

        drawPath(
            path = path,
            color = Color.Green.copy(alpha = 0.2f),
            style = Fill
        )

        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}