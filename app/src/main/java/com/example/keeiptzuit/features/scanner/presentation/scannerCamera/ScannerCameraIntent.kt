package com.example.keeiptzuit.features.scanner.presentation.scannerCamera

import android.content.Context
import androidx.lifecycle.LifecycleOwner

sealed class ScannerCameraIntent {
    data class BindCamera(val context: Context, val lifecycleOwner: LifecycleOwner) : ScannerCameraIntent()
}