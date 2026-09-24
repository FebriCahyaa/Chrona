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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.Menu.NONE
import android.view.MenuItem

import com.febricahyaa.chrona.R

/**
 * [MenuItemController] that opens the project's issue tracker to send feedback.
 */
class FeedbackMenuItemController(private val context: Context) : MenuItemController {

    override val id: Int = R.id.menu_item_feedback

    override fun onCreateOptionsItem(menu: Menu) {
        menu.add(NONE, id, NONE, R.string.menu_item_feedback)
                .withOverflowIcon(context, R.drawable.ic_feedback_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    override fun onPrepareOptionsItem(item: MenuItem) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(FEEDBACK_URL)))
        } catch (ignored: ActivityNotFoundException) {
            // No browser; nothing to open.
        }
        return true
    }

    private companion object {
        const val FEEDBACK_URL = "https://github.com/FebriCahyaa/Chrona/issues"
    }
}
