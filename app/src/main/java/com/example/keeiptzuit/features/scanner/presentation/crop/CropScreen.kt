package com.example.keeiptzuit.features.scanner.presentation.crop

import android.util.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keeiptzuit.features.scanner.presentation.camera.CameraViewModel
import com.example.keeiptzuit.features.scanner.presentation.shared.SharedViewModel
import com.example.keeiptzuit.features.scanner.utils.ImageManipulationUtils
import org.opencv.core.Point
import kotlin.math.roundToInt

@Composable
fun CropScreen(
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel
) {
    val originalBitmap by sharedViewModel.originalBitmap.collectAsStateWithLifecycle()
    val points by sharedViewModel.points.collectAsStateWithLifecycle()
    val imageSize by sharedViewModel.imageSize.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IconButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            originalBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF2F3F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Original photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    DraggableGuideOverlay(
                        points = points,
                        sourceSize = imageSize ?: Size(bitmap.width, bitmap.height),
                        onPointsChanged = { updatedPoints ->
                            sharedViewModel.setPoints(updatedPoints)
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val currentPoints = sharedViewModel.points.value
                                val currentImageSize = sharedViewModel.imageSize.value
                                val scanned =
                                    viewModel.processImage(bitmap, currentPoints, currentImageSize)
                                sharedViewModel.setBitmap(scanned ?: bitmap)
                                onNavigateBack()
                            }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Confirm",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text("Confirm", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableGuideOverlay(
    points: List<Point>?,
    sourceSize: Size,
    modifier: Modifier = Modifier,
    onPointsChanged: ((List<Point>) -> Unit)? = null
) {
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var corners by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(points, sourceSize, canvasSize) {
        if (canvasSize.width == 0f || canvasSize.height == 0f) return@LaunchedEffect

        val imageWidth = sourceSize.width.toFloat()
        val imageHeight = sourceSize.height.toFloat()
        val scale = minOf(canvasSize.width / imageWidth, canvasSize.height / imageHeight)
        val offsetX = (canvasSize.width - imageWidth * scale) / 2f
        val offsetY = (canvasSize.height - imageHeight * scale) / 2f

        corners = if (points != null && points.size >= 4) {
            val sorted = ImageManipulationUtils.sortCornersTLTRBRBL(points)
            sorted.map { p ->
                Offset(p.x.toFloat() * scale + offsetX, p.y.toFloat() * scale + offsetY)
            }
        } else {
            listOf(
                Offset(offsetX, offsetY),
                Offset(offsetX + imageWidth * scale, offsetY),
                Offset(offsetX + imageWidth * scale, offsetY + imageHeight * scale),
                Offset(offsetX, offsetY + imageHeight * scale)
            )
        }
    }

    val density = LocalDensity.current
    val handleRadius = with(density) { 12.dp.toPx() }
    val handleSizeDp = 24.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = androidx.compose.ui.geometry.Size(
                    size.width.toFloat(),
                    size.height.toFloat()
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (corners.size < 4) return@Canvas

            val path = Path().apply {
                moveTo(corners[0].x, corners[0].y)
                corners.forEach { lineTo(it.x, it.y) }
                close()
            }

            drawPath(path, Color.Green.copy(alpha = 0.2f), style = Fill)
            drawPath(path, Color.Green, style = Stroke(width = 4f, cap = StrokeCap.Round))
        }

        if (corners.size == 4) {
            corners.forEachIndexed { index, corner ->
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (corner.x - handleRadius).roundToInt(),
                                (corner.y - handleRadius).roundToInt()
                            )
                        }
                        .size(handleSizeDp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .border(2.dp, Color(0xFF4CAF50), CircleShape)
                        .pointerInput(index) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newCorners = corners.toMutableList()
                                newCorners[index] = Offset(
                                    (corners[index].x + dragAmount.x)
                                        .coerceIn(0f, canvasSize.width),
                                    (corners[index].y + dragAmount.y)
                                        .coerceIn(0f, canvasSize.height)
                                )
                                corners = newCorners

                                onPointsChanged?.invoke(
                                    screenCornersToImagePoints(newCorners, sourceSize, canvasSize)
                                )
                            }
                        }
                )
            }
        }
    }
}

/**
 * Reverses the fit-scale + centering transform to convert screen-space handles
 * back into image-space [Point]s for processing/storage.
 */
private fun screenCornersToImagePoints(
    screenCorners: List<Offset>,
    sourceSize: Size,
    canvasSize: androidx.compose.ui.geometry.Size
): List<Point> {
    if (canvasSize.width == 0f || canvasSize.height == 0f) return emptyList()

    val scale = minOf(
        canvasSize.width / sourceSize.width.toFloat(),
        canvasSize.height / sourceSize.height.toFloat()
    )
    val offsetX = (canvasSize.width - sourceSize.width * scale) / 2f
    val offsetY = (canvasSize.height - sourceSize.height * scale) / 2f

    return screenCorners.map { offset ->
        Point(
            ((offset.x - offsetX) / scale).toDouble(),
            ((offset.y - offsetY) / scale).toDouble()
        )
    }
}