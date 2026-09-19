/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Material3AnalogClock(
    zoned: ZonedDateTime,
    showSeconds: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val semanticsText = stringResource(
        R.string.home_switch_to_digital,
    )

    Box(
        modifier = modifier.semantics {
            contentDescription = semanticsText
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * 0.42f

            drawCircle(
                color = outline.copy(alpha = 0.38f),
                radius = radius + 5.dp.toPx(),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
            )
            drawCircle(
                color = surface,
                radius = radius,
                center = center,
            )
            drawCircle(
                color = outline.copy(alpha = 0.18f),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
            )

            for (index in 0 until 60) {
                val angle = index * 6f - 90f
                val radians = Math.toRadians(angle.toDouble())
                val isHour = index % 5 == 0
                val outer = radius - if (isHour) 11.dp.toPx() else 8.dp.toPx()
                val inner = radius - if (isHour) 24.dp.toPx() else 15.dp.toPx()
                val start = Offset(
                    center.x + cos(radians).toFloat() * inner,
                    center.y + sin(radians).toFloat() * inner,
                )
                val end = Offset(
                    center.x + cos(radians).toFloat() * outer,
                    center.y + sin(radians).toFloat() * outer,
                )
                drawLine(
                    color = if (isHour) onSurface else onSurfaceVariant.copy(alpha = 0.42f),
                    start = start,
                    end = end,
                    strokeWidth = if (isHour) 2.1.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            val hourAngle = ((zoned.hour % 12) + zoned.minute / 60f) * 30f - 90f
            val minuteAngle = (zoned.minute + zoned.second / 60f) * 6f - 90f
            val secondAngle = zoned.second * 6f - 90f

            drawHand(center, hourAngle, radius * 0.52f, 4.5.dp.toPx(), onSurface)
            drawHand(center, minuteAngle, radius * 0.72f, 3.dp.toPx(), onSurface)
            if (showSeconds) {
                drawHand(center, secondAngle, radius * 0.80f, 1.4.dp.toPx(), accent)
                val radians = Math.toRadians(secondAngle.toDouble())
                val tail = Offset(
                    center.x - cos(radians).toFloat() * radius * 0.16f,
                    center.y - sin(radians).toFloat() * radius * 0.16f,
                )
                drawLine(
                    color = accent,
                    start = center,
                    end = tail,
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            drawCircle(
                color = surface,
                radius = 6.dp.toPx(),
                center = center,
            )
            drawCircle(
                color = accent,
                radius = 4.dp.toPx(),
                center = center,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHand(
    center: Offset,
    angle: Float,
    length: Float,
    width: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    rotate(angle, pivot = center) {
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x + length, center.y),
            strokeWidth = width,
            cap = StrokeCap.Round,
        )
    }
}
