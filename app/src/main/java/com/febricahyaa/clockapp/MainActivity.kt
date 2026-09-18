/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Chrona owns the edge-to-edge insets and system-bar icon appearance in
        // Compose so light/dark changes update immediately with the theme.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { ClockApp() }
    }
}
