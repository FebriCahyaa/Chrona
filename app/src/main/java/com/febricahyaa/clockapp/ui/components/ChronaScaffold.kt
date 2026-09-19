/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.ui.theme.ChronaGlassTokens
import com.febricahyaa.clockapp.ui.theme.LocalChronaThemeMode

/**
 * Chrona's universal screen shell.
 *
 * The title, subtitle, navigation affordance, actions and collapse animation are
 * owned by one Material 3 flexible top-app-bar state. This prevents the old
 * dual-collapse system where the platform app bar and a custom graphics layer
 * could disagree about scroll progress and produce subtitle/back jitter.
 */
@Composable
fun ChronaScaffold(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState,
    )
    val isGlass = LocalChronaThemeMode.current == AppThemeMode.GLASS
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    val expandedContainer = if (isGlass) {
        background.copy(alpha = ChronaGlassTokens.ToolbarAlpha)
    } else {
        background
    }
    val collapsedContainer = if (isGlass) {
        surface.copy(alpha = ChronaGlassTokens.ToolbarScrolledAlpha)
    } else {
        surface
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    androidx.compose.material3.Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                subtitle = {
                    androidx.compose.material3.Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = expandedContainer,
                    scrolledContainerColor = collapsedContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    subtitleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}
