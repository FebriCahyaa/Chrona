/*
 * Copyright (C) 2026 The Chrona Authors
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

package com.febricahyaa.chrona.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils

import com.google.android.material.button.MaterialButton

import kotlin.math.abs

/**
 * The control on the ringing-alarm screen that snoozes or dismisses the alarm, in one of three
 * styles chosen in Settings, as in the M3 Expressive clock:
 *
 * - [STYLE_TAP]: two large buttons, Snooze and Stop.
 * - [STYLE_SLIDE]: a pill with a handle that slides left to snooze, right to stop.
 * - [STYLE_SWIPE]: a handle that swipes up to stop, down to snooze.
 *
 * Dragging handles spring back unless released near the end of their track.
 */
class AlarmDismissControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    interface Listener {
        fun onSnooze()
        fun onDismiss()
    }

    var listener: Listener? = null

    /** The view the snooze reveal animation starts from.  */
    var snoozeTarget: View = this
        private set

    /** The view the dismiss reveal animation starts from.  */
    var dismissTarget: View = this
        private set

    var style: String = STYLE_SLIDE
        set(value) {
            field = value
            build()
        }

    private var mHandled = false

    init {
        build()
    }

    /** Lets a finished preview (in the settings dialog) be used again.  */
    fun reset() {
        mHandled = false
        build()
    }

    private fun build() {
        removeAllViews()
        when (style) {
            STYLE_TAP -> buildTap()
            STYLE_SWIPE -> buildSwipe()
            else -> buildSlide()
        }
    }

    private fun buildTap() {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        val snooze = pillButton(R.string.alarm_alert_snooze_text,
                com.google.android.material.R.attr.colorTertiary,
                com.google.android.material.R.attr.colorOnTertiary) { fireSnooze() }
        val dismiss = pillButton(R.string.alarm_control_stop,
                com.google.android.material.R.attr.colorTertiaryContainer,
                com.google.android.material.R.attr.colorOnTertiaryContainer) { fireDismiss() }
        val gap = dp(4)
        row.addView(snooze, LinearLayout.LayoutParams(0, dp(88), 1f).apply { marginEnd = gap })
        row.addView(dismiss, LinearLayout.LayoutParams(0, dp(88), 1f).apply { marginStart = gap })
        addView(row, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER))
        snoozeTarget = snooze
        dismissTarget = dismiss
    }

    private fun pillButton(label: Int, fillAttr: Int, textAttr: Int, onClick: () -> Unit):
            MaterialButton {
        return MaterialButton(context).apply {
            setText(label)
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            backgroundTintList = ColorStateList.valueOf(color(fillAttr))
            setTextColor(color(textAttr))
            cornerRadius = dp(44)
            insetTop = 0
            insetBottom = 0
            setOnClickListener { onClick() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildSlide() {
        val track = FrameLayout(context)
        track.background = pill(color(com.google.android.material.R.attr.colorSurfaceContainerHigh))
        val snoozeLabel = label(R.string.alarm_alert_snooze_text)
        val dismissLabel = label(R.string.alarm_control_stop)
        track.addView(snoozeLabel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.START or Gravity.CENTER_VERTICAL).apply {
            marginStart = dp(28)
        })
        track.addView(dismissLabel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.END or Gravity.CENTER_VERTICAL).apply {
            marginEnd = dp(28)
        })
        val handle = handle(dp(104), dp(72))
        track.addView(handle, FrameLayout.LayoutParams(dp(104), dp(72), Gravity.CENTER))
        addView(track, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(88), Gravity.CENTER))

        handle.setOnTouchListener(DragListener(horizontal = true) { towardsEnd ->
            // Left snoozes, right stops.
            if (towardsEnd) fireDismiss() else fireSnooze()
        })
        snoozeTarget = snoozeLabel
        dismissTarget = dismissLabel
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildSwipe() {
        val track = FrameLayout(context)
        track.background = pill(color(com.google.android.material.R.attr.colorSurfaceContainerHigh))
        val dismissLabel = arrowLabel(R.string.alarm_control_stop, up = true)
        val snoozeLabel = arrowLabel(R.string.alarm_alert_snooze_text, up = false)
        track.addView(dismissLabel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = dp(20)
        })
        track.addView(snoozeLabel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
            bottomMargin = dp(20)
        })
        val handle = handle(dp(96), dp(96))
        handle.contentDescription = context.getString(R.string.alarm_control_swipe_hint)
        track.addView(handle, FrameLayout.LayoutParams(dp(96), dp(96), Gravity.CENTER))
        addView(track, FrameLayout.LayoutParams(dp(136), dp(320), Gravity.CENTER))

        handle.setOnTouchListener(DragListener(horizontal = false) { towardsEnd ->
            // Up stops, down snoozes.
            if (towardsEnd) fireSnooze() else fireDismiss()
        })
        snoozeTarget = snoozeLabel
        dismissTarget = dismissLabel
    }

    private fun handle(width: Int, height: Int): View {
        val handle = FrameLayout(context)
        handle.background = pill(color(androidx.appcompat.R.attr.colorPrimary))
        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_alarm)
        icon.imageTintList = ColorStateList.valueOf(
                color(com.google.android.material.R.attr.colorOnPrimary))
        handle.addView(icon, FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER))
        handle.minimumWidth = width
        handle.minimumHeight = height
        handle.contentDescription = context.getString(R.string.description_direction_both)
        // Switch Access / TalkBack users get explicit actions instead of dragging.
        ViewCompat.addAccessibilityAction(handle,
                context.getString(R.string.alarm_alert_snooze_text)) { _, _ ->
            fireSnooze()
            true
        }
        ViewCompat.addAccessibilityAction(handle,
                context.getString(R.string.alarm_control_stop)) { _, _ ->
            fireDismiss()
            true
        }
        return handle
    }

    private fun label(text: Int): TextView {
        return TextView(context).apply {
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(color(com.google.android.material.R.attr.colorOnSurface))
        }
    }

    private fun arrowLabel(text: Int, up: Boolean): LinearLayout {
        val column = LinearLayout(context)
        column.orientation = LinearLayout.VERTICAL
        column.gravity = Gravity.CENTER_HORIZONTAL
        val arrow = ImageView(context)
        arrow.setImageResource(R.drawable.ic_chevron_left)
        arrow.rotation = if (up) 90f else -90f
        arrow.imageTintList = ColorStateList.valueOf(
                color(com.google.android.material.R.attr.colorOnSurfaceVariant))
        val label = label(text)
        if (up) {
            column.addView(arrow, LinearLayout.LayoutParams(dp(24), dp(24)))
            column.addView(label)
        } else {
            column.addView(label)
            column.addView(arrow, LinearLayout.LayoutParams(dp(24), dp(24)))
        }
        return column
    }

    private fun pill(fill: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(1000).toFloat()
            setColor(fill)
        }
    }

    private fun fireSnooze() {
        if (!mHandled) {
            mHandled = true
            listener?.onSnooze() ?: reset()
        }
    }

    private fun fireDismiss() {
        if (!mHandled) {
            mHandled = true
            listener?.onDismiss() ?: reset()
        }
    }

    private fun color(attr: Int): Int = ThemeUtils.resolveColor(context, attr)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /**
     * Drags a handle along its track; released past [COMMIT_FRACTION] of the way to either end
     * it completes ([onCommit] gets `true` for the right/bottom end), otherwise it springs back.
     */
    private inner class DragListener(
        private val horizontal: Boolean,
        private val onCommit: (towardsEnd: Boolean) -> Unit
    ) : View.OnTouchListener {
        private var mStart = 0f
        private var mMoved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val parent = view.parent as View
            val maxShift = if (horizontal) {
                (parent.width - view.width) / 2f
            } else {
                (parent.height - view.height) / 2f
            }
            val position = if (horizontal) event.rawX else event.rawY
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mStart = position
                    mMoved = false
                    view.animate().cancel()
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start()
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = (position - mStart).coerceIn(-maxShift, maxShift)
                    if (abs(shift) > dp(4)) {
                        mMoved = true
                    }
                    if (horizontal) view.translationX = shift else view.translationY = shift
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val shift = if (horizontal) view.translationX else view.translationY
                    val committed = event.actionMasked == MotionEvent.ACTION_UP &&
                            maxShift > 0 && abs(shift) >= maxShift * COMMIT_FRACTION
                    if (committed) {
                        val end = if (shift > 0) maxShift else -maxShift
                        val anim = view.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        if (horizontal) anim.translationX(end) else anim.translationY(end)
                        anim.withEndAction { onCommit(shift > 0) }.start()
                    } else {
                        // Spring back, overshooting slightly, like M3 Expressive motion.
                        val anim = view.animate().scaleX(1f).scaleY(1f)
                                .setInterpolator(OvershootInterpolator(2.5f)).setDuration(420)
                        if (horizontal) anim.translationX(0f) else anim.translationY(0f)
                        anim.start()
                        if (!mMoved && event.actionMasked == MotionEvent.ACTION_UP) {
                            hint(view, maxShift)
                        }
                    }
                }
            }
            return true
        }

        /** A tap nudges the handle to show which way it moves.  */
        private fun hint(view: View, maxShift: Float) {
            val nudge = maxShift * 0.35f
            val anim = view.animate().setInterpolator(OvershootInterpolator(2.5f))
                    .setDuration(220)
            if (horizontal) anim.translationX(nudge) else anim.translationY(-nudge)
            anim.withEndAction {
                val back = view.animate().setDuration(420)
                if (horizontal) back.translationX(0f) else back.translationY(0f)
                back.start()
            }.start()
        }
    }

    companion object {
        const val STYLE_TAP = "tap"
        const val STYLE_SLIDE = "slide"
        const val STYLE_SWIPE = "swipe"

        private const val COMMIT_FRACTION = 0.8f
    }
}
