/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorldClockFavoritePersistenceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    @Test
    fun zoneIdFavoritesSurviveRepositoryRecreation() {
        val repository = SharedPreferencesWorldClockRepository(context)
        val expected = setOf("America/New_York", "Asia/Tokyo", "Pacific/Auckland")

        repository.saveFavorites(expected)

        val recreated = SharedPreferencesWorldClockRepository(context)

        assertEquals(expected, recreated.loadFavorites())
    }

    @Test
    fun favoriteTogglePersistsAcrossViewModelRecreationAndDeletion() {
        val firstViewModel = WorldClockViewModel(SharedPreferencesWorldClockRepository(context))

        waitUntil("Default World Clock seed should load") {
            firstViewModel.state.value.items.any { it.zoneId == "Pacific/Auckland" }
        }

        firstViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland zone should be persisted as a favorite") {
            "Pacific/Auckland" in SharedPreferencesWorldClockRepository(context).loadFavorites()
        }

        val secondViewModel = WorldClockViewModel(SharedPreferencesWorldClockRepository(context))
        waitUntil("Recreated ViewModel should restore Auckland") {
            "Pacific/Auckland" in secondViewModel.state.value.favorites
        }

        assertTrue(
            "Pacific/Auckland must survive ViewModel recreation",
            "Pacific/Auckland" in secondViewModel.state.value.favorites,
        )

        secondViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland should be removed from persistence") {
            "Pacific/Auckland" !in SharedPreferencesWorldClockRepository(context).loadFavorites()
        }

        secondViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland should be persisted again") {
            "Pacific/Auckland" in SharedPreferencesWorldClockRepository(context).loadFavorites()
        }

        val auckland = secondViewModel.state.value.items.first { it.zoneId == "Pacific/Auckland" }
        secondViewModel.remove(auckland)
        waitUntil("Removing a city should remove its favorite and item") {
            "Pacific/Auckland" !in SharedPreferencesWorldClockRepository(context).loadFavorites() &&
                SharedPreferencesWorldClockRepository(context).load().none { it.zoneId == "Pacific/Auckland" }
        }

        val thirdViewModel = WorldClockViewModel(SharedPreferencesWorldClockRepository(context))
        waitUntil("Recreated ViewModel should not restore a removed city") {
            thirdViewModel.state.value.items.none { it.zoneId == "Pacific/Auckland" } &&
                "Pacific/Auckland" !in thirdViewModel.state.value.favorites
        }
    }

    @Test
    fun legacyCityNameFavoriteIsMigratedToStableZoneId() {
        val repository = SharedPreferencesWorldClockRepository(context)
        repository.save(AppDefaults.defaultWorldClocks())
        repository.saveFavorites(setOf("Tokyo"))

        val viewModel = WorldClockViewModel(SharedPreferencesWorldClockRepository(context))

        waitUntil("Legacy city name should migrate to Asia/Tokyo") {
            "Asia/Tokyo" in viewModel.state.value.favorites
        }

        assertTrue("Legacy city-name favorite should be normalized", "Asia/Tokyo" in viewModel.state.value.favorites)
        assertTrue("Canonical city favorite should be persisted", "Asia/Tokyo" in SharedPreferencesWorldClockRepository(context).loadFavorites())
    }

    private fun clearPreferences() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun waitUntil(description: String, timeoutMs: Long = 5_000L, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (!condition() && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(25L)
        }
        assertTrue(description, condition())
    }

    private companion object {
        const val PREFS_NAME = "chrona_world_clocks"
    }
}
