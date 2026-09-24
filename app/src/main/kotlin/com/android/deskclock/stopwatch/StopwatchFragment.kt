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

package com.android.deskclock.stopwatch

import android.R.attr.state_activated
import android.R.attr.state_pressed
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

import com.android.deskclock.DeskClockFragment
import com.android.deskclock.FabContainer
import com.android.deskclock.FabContainer.UpdateFabFlag
import com.android.deskclock.data.DataModel
import com.android.deskclock.data.Lap
import com.android.deskclock.data.Stopwatch
import com.android.deskclock.data.StopwatchListener
import com.android.deskclock.events.Events
import com.android.deskclock.LogUtils
import com.android.deskclock.R
import com.android.deskclock.StopwatchTextController
import com.android.deskclock.ThemeUtils
import com.android.deskclock.Utils
import com.android.deskclock.uidata.TabListener
import com.android.deskclock.uidata.UiDataModel

import com.google.android.material.button.MaterialButton

import kotlin.math.max

/**
 * Fragment that shows the stopwatch and recorded laps.
 *
 * Material 3 Expressive layout: a large time readout, laps as a horizontal row of cards and a
 * stack of full-width pill buttons (start/pause, reset, lap/share). The buttons live in this
 * fragment, so the shared fab and side buttons of [com.android.deskclock.DeskClock] are hidden
 * while this tab is shown.
 */
class StopwatchFragment : DeskClockFragment(UiDataModel.Tab.STOPWATCH) {

    /** Keep the screen on when this tab is selected.  */
    private val mTabWatcher: TabListener = TabWatcher()

    /** Scheduled to update the stopwatch time and current lap time while stopwatch is running.  */
    private val mTimeUpdateRunnable: Runnable = TimeUpdateRunnable()

    /** Updates the user interface in response to stopwatch changes.  */
    private val mStopwatchWatcher: StopwatchListener = StopwatchWatcher()

    /** The data source for [.mLapsList].  */
    private lateinit var mLapsAdapter: LapsAdapter

    /** The View containing both TextViews of the stopwatch.  */
    private lateinit var mStopwatchWrapper: View

    /** Displays the recorded lap times, newest at the end of the row.  */
    private lateinit var mLapsList: RecyclerView

    /** Displays the current stopwatch time (seconds and above only).  */
    private lateinit var mMainTimeText: TextView

    /** Displays the current stopwatch time (hundredths only).  */
    private lateinit var mHundredthsTimeText: TextView

    /** Formats and displays the text in the stopwatch.  */
    private lateinit var mStopwatchTextController: StopwatchTextController

    /** Start / pause. */
    private lateinit var mPrimaryButton: MaterialButton

    /** Reset. */
    private lateinit var mResetButton: MaterialButton

    /** Lap while running, share while paused. */
    private lateinit var mLapButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        mLapsAdapter = LapsAdapter(requireActivity())

        val v: View = inflater.inflate(R.layout.stopwatch_fragment, container, false)
        mLapsList = v.findViewById(R.id.laps_list) as RecyclerView
        (mLapsList.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        // Adapter position 0 is the current lap; reverse layout keeps it at the end of the row.
        mLapsList.setLayoutManager(LinearLayoutManager(requireActivity(),
                LinearLayoutManager.HORIZONTAL, true))
        mLapsList.setAdapter(mLapsAdapter)
        setTabScrolledToTop(true)

        // Timer text serves as a virtual start/stop button.
        mMainTimeText = v.findViewById(R.id.stopwatch_time_text) as TextView
        mHundredthsTimeText = v.findViewById(R.id.stopwatch_hundredths_text) as TextView
        mStopwatchTextController = StopwatchTextController(mMainTimeText, mHundredthsTimeText)
        mStopwatchWrapper = v.findViewById(R.id.stopwatch_time_wrapper)
        mStopwatchWrapper.setOnClickListener { toggleStopwatchState() }

        mPrimaryButton = v.findViewById(R.id.stopwatch_primary_button)
        mResetButton = v.findViewById(R.id.stopwatch_reset_button)
        mLapButton = v.findViewById(R.id.stopwatch_lap_button)
        mPrimaryButton.setOnClickListener { toggleStopwatchState() }
        mResetButton.setOnClickListener { doReset() }
        mLapButton.setOnClickListener {
            when (stopwatch.state) {
                Stopwatch.State.RUNNING -> doAddLap()
                Stopwatch.State.PAUSED -> doShare()
                else -> {
                }
            }
        }

        DataModel.dataModel.addStopwatchListener(mStopwatchWatcher)

        val c: Context = mMainTimeText.getContext()
        val colorPrimary = ThemeUtils.resolveColor(c,
                androidx.appcompat.R.attr.colorPrimary)
        val colorOnSurface = ThemeUtils.resolveColor(c,
                com.google.android.material.R.attr.colorOnSurface)
        val timeTextColor =
                ColorStateList(
                        arrayOf(intArrayOf(-state_activated, -state_pressed), intArrayOf()),
                        intArrayOf(colorOnSurface, colorPrimary)
                )
        mMainTimeText.setTextColor(timeTextColor)
        mHundredthsTimeText.setTextColor(timeTextColor)

        return v
    }

    override fun onStart() {
        super.onStart()

        val activity: Activity = requireActivity()
        val intent: Intent? = activity.getIntent()
        if (intent != null) {
            val action: String? = intent.getAction()
            if (StopwatchService.Companion.ACTION_START_STOPWATCH == action) {
                DataModel.dataModel.startStopwatch()
                // Consume the intent
                activity.setIntent(null)
            } else if (StopwatchService.Companion.ACTION_PAUSE_STOPWATCH == action) {
                DataModel.dataModel.pauseStopwatch()
                // Consume the intent
                activity.setIntent(null)
            }
        }

        // Conservatively assume the data in the adapter has changed while the fragment was paused.
        mLapsAdapter.notifyDataSetChanged()

        // Synchronize the user interface with the data model.
        updateUI(FabContainer.FAB_AND_BUTTONS_IMMEDIATE)

        // Start watching for page changes away from this fragment.
        UiDataModel.uiDataModel.addTabListener(mTabWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Stop all updates while the fragment is not visible.
        stopUpdatingTime()

        // Stop watching for page changes away from this fragment.
        UiDataModel.uiDataModel.removeTabListener(mTabWatcher)

        // Release the wake lock if it is currently held.
        releaseWakeLock()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DataModel.dataModel.removeStopwatchListener(mStopwatchWatcher)
    }

    /** Hardware keyboard / accessibility path through the shared fab still toggles. */
    override fun onFabClick(fab: ImageView) {
        toggleStopwatchState()
    }

    /** The shared fab is replaced by [mPrimaryButton] on this tab. */
    override fun onUpdateFab(fab: ImageView) {
        fab.setVisibility(INVISIBLE)
    }

    override fun onMorphFab(fab: ImageView) {
        fab.setVisibility(INVISIBLE)
    }

    /** The shared side buttons are replaced by [mResetButton] and [mLapButton] on this tab. */
    override fun onUpdateFabButtons(left: Button, right: Button) {
        left.setVisibility(INVISIBLE)
        right.setVisibility(INVISIBLE)
        left.setClickable(false)
        right.setClickable(false)
    }

    /**
     * Updates the stacked buttons: primary start (primary color) or pause (tertiary container),
     * reset once the stopwatch has run, and lap (running) or share (paused).
     */
    private fun updateButtons() {
        val context: Context = requireContext()
        val state = stopwatch.state
        val running = state == Stopwatch.State.RUNNING

        val fillAttr: Int
        val textAttr: Int
        if (running) {
            fillAttr = com.google.android.material.R.attr.colorTertiaryContainer
            textAttr = com.google.android.material.R.attr.colorOnTertiaryContainer
            mPrimaryButton.setText(R.string.sw_pause_button)
        } else {
            fillAttr = androidx.appcompat.R.attr.colorPrimary
            textAttr = com.google.android.material.R.attr.colorOnPrimary
            mPrimaryButton.setText(R.string.sw_start_button)
        }
        mPrimaryButton.backgroundTintList =
                ColorStateList.valueOf(ThemeUtils.resolveColor(context, fillAttr))
        mPrimaryButton.setTextColor(ThemeUtils.resolveColor(context, textAttr))

        mResetButton.setVisibility(if (state == Stopwatch.State.RESET) GONE else VISIBLE)

        when (state) {
            Stopwatch.State.RUNNING -> {
                mLapButton.setText(R.string.sw_lap_button)
                mLapButton.setVisibility(if (canRecordMoreLaps()) VISIBLE else GONE)
            }
            Stopwatch.State.PAUSED -> {
                mLapButton.setText(R.string.sw_share_button)
                mLapButton.setVisibility(VISIBLE)
            }
            else -> mLapButton.setVisibility(GONE)
        }
        mLapButton.setEnabled(true)
    }

    /**
     * Start the stopwatch.
     */
    private fun doStart() {
        Events.sendStopwatchEvent(R.string.action_start, R.string.label_deskclock)
        DataModel.dataModel.startStopwatch()
    }

    /**
     * Pause the stopwatch.
     */
    private fun doPause() {
        Events.sendStopwatchEvent(R.string.action_pause, R.string.label_deskclock)
        DataModel.dataModel.pauseStopwatch()
    }

    /**
     * Reset the stopwatch.
     */
    private fun doReset() {
        Events.sendStopwatchEvent(R.string.action_reset, R.string.label_deskclock)
        DataModel.dataModel.resetStopwatch()
        mMainTimeText.setAlpha(1f)
        mHundredthsTimeText.setAlpha(1f)
    }

    /**
     * Send stopwatch time and lap times to an external sharing application.
     */
    private fun doShare() {
        // Disable the share button to avoid double-taps.
        mLapButton.setEnabled(false)

        val subjects: Array<String> = getResources().getStringArray(R.array.sw_share_strings)
        val subject = subjects[(Math.random() * subjects.size).toInt()]
        val text = mLapsAdapter.shareText

        @SuppressLint("InlinedApi")
        val shareIntent: Intent = Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain")

        val context: Context = requireActivity()
        val title: String = context.getString(R.string.sw_share_button)
        val shareChooserIntent: Intent = Intent.createChooser(shareIntent, title)
        try {
            context.startActivity(shareChooserIntent)
        } catch (anfe: ActivityNotFoundException) {
            LogUtils.e("Cannot share lap data because no suitable receiving Activity exists")
        }
        // Re-enable once the chooser is up (or failed to open).
        mLapButton.post { mLapButton.setEnabled(true) }
    }

    /**
     * Record and add a new lap ending now.
     */
    private fun doAddLap() {
        Events.sendStopwatchEvent(R.string.action_lap, R.string.label_deskclock)

        // Record a new lap.
        val lap = mLapsAdapter.addLap() ?: return

        // The lap limit may have been reached.
        updateButtons()
        if (lap.lapNumber == 1) {
            // Child views from prior lap sets hang around and blit to the screen when adding the
            // first lap of the subsequent lap set. Remove those superfluous children here manually
            // to ensure they aren't seen as the first lap is drawn.
            mLapsList.removeAllViewsInLayout()

            // Recording the first lap transitions the UI to display the laps list.
            showOrHideLaps(false)
        }

        // Ensure the newly added lap is visible on screen.
        mLapsList.scrollToPosition(0)
    }

    /**
     * Show or hide the list of laps.
     */
    private fun showOrHideLaps(clearLaps: Boolean) {
        val sceneRoot: ViewGroup = getView() as ViewGroup? ?: return

        TransitionManager.beginDelayedTransition(sceneRoot)

        if (clearLaps) {
            mLapsAdapter.clearLaps()
        }

        val lapsVisible = mLapsAdapter.getItemCount() > 0
        mLapsList.setVisibility(if (lapsVisible) VISIBLE else GONE)
    }

    private fun adjustWakeLock() {
        val appInForeground = DataModel.dataModel.isApplicationInForeground
        if (stopwatch.isRunning && isTabSelected && appInForeground) {
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            releaseWakeLock()
        }
    }

    private fun releaseWakeLock() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Either pause or start the stopwatch based on its current state.
     */
    private fun toggleStopwatchState() {
        if (stopwatch.isRunning) {
            doPause()
        } else {
            doStart()
        }
    }

    private val stopwatch: Stopwatch
        get() = DataModel.dataModel.stopwatch

    private fun canRecordMoreLaps(): Boolean = DataModel.dataModel.canAddMoreLaps()

    /**
     * Post the first runnable to update times within the UI. It will reschedule itself as needed.
     */
    private fun startUpdatingTime() {
        // Ensure only one copy of the runnable is ever scheduled by first stopping updates.
        stopUpdatingTime()
        mMainTimeText.post(mTimeUpdateRunnable)
    }

    /**
     * Remove the runnable that updates times within the UI.
     */
    private fun stopUpdatingTime() {
        mMainTimeText.removeCallbacks(mTimeUpdateRunnable)
    }

    /**
     * Update all time displays based on a single snapshot of the stopwatch progress: the large
     * stopwatch time and the current lap card.
     */
    private fun updateTime() {
        val stopwatch = stopwatch
        val totalTime = stopwatch.totalTime
        mStopwatchTextController.setTimeString(totalTime)

        if (!stopwatch.isReset) {
            mLapsAdapter.updateCurrentLap(mLapsList, totalTime)
        }
    }

    /**
     * Synchronize the UI state with the model data.
     */
    private fun updateUI(@UpdateFabFlag updateTypes: Int) {
        adjustWakeLock()

        // Draw the latest stopwatch and current lap times.
        updateTime()
        val stopwatch = stopwatch
        if (!stopwatch.isReset) {
            startUpdatingTime()
        }

        // Adjust the visibility of the list of laps.
        showOrHideLaps(stopwatch.isReset)

        updateButtons()

        // Keep the shared fab/buttons hidden on this tab.
        updateFab(updateTypes)
    }

    /**
     * This runnable periodically updates times throughout the UI. It stops these updates when the
     * stopwatch is no longer running.
     */
    private inner class TimeUpdateRunnable : Runnable {
        override fun run() {
            val startTime = Utils.now()
            updateTime()

            // Blink text iff the stopwatch is paused and not pressed.
            val stopwatch = stopwatch
            val blink = (stopwatch.isPaused && startTime % 1000 < 500 &&
                    !mStopwatchWrapper.isPressed())

            if (blink) {
                mMainTimeText.setAlpha(0f)
                mHundredthsTimeText.setAlpha(0f)
            } else {
                mMainTimeText.setAlpha(1f)
                mHundredthsTimeText.setAlpha(1f)
            }

            if (!stopwatch.isReset) {
                val period = (if (stopwatch.isPaused) {
                    REDRAW_PERIOD_PAUSED
                } else {
                    REDRAW_PERIOD_RUNNING
                }).toLong()
                val endTime = Utils.now()
                val delay: Long = max(0, startTime + period - endTime)
                mMainTimeText.postDelayed(this, delay)
            }
        }
    }

    /**
     * Acquire or release the wake lock based on the tab state.
     */
    private inner class TabWatcher : TabListener {
        override fun selectedTabChanged(
            oldSelectedTab: UiDataModel.Tab,
            newSelectedTab: UiDataModel.Tab
        ) {
            adjustWakeLock()
        }
    }

    /**
     * Update the user interface in response to a stopwatch change.
     */
    private inner class StopwatchWatcher : StopwatchListener {
        override fun stopwatchUpdated(before: Stopwatch, after: Stopwatch) {
            if (after.isReset) {
                // Ensure the drop shadow is hidden when the stopwatch is reset.
                setTabScrolledToTop(true)
            }
            if (DataModel.dataModel.isApplicationInForeground) {
                updateUI(FabContainer.BUTTONS_IMMEDIATE)
            }
        }

        override fun lapAdded(lap: Lap) {
        }
    }

    companion object {
        /** Milliseconds between redraws while running.  */
        private const val REDRAW_PERIOD_RUNNING = 25

        /** Milliseconds between redraws while paused.  */
        private const val REDRAW_PERIOD_PAUSED = 500
    }
}
