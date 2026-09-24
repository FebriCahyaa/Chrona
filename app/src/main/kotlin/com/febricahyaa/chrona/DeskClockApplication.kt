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

package com.febricahyaa.chrona

import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

import com.febricahyaa.chrona.controller.Controller
import com.febricahyaa.chrona.data.DataModel
import com.febricahyaa.chrona.events.LogEventTracker
import com.febricahyaa.chrona.settings.SettingsActivity
import com.febricahyaa.chrona.uidata.UiDataModel

class DeskClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val applicationContext = applicationContext
        val prefs = getDefaultSharedPreferences(applicationContext)

        // The UI is dark-only for now, so always take the dark dynamic scheme; the default
        // DayNight overlay would apply light-mode colors whenever the system is in light mode.
        // Seeding from the wallpaper's main color uses Material's content-based scheme, which
        // keeps the color's full chroma (more vibrant than the system's tonal-spot palette).
        val dynamicColors = DynamicColorsOptions.Builder()
                .setThemeOverlay(
                        com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_Dark)
        wallpaperSeedColor()?.let { dynamicColors.setContentBasedSource(it) }
        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColors.build())

        // Registered after DynamicColors so the black surfaces win over the dynamic ones.
        registerActivityLifecycleCallbacks(AmoledThemeApplier(prefs))

        DataModel.dataModel.init(applicationContext, prefs)
        UiDataModel.uiDataModel.init(applicationContext, prefs)
        Controller.getController().setContext(applicationContext)
        Controller.getController().addEventTracker(LogEventTracker(applicationContext))
    }

    /** @return the wallpaper's primary color, or null if it is unavailable */
    private fun wallpaperSeedColor(): Int? = try {
        WallpaperManager.getInstance(this)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.primaryColor
                ?.toArgb()
    } catch (e: RuntimeException) {
        LogUtils.w("Unable to read wallpaper colors: $e")
        null
    }

    /** Applies the pure-black AMOLED overlay to app activities when that setting is on. */
    private class AmoledThemeApplier(
        private val prefs: SharedPreferences
    ) : ActivityLifecycleCallbacks {
        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is AppCompatActivity &&
                    prefs.getBoolean(SettingsActivity.KEY_AMOLED_THEME, false)) {
                activity.theme.applyStyle(R.style.ThemeOverlay_Chrona_Amoled, true)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
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