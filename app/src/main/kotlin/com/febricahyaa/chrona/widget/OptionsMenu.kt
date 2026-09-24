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

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils

import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * Shows [labels] in a rounded Material 3 menu anchored over [anchor], with the entry at
 * [selected] highlighted, and reports the picked position to [onPick].
 */
fun showOptionsMenu(
    anchor: View,
    labels: Array<out CharSequence>,
    selected: Int,
    onPick: (Int) -> Unit
) {
    val context = anchor.context
    val res = context.resources
    val popup = ListPopupWindow(context, null, androidx.appcompat.R.attr.listPopupWindowStyle)
    popup.anchorView = anchor
    // A rounded M3 menu surface; the default popup background has square corners.
    popup.setBackgroundDrawable(MaterialShapeDrawable(ShapeAppearanceModel.builder()
            .setAllCornerSizes(res.getDimension(R.dimen.preference_menu_corner_radius))
            .build()).apply {
        fillColor = ColorStateList.valueOf(ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorSurfaceContainer))
        initializeElevationOverlay(context)
        elevation = res.getDimension(R.dimen.preference_menu_elevation)
    })
    popup.isModal = true
    popup.setDropDownGravity(Gravity.START)
    popup.setAdapter(object : ArrayAdapter<CharSequence>(
            context, R.layout.preference_menu_item, labels) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = (convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.preference_menu_item, parent, false)) as TextView
            view.text = labels[position]
            view.isActivated = position == selected
            return view
        }
    })
    popup.setContentWidth(res.getDimensionPixelSize(R.dimen.preference_menu_width))
    popup.verticalOffset = -anchor.height
    popup.setOnItemClickListener { _, _, position, _ ->
        popup.dismiss()
        onPick(position)
    }
    popup.show()
    if (selected >= 0) {
        popup.listView?.setSelection(selected)
    }
}
