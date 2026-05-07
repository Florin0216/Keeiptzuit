package com.example.keeiptzuit.features.scanner.presentation.scannerResult

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.keeiptzuit.features.scanner.presentation.shared.ScannerViewModel
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp


@Composable
fun ScannerResultScreen(
    viewModel: ScannerViewModel
) {
    val state by viewModel.state.collectAsState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        state.scannedBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Scanned Receipt",
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            CircularProgressIndicator()
        }
    }
}