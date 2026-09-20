/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Optional in-app client for the isolated Binder service. */
@Singleton
class ChronaSystemServiceClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun ping(): Boolean = suspendCancellableCoroutine { continuation ->
        var service: IChronaSystemService? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = IChronaSystemService.Stub.asInterface(binder)
                val result = runCatching { service?.ping() == true }.getOrDefault(false)
                context.unbindService(this)
                if (continuation.isActive) continuation.resume(result)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        val bound = context.bindService(
            Intent(context, ChronaSystemService::class.java),
            connection,
            Context.BIND_AUTO_CREATE,
        )
        if (!bound && continuation.isActive) continuation.resume(false)
        continuation.invokeOnCancellation {
            if (bound) runCatching { context.unbindService(connection) }
        }
    }
}
