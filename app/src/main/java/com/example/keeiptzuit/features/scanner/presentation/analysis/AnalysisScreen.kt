package com.example.keeiptzuit.features.scanner.presentation.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keeiptzuit.features.scanner.presentation.shared.SharedViewModel

@Composable
fun AnalysisScreen(
    onExitToMain: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    val extractedText by sharedViewModel.extractedText.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF9F6))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = {
                    sharedViewModel.clearBitmap()
                    sharedViewModel.clearExtractedText()
                    onExitToMain()
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
            Text(
                text = extractedText ?: "No text extracted",
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}