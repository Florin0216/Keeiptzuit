package com.example.keeiptzuit.features.scanner.presentation.shared

import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.opencv.core.Point
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scannedBitmap: StateFlow<Bitmap?> = _scannedBitmap

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap

    private val _extractedText = MutableStateFlow<String?>(null)
    val extractedText: StateFlow<String?> = _extractedText

    private val _points = MutableStateFlow<List<Point>?>(null)
    val points: StateFlow<List<Point>?> = _points

    private val _imageSize = MutableStateFlow<Size?>(null)
    val imageSize: StateFlow<Size?> = _imageSize

    fun setBitmap(bitmap: Bitmap?) {
        _scannedBitmap.value = bitmap
    }

    fun setOriginalBitmap(bitmap: Bitmap?) {
        _originalBitmap.value = bitmap
    }

    fun setPoints(points: List<Point>?) {
        _points.value = points
    }

    fun setImageSize(size: Size?) {
        _imageSize.value = size
    }

    fun setExtractedText(text: String?) {
        _extractedText.value = text
    }

    fun clearBitmap() {
        _scannedBitmap.value = null
    }

    fun clearExtractedText() {
        _extractedText.value = null
    }

    fun clearPoints() {
        _points.value = null
    }

    fun clearImageSize() {
        _imageSize.value = null
    }

}