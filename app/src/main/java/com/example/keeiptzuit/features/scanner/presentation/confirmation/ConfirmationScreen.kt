package com.example.keeiptzuit.features.scanner.presentation.confirmation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keeiptzuit.features.scanner.presentation.shared.SharedViewModel


@Composable
fun ConfirmationScreen(
    sharedViewModel: SharedViewModel,
    viewModel: ConfirmationViewModel = hiltViewModel(),
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    val scannedBitmap by sharedViewModel.scannedBitmap.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IconButton(
                onClick = {
                    sharedViewModel.clearBitmap()
                    onRetake()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFAF9F6))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            scannedBitmap?.let { bitmap ->

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Scanned Receipt",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.extractTextFromBitmap(bitmap) { text ->

                                sharedViewModel.setExtractedText(text)

                                onConfirm()
                            }
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}