/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

import android.location.Location

/**
 * Orders real device location providers by capability and fails over without
 * coupling the repository to Google Play services.
 *
 * The provider list is intentionally ordered by the composition root:
 * FusedLocationProvider first, then Android's platform LocationManager.
 */
class AdaptiveCurrentLocationProvider(
    private val providers: List<CurrentLocationProvider>,
) : CurrentLocationProvider {

    override val isAvailable: Boolean
        get() = providers.any(CurrentLocationProvider::isAvailable)

    override suspend fun getFreshLocation(): Location? {
        for (provider in availableProviders()) {
            val location = runCatching { provider.getFreshLocation() }.getOrNull()
            if (location != null) return location
        }
        return null
    }

    override suspend fun getLastKnownLocation(): Location? =
        availableProviders()
            .mapNotNull { provider ->
                runCatching { provider.getLastKnownLocation() }.getOrNull()
            }
            .maxWithOrNull(
                compareBy<Location> {
                    if (it.accuracy > 0f) -it.accuracy else Float.NEGATIVE_INFINITY
                }.thenBy { it.time },
            )

    private fun availableProviders(): List<CurrentLocationProvider> =
        providers.filter(CurrentLocationProvider::isAvailable)
}
