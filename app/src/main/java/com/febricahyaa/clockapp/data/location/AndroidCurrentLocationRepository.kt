/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

import android.Manifest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Application-facing repository for the device's current location.
 *
 * Provider selection is delegated to [CurrentLocationProvider]. The repository
 * owns normalization, freshness filtering, and reverse geocoding only.
 */
class AndroidCurrentLocationRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val provider: AdaptiveCurrentLocationProvider,
) : CurrentLocationRepository {

    private val appContext = context.applicationContext

    override suspend fun getCurrentLocation(): CurrentLocation? {
        if (!hasLocationPermission()) return null

        val location = withTimeoutOrNull(LOCATION_PROVIDER_TIMEOUT_MS) {
            provider.getFreshLocation()
        } ?: bestRecentLastKnownLocation()

        location ?: return null

        val address = reverseGeocode(location)

        return CurrentLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            city = address?.cityName(),
            country = address?.countryName ?: address?.countryCode,
            accuracyMeters = location.accuracy.takeIf { it > 0f },
            updatedAtMillis = location.time.takeIf { it > 0L }
                ?: System.currentTimeMillis(),
        )
    }

    private suspend fun bestRecentLastKnownLocation(): Location? =
        provider.getLastKnownLocation()?.takeIf { location ->
            val now = System.currentTimeMillis()
            val age = now - location.time
            location.time > 0L && age in 0..MAX_LAST_KNOWN_AGE_MS
        }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    private suspend fun reverseGeocode(location: Location): Address? {
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(appContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) {
                                    continuation.resume(addresses.firstOrNull())
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) {
                                    continuation.resume(null)
                                }
                            }
                        },
                    )
                } catch (_: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            withContext(Dispatchers.IO) {
                runCatching {
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1,
                    )?.firstOrNull()
                }.getOrNull()
            }
        }
    }

    private fun Address.cityName(): String? =
        locality
            ?: subAdminArea
            ?: adminArea

    private companion object {
        const val LOCATION_PROVIDER_TIMEOUT_MS = 12_000L
        const val MAX_LAST_KNOWN_AGE_MS = 30 * 60 * 1000L
    }
}
