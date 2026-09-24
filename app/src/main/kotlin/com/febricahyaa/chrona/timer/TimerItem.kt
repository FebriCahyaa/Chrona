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
import android.content.res.ColorStateList
import android.os.SystemClock
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils
import com.febricahyaa.chrona.TimerTextController
import com.febricahyaa.chrona.Utils.ClickAccessibilityDelegate
import com.febricahyaa.chrona.data.Timer

import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * This view is a visual representation of a [Timer]: a Material 3 Expressive card with the
 * label and a delete button, a progress ring holding the time and a state icon, and +1:00 /
 * reset buttons while running. The card turns primary-container colored once the timer expires.
 * Several timers are laid out as a grid of [compact] cards.
 */
class TimerItem @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    /** Displays the remaining time or time since expiration.  */
    private lateinit var mTimerText: TextView

    /** Formats and displays the text in the timer.  */
    private lateinit var mTimerTextController: TimerTextController

    /** Displays timer progress as a ring.  */
    private lateinit var mCircleView: TimerCircleView

    /** Adds a minute to the timer.  */
    private lateinit var mAddMinuteButton: MaterialButton
    private var mAddMinuteTint: ColorStateList? = null
    private var mAddMinuteTextColors: ColorStateList? = null

    /** Resets the timer.  */
    private lateinit var mResetButton: MaterialButton

    /** Pause (running), play (paused/reset) or stop (expired) under the time.  */
    private lateinit var mStateIcon: ImageView

    /** The card that holds everything; recolored when the timer expires.  */
    private lateinit var mCard: View
    private lateinit var mCardBackground: MaterialShapeDrawable

    /** Displays the label associated with the timer. Tapping it presents an edit dialog.  */
    private lateinit var mLabelView: TextView

    /** Deletes the timer.  */
    private lateinit var mDeleteButton: ImageView

    /** The last state of the timer that was rendered; used to avoid expensive operations.  */
    private var mLastState: Timer.State? = null

    /** `false` where timers can only be dismissed, such as the expired-timers alert.  */
    var showDeleteAndReset = true
        set(value) {
            field = value
            mLastState = null
        }

    /** `true` to draw the smaller card used when several timers share a grid.  */
    var compact = false
        set(value) {
            if (field != value) {
                field = value
                applySize()
            }
        }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mLabelView = findViewById(R.id.timer_label)
        mAddMinuteButton = findViewById(R.id.reset_add)
        mResetButton = findViewById(R.id.timer_reset)
        mDeleteButton = findViewById(R.id.timer_delete)
        mStateIcon = findViewById(R.id.timer_state_icon)
        mCircleView = findViewById(R.id.timer_time)
        mTimerText = findViewById(R.id.timer_time_text)
        mTimerTextController = TimerTextController(mTimerText)

        mCard = findViewById(R.id.timer_card)
        val radius = resources.getDimension(R.dimen.timer_card_corner_radius)
        mCardBackground = MaterialShapeDrawable(
                ShapeAppearanceModel.builder().setAllCornerSizes(radius).build())
        mCard.background = mCardBackground
        mAddMinuteTint = mAddMinuteButton.backgroundTintList
        mAddMinuteTextColors = mAddMinuteButton.textColors
        applyCardColors(expired = false)
    }

    private fun applySize() {
        val res = resources
        val dp = res.displayMetrics.density
        findViewById<View>(R.id.timer_ring).updateLayoutParams {
            height = if (compact) (124 * dp).toInt()
                    else res.getDimensionPixelSize(R.dimen.timer_card_ring_size)
        }
        mTimerText.updateLayoutParams {
            width = if (compact) (88 * dp).toInt()
                    else res.getDimensionPixelSize(R.dimen.timer_card_time_width)
        }
        mTimerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (compact) 26f else 64f)
        val buttonHeight = if (compact) (36 * dp).toInt()
                else res.getDimensionPixelSize(R.dimen.timer_card_button_height)
        mAddMinuteButton.updateLayoutParams { height = buttonHeight }
        mAddMinuteButton.minWidth = ((if (compact) 64 else 120) * dp).toInt()
        mAddMinuteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (compact) 13f else 16f)
        mResetButton.updateLayoutParams {
            width = buttonHeight
            height = buttonHeight
        }
        mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (compact) 12f else 16f)
    }

    private fun applyCardColors(expired: Boolean) {
        val c = context
        val fillAttr: Int
        val contentAttr: Int
        if (expired) {
            fillAttr = com.google.android.material.R.attr.colorPrimaryContainer
            contentAttr = com.google.android.material.R.attr.colorOnPrimaryContainer
        } else {
            fillAttr = com.google.android.material.R.attr.colorSurfaceContainer
            contentAttr = com.google.android.material.R.attr.colorOnSurface
        }
        mCardBackground.fillColor = ColorStateList.valueOf(ThemeUtils.resolveColor(c, fillAttr))
        val content = ThemeUtils.resolveColor(c, contentAttr)
        mTimerText.setTextColor(content)
        mLabelView.setTextColor(content)
        mLabelView.setHintTextColor(ColorStateList.valueOf(content).withAlpha(0x99))
        val contentTint = ColorStateList.valueOf(content)
        mStateIcon.imageTintList = contentTint
        mDeleteButton.imageTintList = contentTint
        if (expired) {
            // A dark pill on the light expired card.
            mAddMinuteButton.backgroundTintList = ColorStateList.valueOf(content)
            mAddMinuteButton.setTextColor(ThemeUtils.resolveColor(c, fillAttr))
        } else {
            mAddMinuteButton.backgroundTintList = mAddMinuteTint
            mAddMinuteTextColors?.let { mAddMinuteButton.setTextColor(it) }
        }
    }

    /**
     * Updates this view to display the latest state of the `timer`.
     */
    fun update(timer: Timer) {
        // Update the time.
        mTimerTextController.setTimeString(timer.remainingTime)

        // Update the label if it changed.
        val label: String? = timer.label
        if (!TextUtils.equals(label, mLabelView.text)) {
            mLabelView.text = label
        }
        // An unnamed timer is called after its length, e.g. "Timer 5m".
        if (showDeleteAndReset) {
            mLabelView.hint = defaultLabel(timer.length)
        }

        // Update the progress of the circle.
        mCircleView.update(timer)

        // A paused timer blinks its time.
        val blinkOff = SystemClock.elapsedRealtime() % 1000 < 500
        if (!timer.isPaused || !blinkOff || mTimerText.isPressed) {
            mTimerText.alpha = 1f
        } else {
            mTimerText.alpha = 0f
        }

        // Update some potentially expensive areas of the user interface only on state changes.
        if (timer.state != mLastState) {
            mLastState = timer.state
            val context = context
            when (mLastState) {
                Timer.State.RESET, Timer.State.PAUSED -> {
                    applyCardColors(expired = false)
                    mStateIcon.setImageResource(R.drawable.ic_start_24dp)
                    // Only the play icon remains; tapping the ring resumes the timer.
                    mAddMinuteButton.visibility = View.GONE
                    mResetButton.visibility = View.GONE
                    mDeleteButton.visibility = deleteVisibility
                    mTimerText.isClickable = true
                    mTimerText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    ViewCompat.setAccessibilityDelegate(mTimerText, ClickAccessibilityDelegate(
                            context.getString(R.string.timer_start), true))
                }
                Timer.State.RUNNING -> {
                    applyCardColors(expired = false)
                    mStateIcon.setImageResource(R.drawable.ic_pause_24dp)
                    mAddMinuteButton.visibility = View.VISIBLE
                    mResetButton.visibility =
                            if (showDeleteAndReset) View.VISIBLE else View.GONE
                    mDeleteButton.visibility = deleteVisibility
                    mTimerText.isClickable = true
                    mTimerText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    ViewCompat.setAccessibilityDelegate(mTimerText, ClickAccessibilityDelegate(
                            context.getString(R.string.timer_pause)))
                }
                Timer.State.EXPIRED, Timer.State.MISSED -> {
                    applyCardColors(expired = true)
                    mStateIcon.setImageResource(R.drawable.ic_stop_24dp)
                    mAddMinuteButton.visibility = View.VISIBLE
                    mResetButton.visibility = View.GONE
                    // Stop (tap the ring) dismisses an expired timer; hide the ✕ meanwhile.
                    mDeleteButton.visibility =
                            if (showDeleteAndReset) View.INVISIBLE else View.GONE
                    mTimerText.isClickable = true
                    mTimerText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    ViewCompat.setAccessibilityDelegate(mTimerText, ClickAccessibilityDelegate(
                            context.getString(R.string.timer_stop)))
                }
                null -> { }
            }
        }
    }

    private val deleteVisibility: Int
        get() = if (showDeleteAndReset) View.VISIBLE else View.GONE

    private fun defaultLabel(lengthMillis: Long): String {
        val totalSeconds = lengthMillis / DateUtils.SECOND_IN_MILLIS
        val hours = totalSeconds / 3600
        val minutes = totalSeconds / 60 % 60
        val seconds = totalSeconds % 60
        val res = resources
        val parts = buildList {
            if (hours > 0) add(res.getString(R.string.timer_length_hours_short, hours))
            if (minutes > 0) add(res.getString(R.string.timer_length_minutes_short, minutes))
            if (seconds > 0 || isEmpty()) {
                add(res.getString(R.string.timer_length_seconds_short, seconds))
            }
        }
        return res.getString(R.string.timer_default_label, parts.joinToString(" "))
    }
}
