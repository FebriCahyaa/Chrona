/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.components

import android.graphics.RectF
import android.graphics.Region
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.location.CurrentLocation
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

private data class RuntimeWorldMapFeature(
    val data: WorldMapFeatureData,
    val path: Path,
    val region: Region,
)

/** Fixed offline world map driven only by the device's real location. */
@Composable
fun WorldClockMap(
    location: CurrentLocation?,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val land = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
    val selected = MaterialTheme.colorScheme.primaryContainer
    val selectedOutline = MaterialTheme.colorScheme.primary
    val marker = MaterialTheme.colorScheme.primary
    val markerHalo = MaterialTheme.colorScheme.surface

    val features = remember {
        val clip = Region(
            0,
            0,
            WORLD_MAP_WIDTH.roundToInt(),
            WORLD_MAP_HEIGHT.roundToInt(),
        )
        WORLD_MAP_FEATURES.mapNotNull { data ->
            val androidPath = runCatching {
                PathParser.createPathFromPathData(data.pathData)
            }.getOrNull() ?: return@mapNotNull null
            RuntimeWorldMapFeature(
                data = data,
                path = androidPath.asComposePath(),
                region = Region().apply { setPath(androidPath, clip) },
            )
        }
    }

    val markerPoint = location?.let { projectLocation(it.latitude, it.longitude) }
    val activeFeatureIndex = markerPoint?.let { point ->
        features.indexOfFirst { feature ->
            feature.region.contains(point.x.roundToInt(), point.y.roundToInt())
        }
    } ?: -1
    val activeRegion = features.getOrNull(activeFeatureIndex)?.data?.name

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(WORLD_MAP_WIDTH / WORLD_MAP_HEIGHT),
            ) {
                drawRect(surface)
                val scaleX = size.width / WORLD_MAP_WIDTH
                val scaleY = size.height / WORLD_MAP_HEIGHT

                withTransform({
                    scale(scaleX, scaleY, pivot = Offset.Zero)
                }) {
                    val xMeridian = markerPoint?.x ?: utcMeridianX()
                    features.forEachIndexed { index, feature ->
                        val active = index == activeFeatureIndex
                        drawPath(
                            feature.path,
                            color = if (active) selected else land,
                        )
                        drawPath(
                            feature.path,
                            color = if (active) selectedOutline else outline,
                            style = Stroke(
                                width = if (active) 1.5f else 0.75f,
                                cap = StrokeCap.Round,
                            ),
                        )
                    }

                    drawLine(
                        color = selectedOutline.copy(alpha = 0.42f),
                        start = Offset(xMeridian, 0f),
                        end = Offset(xMeridian, WORLD_MAP_HEIGHT),
                        strokeWidth = 1.2f,
                    )
                }

                if (markerPoint != null) {
                    val point = Offset(
                        markerPoint.x * scaleX,
                        markerPoint.y * scaleY,
                    )
                    drawCircle(
                        color = markerHalo.copy(alpha = 0.90f),
                        radius = 8.dp.toPx(),
                        center = point,
                    )
                    drawCircle(
                        color = marker,
                        radius = 4.5.dp.toPx(),
                        center = point,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            (-3..3).forEach { delta ->
                val hour = deviceUtcOffsetHour() + delta
                Text(
                    formatUtc(hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (delta == 0) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        Text(
            text = when {
                activeRegion != null -> stringResource(R.string.world_map_active_region, activeRegion)
                location == null -> stringResource(R.string.world_map_location_unavailable)
                else -> stringResource(R.string.world_map_location_outside_catalog)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}

private fun projectLocation(latitude: Double, longitude: Double): Offset = Offset(
    x = (((longitude + 180.0) / 360.0) * WORLD_MAP_WIDTH)
        .coerceIn(0.0, WORLD_MAP_WIDTH.toDouble())
        .toFloat(),
    y = (((90.0 - latitude) / 180.0) * WORLD_MAP_HEIGHT)
        .coerceIn(0.0, WORLD_MAP_HEIGHT.toDouble())
        .toFloat(),
)

private fun utcMeridianX(): Float = WORLD_MAP_WIDTH / 2f

private fun deviceUtcOffsetHour(): Int {
    val now = Instant.now()
    val offsetSeconds = ZoneId.systemDefault().rules.getOffset(now).totalSeconds
    return (offsetSeconds / 3600f).roundToInt().coerceIn(-12, 14)
}

private fun formatUtc(hour: Int): String = when {
    hour == 0 -> "UTC"
    hour > 0 -> "UTC +$hour"
    else -> "UTC $hour"
}
