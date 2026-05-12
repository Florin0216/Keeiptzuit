package com.example.keeiptzuit.features.scanner.presentation.shared

import android.graphics.Bitmap
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scannedBitmap: StateFlow<Bitmap?> = _scannedBitmap

    private val _extractedText = MutableStateFlow<String?>(null)
    val extractedText: StateFlow<String?> = _extractedText

    fun setBitmap(bitmap: Bitmap?) {
        _scannedBitmap.value = bitmap
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

}