package com.febricahyaa.clockapp.core

/** Kotlin facade: Kotlin → Java/JNI → C++ for timing/math primitives. */
object NativeClock {
    fun remainingSeconds(endMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.remainingSeconds(endMillis, nowMillis)

    fun elapsedMillis(startMillis: Long, nowMillis: Long): Long =
        ChronaNativeBridge.elapsedMillis(startMillis, nowMillis)
}
