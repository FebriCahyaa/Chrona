package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.febricahyaa.clockapp.core.ChronaNativeBridge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope

private data class AnalogAngles(val hour: Float, val minute: Float, val second: Float)

@Composable
fun LiveAnalogClock(
    modifier: Modifier = Modifier,
    sizeFraction: Float = 0.94f,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val angles by produceState(
        initialValue = AnalogAngles(0f, 0f, 0f),
        key1 = lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                val now = System.currentTimeMillis()
                val nextBoundary = 1000L - (now % 1000L)
                val values = ChronaNativeBridge.anglesForEpochMillis(now)
                value = AnalogAngles(values[0], values[1], values[2])
                delay(nextBoundary.coerceAtLeast(16L))
            }
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val dialBrush = Brush.radialGradient(listOf(surfaceContainerHighest.copy(alpha = 0.95f), surfaceContainer.copy(alpha = 0.88f), surface.copy(alpha = 0.74f)))
    val outlineColor = outline.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxWidth(sizeFraction).aspectRatio(1f).clip(RoundedCornerShape(50.dp))) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            drawCircle(brush = dialBrush, radius = radius * 0.98f, center = center)
            drawCircle(
                color = outlineColor,
                radius = radius * 0.98f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.018f)
            )

            // Material 3 expressive pill index system.
            for (index in 0 until 60) {
                val major = index % 5 == 0
                val angle = index * 6f
                rotate(angle, pivot = center) {
                    val y1 = center.y - radius * if (major) 0.86f else 0.90f
                    val y2 = center.y - radius * if (major) 0.77f else 0.87f
                    drawLine(
                        color = onSurface.copy(alpha = if (major) 0.72f else 0.28f),
                        start = Offset(center.x, y1),
                        end = Offset(center.x, y2),
                        strokeWidth = radius * if (major) 0.020f else 0.010f,
                        cap = StrokeCap.Round
                    )
                }
            }

            fun drawHand(angle: Float, length: Float, width: Float, color: Color, tail: Float = 0f) {
                rotate(angle, pivot = center) {
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y + radius * tail),
                        end = Offset(center.x, center.y - radius * length),
                        strokeWidth = radius * width,
                        cap = StrokeCap.Round
                    )
                }
            }

            drawHand(angles.hour, 0.52f, 0.070f, onSurface, 0.04f)
            drawHand(angles.minute, 0.70f, 0.050f, primary, 0.05f)
            drawHand(angles.second, 0.78f, 0.022f, tertiary, 0.10f)

            drawCircle(color = onSurface, radius = radius * 0.064f, center = center)
            drawCircle(color = primary, radius = radius * 0.027f, center = center)
        }
    }
}
