package com.example.doancoso3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A custom background component that implements the "Fresh Vitality" design aesthetic.
 * Features a deep dark green to black radial gradient with subtle organic light glows.
 */
@Composable
fun FreshVitalityBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Colors from the Fresh Vitality design system
    val surfaceColor = Color(0xFF01180A)
    val blackColor = Color(0xFF001206)
    val limeGlow = Color(0xFFC0FF00).copy(alpha = 0.12f) // Subtle lime glow
    val forestGlow = Color(0xFF1D3525).copy(alpha = 0.25f) // Soft forest green glow

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
            .drawBehind {
                // 1. Main radial gradient from deep green to black
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(surfaceColor, blackColor),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.maxDimension
                    )
                )

                // 2. Organic Lime Glow - Top Right area
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(limeGlow, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = size.width * 0.9f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.9f
                )

                // 3. Organic Forest Glow - Middle Left area
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(forestGlow, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.45f),
                        radius = size.width * 1.2f
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.45f),
                    radius = size.width * 1.2f
                )
                
                // 4. Subtle lime glow - Bottom Right area
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(limeGlow.copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.85f),
                        radius = size.width * 0.6f
                    ),
                    center = Offset(size.width * 0.95f, size.height * 0.85f),
                    radius = size.width * 0.6f
                )
                
                // 4. Subtle center glow for depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF082011).copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = size.width
                    ),
                    center = center,
                    radius = size.width
                )
            }
    ) {
        content()
    }
}
