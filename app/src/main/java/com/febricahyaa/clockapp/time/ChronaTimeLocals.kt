/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

import androidx.compose.runtime.compositionLocalOf

val LocalChronaTimeEngine = compositionLocalOf<ChronaTimeEngine> {
    error("ChronaTimeEngine is not provided to the Compose tree")
}
