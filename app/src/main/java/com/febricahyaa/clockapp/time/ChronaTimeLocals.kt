/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import androidx.compose.runtime.compositionLocalOf

val LocalChronaTimeEngine = compositionLocalOf<ChronaTimeEngine> {
    error("ChronaTimeEngine is not provided to the Compose tree")
}
