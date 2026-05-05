package com.example.keeiptzuit.features.scanner.presentation.scannerCamera

import android.content.Context
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerCameraViewModel @Inject constructor(): ViewModel() {
    private val _state = MutableStateFlow(ScannerCameraState())
    val state = _state

    fun processIntent(intent: ScannerCameraIntent) {
        when (intent) {
            is ScannerCameraIntent.BindCamera -> {
                handleBindCamera(intent.context, intent.lifecycleOwner)
            }
        }
    }

    private fun handleBindCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val provider = ProcessCameraProvider.awaitInstance(context)
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider { request ->
                        _state.update { it.copy(surfaceRequest = request, isLoading = false) }
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner, DEFAULT_BACK_CAMERA, preview,
                )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }

    }
}