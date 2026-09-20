/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import android.location.LocationManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject

/**
 * Google Play services fused location provider.
 *
 * This provider is opportunistic: it is used only when the device has a
 * usable Google Play services location implementation. Devices without GMS
 * fall back to the platform provider without losing the Current Location
 * feature.
 */
class AndroidFusedLocationProvider @Inject constructor(
    @ApplicationContext context: Context,
) : CurrentLocationProvider {

    private val appContext = context.applicationContext
    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS &&
            LocationManagerCompat.isLocationEnabled(locationManager)

    @SuppressLint("MissingPermission")
    override suspend fun getFreshLocation(): Location? {
        if (!isAvailable || !hasLocationPermission()) return null

        val request = CurrentLocationRequest.Builder()
            .setPriority(
                if (hasFineLocationPermission()) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                },
            )
            .setMaxUpdateAgeMillis(MAX_ACCEPTED_FUSED_CACHE_AGE_MS)
            .setDurationMillis(FRESH_LOCATION_TIMEOUT_MS)
            .build()

        return runCatching {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }

                try {
                    client.getCurrentLocation(
                        request,
                        cancellationTokenSource.token,
                    ).addOnSuccessListener { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }.addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                } catch (_: SecurityException) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                } catch (_: RuntimeException) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? {
        if (!isAvailable || !hasLocationPermission()) return null

        return runCatching {
            suspendCancellableCoroutine { continuation ->
                try {
                    client.lastLocation
                        .addOnSuccessListener { location ->
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                } catch (_: SecurityException) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                } catch (_: RuntimeException) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }.getOrNull()
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

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val FRESH_LOCATION_TIMEOUT_MS = 12_000L
        const val MAX_ACCEPTED_FUSED_CACHE_AGE_MS = 15_000L
    }
}
