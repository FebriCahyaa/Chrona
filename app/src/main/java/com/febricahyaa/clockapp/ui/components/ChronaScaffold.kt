/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBarScrollBehavior

/**
 * Chrona's universal screen shell.
 *
 * Every scrollable destination gets the same LargeTopAppBar + nested-scroll
 * contract. The title and subtitle read the collapse fraction only inside
 * graphicsLayer, so toolbar motion updates the draw layer without making the
 * screen root recompose for every frame.
 */
@Composable
fun ChronaScaffold(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                title = {
                    ChronaCollapsingTitle(
                        title = title,
                        subtitle = subtitle,
                        scrollBehavior = scrollBehavior,
                    )
                },
                actions = actions,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun ChronaCollapsingTitle(
    title: String,
    subtitle: String,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.graphicsLayer {
                val collapsed = scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
                val scale = 1f - (0.08f * collapsed)
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
        ) {
            androidx.compose.material3.Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
                maxLines = 1,
            )
        }

        Spacer(
            modifier = Modifier
                .height(2.dp)
                .graphicsLayer {
                    alpha = 1f - scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
                },
        )

        androidx.compose.material3.Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
                alpha = 1f - scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
                translationY = -2.dp.toPx() * scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
            },
        )
    }
}
