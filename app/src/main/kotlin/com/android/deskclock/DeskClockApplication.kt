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

package com.android.deskclock

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

import com.android.deskclock.controller.Controller
import com.android.deskclock.data.DataModel
import com.android.deskclock.events.LogEventTracker
import com.android.deskclock.uidata.UiDataModel

class DeskClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // The UI is dark-only for now, so always take the dark dynamic scheme; the default
        // DayNight overlay would apply light-mode colors whenever the system is in light mode.
        DynamicColors.applyToActivitiesIfAvailable(this, DynamicColorsOptions.Builder()
                .setThemeOverlay(
                        com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_Dark)
                .build())

        val applicationContext = applicationContext
        val prefs = getDefaultSharedPreferences(applicationContext)

        DataModel.dataModel.init(applicationContext, prefs)
        UiDataModel.uiDataModel.init(applicationContext, prefs)
        Controller.getController().setContext(applicationContext)
        Controller.getController().addEventTracker(LogEventTracker(applicationContext))
    }

    companion object {
        /**
         * Returns the default [SharedPreferences] instance from the device protected storage area,
         * migrating any existing preferences from credential protected storage first.
         */
        private fun getDefaultSharedPreferences(context: Context): SharedPreferences {
            // Same file name android.preference/androidx.preference PreferenceManager use.
            val name = "${context.packageName}_preferences"
            val storageContext = context.createDeviceProtectedStorageContext()
            if (!storageContext.moveSharedPreferencesFrom(context, name)) {
                LogUtils.wtf("Failed to migrate shared preferences")
            }
            return storageContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
    }
}