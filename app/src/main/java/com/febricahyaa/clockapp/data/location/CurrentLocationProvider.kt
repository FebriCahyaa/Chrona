/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

import android.location.Location

/**
 * Provider abstraction for obtaining a one-shot current location.
 *
 * Implementations decide whether the underlying device capabilities are
 * available and must return null rather than throw for expected provider
 * failures. Permission checks remain explicit at each Android API boundary.
 */
interface CurrentLocationProvider {
    val isAvailable: Boolean

    suspend fun getFreshLocation(): Location?

    suspend fun getLastKnownLocation(): Location?
}
