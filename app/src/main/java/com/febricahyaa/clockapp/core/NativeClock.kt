package com.febricahyaa.clockapp.core

/** Stable Kotlin facade for C++ timing primitives exposed through the Java/JNI boundary. */
object NativeClock {
    fun remainingSeconds(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingSeconds(endMillis, nowMillis)

    fun elapsedMillis(startMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.elapsedMillis(startMillis, nowMillis)
}
