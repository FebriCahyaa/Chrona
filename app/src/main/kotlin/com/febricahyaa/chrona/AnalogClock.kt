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

package com.febricahyaa.chrona

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Material 3 Expressive analog clock: a 12-lobed "cookie" face, a thick rounded minute hand,
 * a shorter hour hand and the seconds shown as a dot circling the face. Colors come from the
 * theme (dynamic color), so the clock follows the wallpaper palette.
 */
class AnalogClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED == intent.action) {
                val tz = intent.getStringExtra("time-zone")
                mTime = Calendar.getInstance(TimeZone.getTimeZone(tz))
            }
            onTimeChanged()
        }
    }

    private val mClockTick: Runnable = object : Runnable {
        override fun run() {
            onTimeChanged()
            if (mEnableSeconds) {
                val now = System.currentTimeMillis()
                val delay = DateUtils.SECOND_IN_MILLIS - now % DateUtils.SECOND_IN_MILLIS
                postDelayed(this, delay)
            }
        }
    }

    private var mTime = Calendar.getInstance()
    private val mDescFormat =
            (DateFormat.getTimeFormat(context) as SimpleDateFormat).toLocalizedPattern()
    private var mTimeZone: TimeZone? = null
    private var mEnableSeconds = true

    private val mFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val mHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val mFacePath = Path()

    private val mMinuteHandColor: Int
    private val mHourHandColor: Int

    private val mDefaultSize: Int =
            resources.getDimensionPixelSize(R.dimen.analog_clock_default_size)

    init {
        mFacePaint.color = ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorSecondaryContainer)
        mMinuteHandColor = ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorPrimaryContainer)
        mHourHandColor = ColorUtils.setAlphaComponent(ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorOnSecondaryContainer), 0x99)
        mDotPaint.color = ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorTertiary)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(mDefaultSize, widthMeasureSpec)
        val height = resolveSize(mDefaultSize, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildFacePath()
    }

    /** Scalloped "cookie" outline: 12 lobes around the center. */
    private fun buildFacePath() {
        mFacePath.reset()
        val cx = (paddingLeft + width - paddingRight) / 2f
        val cy = (paddingTop + height - paddingBottom) / 2f
        val radius = min(width - paddingLeft - paddingRight,
                height - paddingTop - paddingBottom) / 2f
        if (radius <= 0f) {
            return
        }
        val steps = 360
        for (i in 0..steps) {
            val theta = 2.0 * PI * i / steps
            val r = radius * (1f - LOBE_DEPTH * (1f - cos(LOBES * theta).toFloat()) / 2f)
            val x = cx + r * cos(theta).toFloat()
            val y = cy + r * sin(theta).toFloat()
            if (i == 0) mFacePath.moveTo(x, y) else mFacePath.lineTo(x, y)
        }
        mFacePath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = (paddingLeft + width - paddingRight) / 2f
        val cy = (paddingTop + height - paddingBottom) / 2f
        val radius = min(width - paddingLeft - paddingRight,
                height - paddingTop - paddingBottom) / 2f
        if (radius <= 0f) {
            return
        }

        canvas.drawPath(mFacePath, mFacePaint)

        val minutes = mTime[Calendar.MINUTE] + mTime[Calendar.SECOND] / 60f
        val hours = mTime[Calendar.HOUR] + minutes / 60f

        mHandPaint.strokeWidth = radius * HAND_WIDTH
        mHandPaint.color = mHourHandColor
        drawHand(canvas, cx, cy, hours * 30f, radius * HOUR_HAND_LENGTH)
        mHandPaint.color = mMinuteHandColor
        drawHand(canvas, cx, cy, minutes * 6f, radius * MINUTE_HAND_LENGTH)

        if (mEnableSeconds) {
            val angle = Math.toRadians((mTime[Calendar.SECOND] * 6f - 90f).toDouble())
            val orbit = radius * SECOND_ORBIT
            canvas.drawCircle(cx + orbit * cos(angle).toFloat(),
                    cy + orbit * sin(angle).toFloat(), radius * SECOND_DOT_RADIUS, mDotPaint)
        }
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float) {
        val angle = Math.toRadians((degrees - 90f).toDouble())
        canvas.drawLine(cx, cy, cx + length * cos(angle).toFloat(),
                cy + length * sin(angle).toFloat(), mHandPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        context.registerReceiver(mIntentReceiver, filter)

        // Refresh the calendar instance since the time zone may have changed while the receiver
        // wasn't registered.
        mTime = Calendar.getInstance(mTimeZone ?: TimeZone.getDefault())
        onTimeChanged()

        // Tick every second.
        if (mEnableSeconds) {
            mClockTick.run()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        context.unregisterReceiver(mIntentReceiver)
        removeCallbacks(mClockTick)
    }

    private fun onTimeChanged() {
        mTime.timeInMillis = System.currentTimeMillis()
        contentDescription = DateFormat.format(mDescFormat, mTime)
        invalidate()
    }

    fun setTimeZone(id: String) {
        mTimeZone = TimeZone.getTimeZone(id)
        mTime.timeZone = mTimeZone!!
        onTimeChanged()
    }

    fun enableSeconds(enable: Boolean) {
        mEnableSeconds = enable
        removeCallbacks(mClockTick)
        if (mEnableSeconds) {
            mClockTick.run()
        } else {
            invalidate()
        }
    }

    companion object {
        private const val LOBES = 12
        /** How far the scallops dip, as a fraction of the radius. */
        private const val LOBE_DEPTH = 0.1f
        private const val HAND_WIDTH = 0.13f
        private const val HOUR_HAND_LENGTH = 0.32f
        private const val MINUTE_HAND_LENGTH = 0.52f
        private const val SECOND_ORBIT = 0.68f
        private const val SECOND_DOT_RADIUS = 0.07f
    }
}
