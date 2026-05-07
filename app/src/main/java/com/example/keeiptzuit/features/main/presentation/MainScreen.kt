package com.example.keeiptzuit.features.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.keeiptzuit.features.profile.navigation.Profile
import com.example.keeiptzuit.features.profile.navigation.profileNavGraph
import com.example.keeiptzuit.features.scanner.navigation.Scanner
import com.example.keeiptzuit.features.scanner.navigation.scannerNavGraph

data class ButtonData(val text: String, val icon: ImageVector)

@Composable
fun MainScreen(
    onNavigateToScanner: () -> Unit = {},
) {
    val navController = rememberNavController()

    val buttons = listOf(
        ButtonData("Home", Icons.Default.Home),
        ButtonData("Stats", Icons.Default.StackedLineChart),
        ButtonData("Scanner", Icons.Default.Add),
        ButtonData("Receipts", Icons.Default.Receipt),
        ButtonData("Profile", Icons.Default.PersonOutline),
    )

    Scaffold(
        bottomBar = {
            StaticNavigationBar(
                buttons = buttons,
                barColor = MaterialTheme.colorScheme.surfaceContainer,
                circleColor = MaterialTheme.colorScheme.primary,
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                fixedCircleIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> { /* navigate to Home */ }
                        1 -> { /* navigate to Stats */ }
                        2 -> onNavigateToScanner()
                        3 -> { /* navigate to Receipts */ }
                        4 -> navController.navigate(Profile)
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Profile
        ) {
            profileNavGraph()
        }
    }
}

@Composable
fun StaticNavigationBar(
    buttons: List<ButtonData>,
    barColor: Color,
    circleColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    fixedCircleIndex: Int = 2,
    onItemSelected: (Int) -> Unit = {},
) {
    val circleRadius = 26.dp
    var selectedItem by rememberSaveable { mutableIntStateOf(fixedCircleIndex) }
    var barSize by remember { mutableStateOf(IntSize(0, 0)) }

    val offsetStep = remember(barSize) {
        barSize.width.toFloat() / (buttons.size * 2)
    }
    val fixedOffset = remember(offsetStep) {
        offsetStep + fixedCircleIndex * 2 * offsetStep
    }

    val circleRadiusPx = androidx.compose.ui.platform.LocalDensity.current.run {
        circleRadius.toPx().toInt()
    }

    val barShape = remember(fixedOffset) {
        BarShape(
            offset = fixedOffset,
            circleRadius = circleRadius,
            cornerRadius = 25.dp,
        )
    }

    val circleOffset = IntOffset(fixedOffset.toInt() - circleRadiusPx, -circleRadiusPx)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Circle(
            modifier = Modifier
                .offset { circleOffset }
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            button = buttons[fixedCircleIndex],
            iconColor = Color.White,
            onClick = {
                selectedItem = fixedCircleIndex
                onItemSelected(fixedCircleIndex)
            }
        )
        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .graphicsLayer {
                    shape = barShape
                    clip = true
                }
                .fillMaxWidth()
                .background(barColor),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            buttons.forEachIndexed { index, button ->
                val isSelected = index == selectedItem
                val isCircleSlot = index == fixedCircleIndex
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        selectedItem = index
                        onItemSelected(index)
                    },
                    icon = {
                        Icon(
                            imageVector = button.icon,
                            contentDescription = button.text,
                            modifier = Modifier.alpha(if (isCircleSlot) 0f else 1f)
                        )
                    },
                    label = {
                        Text(if (isCircleSlot) "" else button.text)
                    },
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        selectedIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}

@Composable
private fun Circle(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    radius: Dp,
    button: ButtonData,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
    ) {
        Icon(button.icon, button.text, tint = iconColor)
    }
}

private class BarShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp = 5.dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterX = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }
        val cornerDiameter = cornerRadiusPx * 2
        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.5f
            val cutoutLeftX = cutoutCenterX - cutoutEdgeOffset
            val cutoutRightX = cutoutCenterX + cutoutEdgeOffset
            moveTo(x = 0F, y = size.height)
            if (cutoutLeftX > 0) {
                val realLeftCornerDiameter = if (cutoutLeftX >= cornerRadiusPx) cornerDiameter else cutoutLeftX * 2
                arcTo(
                    rect = Rect(0f, 0f, realLeftCornerDiameter, realLeftCornerDiameter),
                    startAngleDegrees = 180.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }
            lineTo(cutoutLeftX, 0f)
            cubicTo(cutoutCenterX - cutoutRadius, 0f, cutoutCenterX - cutoutRadius, cutoutRadius, cutoutCenterX, cutoutRadius)
            cubicTo(cutoutCenterX + cutoutRadius, cutoutRadius, cutoutCenterX + cutoutRadius, 0f, cutoutRightX, 0f)
            if (cutoutRightX < size.width) {
                val realRightCornerDiameter = if (cutoutRightX <= size.width - cornerRadiusPx) cornerDiameter else (size.width - cutoutRightX) * 2
                arcTo(
                    rect = Rect(size.width - realRightCornerDiameter, 0f, size.width, realRightCornerDiameter),
                    startAngleDegrees = -90.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }
            lineTo(x = size.width, y = size.height)
            close()
        }
    }
}