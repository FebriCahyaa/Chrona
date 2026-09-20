/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class AndroidCurrentLocationRepository(
    context: Context,
) : CurrentLocationRepository {

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun getCurrentLocation(): CurrentLocation? {
        if (!hasLocationPermission()) return null

        val location = withTimeoutOrNull(LOCATION_PROVIDER_TIMEOUT_MS) {
            requestFreshLocation()
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

    private suspend fun requestFreshLocation(): Location? {
        val provider = enabledProvider() ?: return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestCurrentLocation(provider)
        } else {
            requestLegacySingleUpdate(provider)
        }
    }

    private suspend fun requestCurrentLocation(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()

            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }

            try {
                locationManager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(appContext),
                ) { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            } catch (_: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }

    @Suppress("DEPRECATION")
    private suspend fun requestLegacySingleUpdate(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            }

            continuation.invokeOnCancellation {
                runCatching { locationManager.removeUpdates(listener) }
            }

            try {
                locationManager.requestSingleUpdate(
                    provider,
                    listener,
                    Looper.getMainLooper(),
                )
            } catch (_: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }.also { location ->
            // The one-shot request normally removes itself after the callback.
            // The cancellation handler remains responsible for timeout/cancel.
            location
        }

    private fun enabledProvider(): String? {
        val providers = runCatching {
            locationManager.getProviders(true)
        }.getOrDefault(emptyList())

        return when {
            LocationManager.GPS_PROVIDER in providers -> LocationManager.GPS_PROVIDER
            LocationManager.NETWORK_PROVIDER in providers -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }

    private fun bestRecentLastKnownLocation(): Location? {
        val now = System.currentTimeMillis()
        val maxAge = 30 * 60 * 1000L

        val candidates = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        ).mapNotNull { provider ->
            runCatching {
                locationManager.getLastKnownLocation(provider)
            }.getOrNull()
        }

        return candidates
            .filter { it.time > 0L && now - it.time in 0..maxAge }
            .minWithOrNull(
                compareBy<Location> { if (it.accuracy > 0f) it.accuracy else Float.MAX_VALUE }
                    .thenByDescending { it.time },
            )
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

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
    }
}
