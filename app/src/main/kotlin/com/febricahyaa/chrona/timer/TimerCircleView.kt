/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.febricahyaa.chrona.timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.graphics.ColorUtils

import com.febricahyaa.chrona.ThemeUtils
import com.febricahyaa.chrona.data.Timer

import com.google.android.material.progressindicator.CircularProgressIndicator

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Shows a timer's remaining time as a Material 3 Expressive wavy circular progress indicator
 * that shrinks as the timer runs; an expired timer shows a filled disc instead.
 */
class TimerCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CircularProgressIndicator(context, attrs,
        com.google.android.material.R.attr.circularProgressIndicatorStyle) {

    /** Fills the ring once the timer expires, a lighter tone of the expired card.  */
    private val mExpiredPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ColorUtils.setAlphaComponent(ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorOnPrimaryContainer), 0x1F)
    }

    private var mExpired = false

    init {
        isIndeterminate = false
        max = PROGRESS_MAX
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Grow the indicator to fill the view; it defaults to a small spinner size.
        val size = min(w - paddingLeft - paddingRight, h - paddingTop - paddingBottom) -
                2 * indicatorInset
        if (size > 0 && size != indicatorSize) {
            post { indicatorSize = size }
        }
    }

    fun update(timer: Timer) {
        val expired = timer.isExpired || timer.isMissed
        if (expired != mExpired) {
            mExpired = expired
            invalidate()
        }
        val total = timer.totalLength
        val remaining = if (timer.isReset || total <= 0) {
            1f
        } else {
            (timer.remainingTime.toFloat() / total).coerceIn(0f, 1f)
        }
        setProgressCompat((remaining * PROGRESS_MAX).roundToInt(), false)
    }

    override fun onDraw(canvas: Canvas) {
        if (mExpired) {
            val radius = min(width, height) / 2f - indicatorInset
            canvas.drawCircle(width / 2f, height / 2f, radius, mExpiredPaint)
            return
        }
        super.onDraw(canvas)
    }

    private companion object {
        const val PROGRESS_MAX = 10_000
    }
}
