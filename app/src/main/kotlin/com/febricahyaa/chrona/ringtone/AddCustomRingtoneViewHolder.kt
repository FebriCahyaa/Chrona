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

package com.febricahyaa.chrona.ringtone

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.febricahyaa.chrona.ItemAdapter.ItemViewHolder
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.ThemeUtils

internal class AddCustomRingtoneViewHolder private constructor(itemView: View)
    : ItemViewHolder<AddCustomRingtoneHolder>(itemView), View.OnClickListener {

    init {
        itemView.setOnClickListener(this)
        val selectedView = itemView.findViewById<View>(R.id.sound_image_selected)
        selectedView.visibility = View.GONE
        val nameView = itemView.findViewById<View>(R.id.ringtone_name) as TextView
        nameView.text = itemView.context.getString(R.string.add_new_sound)
        // "Add new" is a plus on a primary circle.
        val context = itemView.context
        val imageView = itemView.findViewById<View>(R.id.ringtone_image) as ImageView
        imageView.setImageResource(R.drawable.ic_add_white_24dp)
        imageView.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ThemeUtils.resolveColor(context, androidx.appcompat.R.attr.colorPrimary))
        }
        imageView.imageTintList = ColorStateList.valueOf(ThemeUtils.resolveColor(context,
                com.google.android.material.R.attr.colorOnPrimary))
    }

    override fun onClick(view: View) {
        notifyItemClicked(CLICK_ADD_NEW)
    }

    class Factory internal constructor(private val mInflater: LayoutInflater)
        : ItemViewHolder.Factory {
        override fun createViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<*> {
            val itemView =
                    mInflater.inflate(R.layout.ringtone_item_sound, parent, false)
            return AddCustomRingtoneViewHolder(itemView)
        }
    }

    companion object {
        const val VIEW_TYPE_ADD_NEW = Int.MIN_VALUE
        const val CLICK_ADD_NEW = VIEW_TYPE_ADD_NEW
    }
}