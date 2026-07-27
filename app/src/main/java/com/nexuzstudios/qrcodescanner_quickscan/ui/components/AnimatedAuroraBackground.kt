package com.nexuzstudios.qrcodescanner_quickscan.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.nexuzstudios.qrcodescanner_quickscan.ui.theme.DarkBackground
import com.nexuzstudios.qrcodescanner_quickscan.ui.theme.NeonBlue

/**
 * A stunning, premium animated mesh/aurora background.
 * Perfect for giving that ultra-modern 2026 flagship feel.
 */
@Composable
fun AnimatedAuroraBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    
    // Slow breathing animation offsets
    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x1"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y1"
    )
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x2"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 800f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y2"
    )

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // First Aurora Orb (Top Left/Center) - Electric Cyan
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonBlue.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f + offsetX1, size.height * 0.1f + offsetY1),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.2f + offsetX1, size.height * 0.1f + offsetY1)
            )

            // Second Aurora Orb (Bottom Right) - Deep Purple Glow
            val purpleGlow = Color(0xFFA000FF)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        purpleGlow.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.8f + offsetX2, size.height * 0.8f + offsetY2),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.8f + offsetX2, size.height * 0.8f + offsetY2)
            )
        }
        
        // Render content on top of beautiful background
        content()
    }
}
