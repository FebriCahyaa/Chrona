/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The root Compose surface owns the visual treatment behind both
        // system bars. This keeps the app background continuous from the
        // physical top edge through the status bar and down to the navigation
        // bar while individual surfaces apply content insets where needed.
        enableEdgeToEdge()

        setContent {
            ClockApp()
        }
    }
}
