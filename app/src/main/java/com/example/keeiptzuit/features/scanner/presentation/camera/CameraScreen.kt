package com.example.keeiptzuit.features.scanner.presentation.camera

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keeiptzuit.features.scanner.presentation.shared.SharedViewModel
import com.example.keeiptzuit.features.scanner.utils.ImageManipulationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import org.opencv.core.Point
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val coordinateTransformer = remember { MutableCoordinateTransformer() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val points by sharedViewModel.points.collectAsStateWithLifecycle()
    val imageSize by sharedViewModel.imageSize.collectAsStateWithLifecycle()
    val scannedBitmap by sharedViewModel.scannedBitmap.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                sharedViewModel.clearPoints()
                val size = Size(bitmap.width, bitmap.height)
                sharedViewModel.setImageSize(size)
                sharedViewModel.setOriginalBitmap(bitmap)
                sharedViewModel.setBitmap(bitmap)
            } else {
                val bitmap = context.contentResolver.openInputStream(uri).use { stream ->
                    Bitmap.createBitmap(BitmapFactory.decodeStream(stream))
                }
                sharedViewModel.clearPoints()
                val size = Size(bitmap.width, bitmap.height)
                sharedViewModel.setImageSize(size)
                sharedViewModel.setOriginalBitmap(bitmap)
                sharedViewModel.setBitmap(bitmap)
            }

        }
    }
    var autofocusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }
    val autofocusRequestId = autofocusRequest.first
    val showAutofocusIndicator = autofocusRequest.second.isSpecified
    val autofocusCoordinates = remember(autofocusRequestId) { autofocusRequest.second }

    if (showAutofocusIndicator) {
        LaunchedEffect(autofocusRequestId) {
            delay(1000)
            autofocusRequest = autofocusRequestId to Offset.Unspecified
        }
    }
    if (!cameraPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
        Text(text = "Camera permission is required to scan receipts.")
        return
    }
    LaunchedEffect(scannedBitmap) {
        if (scannedBitmap != null) {
            onNavigateToResult()
        }
    }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                viewModel.setSurfaceRequest(request)
                surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    request.resolution.width.toFloat(),
                    request.resolution.height.toFloat()
                )
            }
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    viewModel.analyzeImage(imageProxy) { result ->
                        sharedViewModel.setPoints(result.points)
                        sharedViewModel.setImageSize(result.size)
                    }
                }
            }

        provider.unbindAll()
        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer,
            imageCapture
        )

        cameraControl = camera.cameraControl
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row( modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                surfaceRequest?.let { request ->
                    CameraXViewfinder(
                        surfaceRequest = request,
                        coordinateTransformer = coordinateTransformer,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { tapCoordinates ->
                                    val point = surfaceMeteringPointFactory?.createPoint(
                                        tapCoordinates.x,
                                        tapCoordinates.y
                                    )
                                    if (point != null) {
                                        val meteringAction = FocusMeteringAction.Builder(point).build()
                                        cameraControl?.startFocusAndMetering(meteringAction)
                                    }
                                    autofocusRequest = UUID.randomUUID() to tapCoordinates
                                }
                            }
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAutofocusIndicator,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .offset {
                                autofocusCoordinates
                                    .takeOrElse { Offset.Zero }
                                    .round()
                            }
                            .offset((-24).dp, (-24).dp)
                    ) {
                        Spacer(
                            Modifier
                                .border(2.dp, Color.White, CircleShape)
                                .size(48.dp)
                        )
                    }
                }

                GuideOverlay(
                    points = points,
                    sourceSize = imageSize
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 24.dp, horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val rotated = viewModel.rotateImage(image)
                                        sharedViewModel.setOriginalBitmap(rotated)
                                        val scannedResult = viewModel.processImage(rotated, points, imageSize)
                                        sharedViewModel.setBitmap(scannedResult ?: rotated)
                                        image.close()
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White
                        )
                    }
                }
            }
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