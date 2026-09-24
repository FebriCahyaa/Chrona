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
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference

import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils
import com.febricahyaa.chrona.data.DataModel
import com.febricahyaa.chrona.widget.AlarmDismissControl

import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * "Dismiss alarm with": opens the "Choose how to dismiss" dialog, which previews the ringing
 * alarm control live and offers tap, slide or swipe, as in the M3 Expressive clock.
 */
class DismissStylePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {

    init {
        updateSummary()
    }

    override fun onClick() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_dismiss_style, null)
        val preview = view.findViewById<AlarmDismissControl>(R.id.dismiss_preview)
        view.findViewById<View>(R.id.dismiss_preview_frame).background = GradientDrawable().apply {
            cornerRadius = 28 * context.resources.displayMetrics.density
            setColor(ThemeUtils.resolveColor(context,
                    com.google.android.material.R.attr.colorSurfaceContainerLow))
        }

        var selected = DataModel.dataModel.alarmDismissStyle
        val tiles = listOf(
                Triple(R.id.tile_tap, AlarmDismissControl.STYLE_TAP, R.string.dismiss_style_tap),
                Triple(R.id.tile_slide, AlarmDismissControl.STYLE_SLIDE,
                        R.string.dismiss_style_slide),
                Triple(R.id.tile_swipe, AlarmDismissControl.STYLE_SWIPE,
                        R.string.dismiss_style_swipe))
        val strokePx = (3 * context.resources.displayMetrics.density).toInt()

        fun render() {
            preview.style = selected
            for ((tileId, style, _) in tiles) {
                val tile = view.findViewById<View>(tileId)
                val isSelected = style == selected
                tile.findViewById<MaterialCardView>(R.id.tile_card).strokeWidth =
                        if (isSelected) strokePx else 0
                tile.findViewById<View>(R.id.tile_check).visibility =
                        if (isSelected) View.VISIBLE else View.GONE
                tile.isSelected = isSelected
            }
        }

        for ((tileId, style, labelRes) in tiles) {
            val tile = view.findViewById<View>(tileId)
            tile.findViewById<ImageView>(R.id.tile_image).setImageResource(when (style) {
                AlarmDismissControl.STYLE_TAP -> R.drawable.tile_dismiss_tap
                AlarmDismissControl.STYLE_SWIPE -> R.drawable.tile_dismiss_swipe
                else -> R.drawable.tile_dismiss_slide
            })
            tile.findViewById<TextView>(R.id.tile_label).setText(labelRes)
            tile.contentDescription = context.getString(labelRes)
            tile.setOnClickListener {
                selected = style
                render()
            }
        }
        render()

        MaterialAlertDialogBuilder(context)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (callChangeListener(selected)) {
                        DataModel.dataModel.alarmDismissStyle = selected
                        updateSummary()
                    }
                }
                .show()
    }

    private fun updateSummary() {
        summary = context.getString(when (DataModel.dataModel.alarmDismissStyle) {
            AlarmDismissControl.STYLE_TAP -> R.string.dismiss_style_tap
            AlarmDismissControl.STYLE_SWIPE -> R.string.dismiss_style_swipe
            else -> R.string.dismiss_style_slide
        })
    }
}
