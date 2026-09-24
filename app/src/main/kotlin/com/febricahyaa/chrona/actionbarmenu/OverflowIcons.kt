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

package com.febricahyaa.chrona.actionbarmenu

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

import com.febricahyaa.chrona.ThemeUtils

/**
 * Shows [iconRes] before this item's title in the overflow menu, which otherwise draws no icons,
 * as the M3 Expressive clock's overflow menu does.
 */
fun MenuItem.withOverflowIcon(context: Context, @DrawableRes iconRes: Int): MenuItem {
    val icon = AppCompatResources.getDrawable(context, iconRes)?.mutate() ?: return this
    val size = (22 * context.resources.displayMetrics.density).toInt()
    icon.setBounds(0, 0, size, size)
    icon.setTint(ThemeUtils.resolveColor(context,
            androidx.appcompat.R.attr.colorPrimary))
    val plainTitle = title
    val text = SpannableStringBuilder("   ").append(plainTitle)
    text.setSpan(ImageSpan(icon, ImageSpan.ALIGN_CENTER), 0, 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    title = text
    titleCondensed = plainTitle
    return this
}
