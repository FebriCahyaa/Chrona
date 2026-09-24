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

package com.android.deskclock.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder

import com.android.deskclock.R

import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * A [ListPreference] shown as a Material 3 Expressive connected button group: each entry is a
 * toggle button in the row, so the choice is made inline instead of through a dialog.
 */
class ButtonGroupPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ListPreference(context, attrs) {

    init {
        layoutResource = R.layout.preference_button_group
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false

        val group = holder.findViewById(R.id.button_group) as MaterialButtonToggleGroup
        group.clearOnButtonCheckedListeners()
        group.removeAllViews()

        val labels = entries ?: return
        val values = entryValues ?: return
        val inflater = LayoutInflater.from(context)
        for (i in labels.indices) {
            val button = inflater.inflate(R.layout.preference_button_group_item, group, false)
                    as MaterialButton
            button.id = View.generateViewId()
            button.text = labels[i]
            button.tag = values[i].toString()
            group.addView(button, LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            if (values[i].toString() == value) {
                group.check(button.id)
            }
        }

        group.addOnButtonCheckedListener { g, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            val newValue = g.findViewById<View>(checkedId).tag as String
            if (newValue == value) {
                return@addOnButtonCheckedListener
            }
            if (callChangeListener(newValue)) {
                value = newValue
            } else {
                // Change rejected; restore the previous selection.
                notifyChanged()
            }
        }
    }

    /** Selection happens on the buttons; the row itself opens no dialog. */
    override fun onClick() {
    }
}
