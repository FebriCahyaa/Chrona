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
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils
import com.febricahyaa.chrona.TimerTextController
import com.febricahyaa.chrona.Utils.ClickAccessibilityDelegate
import com.febricahyaa.chrona.data.Timer

import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * This view is a visual representation of a [Timer]: a Material 3 Expressive card with the
 * label and a delete button, a progress ring holding the time and a state icon, and +1:00 /
 * reset buttons. The card turns primary-container colored once the timer expires.
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
    private lateinit var mAddMinuteButton: View

    /** Resets the timer.  */
    private lateinit var mResetButton: View

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
        applyCardColors(expired = false)
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

        // Update visibility of things that may blink.
        val blinkOff = SystemClock.elapsedRealtime() % 1000 < 500
        val hideCircle = (timer.isExpired || timer.isMissed) && blinkOff
        mCircleView.visibility = if (hideCircle) View.INVISIBLE else View.VISIBLE
        if (!hideCircle) {
            // Update the progress of the circle.
            mCircleView.update(timer)
        }
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
                    mAddMinuteButton.visibility =
                            if (timer.isPaused) View.VISIBLE else View.GONE
                    mResetButton.visibility = if (timer.isPaused) View.VISIBLE else View.GONE
                    mTimerText.isClickable = true
                    mTimerText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    ViewCompat.setAccessibilityDelegate(mTimerText, ClickAccessibilityDelegate(
                            context.getString(R.string.timer_start), true))
                }
                Timer.State.RUNNING -> {
                    applyCardColors(expired = false)
                    mStateIcon.setImageResource(R.drawable.ic_pause_24dp)
                    mAddMinuteButton.visibility = View.VISIBLE
                    mResetButton.visibility = View.VISIBLE
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
                    mTimerText.isClickable = true
                    mTimerText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    ViewCompat.setAccessibilityDelegate(mTimerText, ClickAccessibilityDelegate(
                            context.getString(R.string.timer_stop)))
                }
                null -> { }
            }
        }
    }
}
