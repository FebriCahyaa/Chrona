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

package com.febricahyaa.chrona.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder

import com.febricahyaa.chrona.widget.showOptionsMenu

/**
 * A [ListPreference] that shows its choices in a Material 3 menu anchored to the row, as the
 * M3 Expressive clock does, instead of a dialog. The selected entry is highlighted and shown as
 * the summary.
 */
class MenuListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ListPreference(context, attrs) {

    /** The row's summary, which the menu opens over.  */
    private var mAnchor: View? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        mAnchor = holder.findViewById(android.R.id.summary)?.takeIf { it.visibility == View.VISIBLE }
                ?: holder.findViewById(android.R.id.title)
                ?: holder.itemView
    }

    override fun setValue(value: String?) {
        super.setValue(value)
        summary = entry
    }

    override fun onClick() {
        val anchor = mAnchor ?: return
        val labels = entries ?: return
        val values = entryValues ?: return
        val selected = findIndexOfValue(value)

        showOptionsMenu(anchor, labels, selected) { position ->
            val newValue = values[position].toString()
            if (newValue != value && callChangeListener(newValue)) {
                value = newValue
            }
        }
    }
}
