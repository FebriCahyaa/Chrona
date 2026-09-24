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

package com.febricahyaa.chrona.timer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.febricahyaa.chrona.LabelDialogFragment
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.Utils
import com.febricahyaa.chrona.data.DataModel
import com.febricahyaa.chrona.data.Timer
import com.febricahyaa.chrona.data.TimerListener
import com.febricahyaa.chrona.data.TimerStringFormatter
import com.febricahyaa.chrona.events.Events

/**
 * Shows every timer as a [TimerItem] card: a single timer fills the width, several timers share
 * a two-column grid of compact cards.
 */
internal class TimerAdapter(
    private val fragmentManager: () -> FragmentManager,
    private val onDelete: (Timer, View) -> Unit
) : RecyclerView.Adapter<TimerAdapter.TimerViewHolder>(), TimerListener {

    /** The holders currently bound to a timer; refreshed while timers run.  */
    private val mBoundHolders = mutableSetOf<TimerViewHolder>()

    init {
        setHasStableIds(true)
    }

    /** Lets a lone timer span both grid columns.  */
    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int = if (itemCount > 1) 1 else SPAN_COUNT
    }

    override fun getItemCount(): Int = timers.size

    override fun getItemId(position: Int): Long = timers[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val item = LayoutInflater.from(parent.context)
                .inflate(R.layout.timer_item, parent, false) as TimerItem
        item.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val gap = parent.resources.getDimensionPixelSize(R.dimen.timer_grid_gap)
        item.setPaddingRelative(gap, gap, gap, gap)
        return TimerViewHolder(item)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.timerId = timers[position].id
        holder.item.compact = itemCount > 1
        mBoundHolders.add(holder)
        holder.update()
    }

    override fun onViewRecycled(holder: TimerViewHolder) {
        mBoundHolders.remove(holder)
    }

    // Adding or removing a timer can switch every card between full width and compact.
    @SuppressLint("NotifyDataSetChanged")
    override fun timerAdded(timer: Timer) {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun timerRemoved(timer: Timer) {
        notifyDataSetChanged()
    }

    override fun timerUpdated(before: Timer, after: Timer) {
        mBoundHolders.firstOrNull { it.timerId == after.id }?.update()
    }

    /**
     * @return `true` if at least one timer is in a state requiring continuous updates
     */
    fun updateTime(): Boolean {
        var continuousUpdates = false
        for (holder in mBoundHolders) {
            continuousUpdates = continuousUpdates or holder.update()
        }
        return continuousUpdates
    }

    fun getTimer(index: Int): Timer = timers[index]

    private val timers: List<Timer>
        get() = DataModel.dataModel.timers

    inner class TimerViewHolder(val item: TimerItem) : RecyclerView.ViewHolder(item) {
        var timerId = -1

        private val timer: Timer?
            get() = DataModel.dataModel.getTimer(timerId)

        init {
            item.findViewById<View>(R.id.reset_add).setOnClickListener { v ->
                val t = timer ?: return@setOnClickListener
                if (t.isReset) {
                    return@setOnClickListener
                }
                DataModel.dataModel.addTimerMinute(t)
                Events.sendTimerEvent(R.string.action_add_minute, R.string.label_deskclock)
                // Must re-retrieve timer because old timer is no longer accurate.
                val remaining = timer?.remainingTime ?: 0L
                if (remaining > 0) {
                    Utils.announceForAccessibilityCompat(v, TimerStringFormatter.formatString(
                            v.context, R.string.timer_accessibility_one_minute_added, remaining,
                            true))
                }
            }
            item.findViewById<View>(R.id.timer_reset).setOnClickListener {
                timer?.let { DataModel.dataModel.resetOrDeleteTimer(it, R.string.label_deskclock) }
            }
            item.findViewById<View>(R.id.timer_delete).setOnClickListener { v ->
                timer?.let { onDelete(it, v) }
            }
            item.findViewById<View>(R.id.timer_label).setOnClickListener {
                timer?.let {
                    LabelDialogFragment.show(fragmentManager(), LabelDialogFragment.newInstance(it))
                }
            }
            val toggle = View.OnClickListener {
                val t = timer ?: return@OnClickListener
                when {
                    t.isPaused || t.isReset -> DataModel.dataModel.startTimer(t)
                    t.isRunning -> DataModel.dataModel.pauseTimer(t)
                    // The stop icon on an expired card.
                    t.isExpired || t.isMissed ->
                        DataModel.dataModel.resetOrDeleteTimer(t, R.string.label_deskclock)
                }
            }
            item.findViewById<View>(R.id.timer_time_text).setOnClickListener(toggle)
            item.findViewById<View>(R.id.timer_ring).setOnClickListener(toggle)
        }

        /** @return `true` iff the timer is in a state that requires continuous updates */
        fun update(): Boolean {
            val t = timer ?: return false
            item.update(t)
            return !t.isReset
        }
    }

    companion object {
        const val SPAN_COUNT = 2
    }
}
