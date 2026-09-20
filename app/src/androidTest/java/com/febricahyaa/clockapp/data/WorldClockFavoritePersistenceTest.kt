/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data

import android.content.Context
import android.os.SystemClock
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.febricahyaa.clockapp.core.config.AppDefaults
import com.febricahyaa.clockapp.data.local.ChronaDatabase
import com.febricahyaa.clockapp.data.local.RoomWorldClockRepository
import com.febricahyaa.clockapp.ui.viewmodel.WorldClockViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorldClockFavoritePersistenceTest {
    private lateinit var context: Context
    private lateinit var database: ChronaDatabase
    private lateinit var migration: StorageMigrationCoordinator

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, ChronaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        migration = StorageMigrationCoordinator(context, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun repository(): RoomWorldClockRepository = RoomWorldClockRepository(database, migration)

    @Test
    fun zoneIdFavoritesSurviveRepositoryRecreation() = runBlocking {
        val first = repository()
        val expectedItems = AppDefaults.defaultWorldClocks()
        first.save(expectedItems)
        val expected = setOf("America/New_York", "Asia/Tokyo", "Pacific/Auckland")
        first.saveFavorites(expected)

        val recreated = repository()
        assertEquals(expected, recreated.loadFavorites())
    }

    @Test
    fun favoriteTogglePersistsAcrossViewModelRecreationAndDeletion() {
        runBlocking { repository().save(AppDefaults.defaultWorldClocks()) }
        val firstViewModel = WorldClockViewModel(repository())

        waitUntil("Default World Clock seed should load") {
            firstViewModel.state.value.items.any { it.zoneId == "Pacific/Auckland" }
        }

        firstViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland zone should be persisted as a favorite") {
            runBlocking { "Pacific/Auckland" in repository().loadFavorites() }
        }

        val secondViewModel = WorldClockViewModel(repository())
        waitUntil("Recreated ViewModel should restore Auckland") {
            "Pacific/Auckland" in secondViewModel.state.value.favorites
        }

        assertTrue("Pacific/Auckland must survive ViewModel recreation", "Pacific/Auckland" in secondViewModel.state.value.favorites)

        secondViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland should be removed from persistence") {
            runBlocking { "Pacific/Auckland" !in repository().loadFavorites() }
        }

        secondViewModel.toggleFavorite("Pacific/Auckland")
        waitUntil("Auckland should be persisted again") {
            runBlocking { "Pacific/Auckland" in repository().loadFavorites() }
        }

        val auckland = secondViewModel.state.value.items.first { it.zoneId == "Pacific/Auckland" }
        secondViewModel.remove(auckland)
        waitUntil("Removing a city should remove its favorite and item") {
            runBlocking {
                "Pacific/Auckland" !in repository().loadFavorites() &&
                    repository().load().none { it.zoneId == "Pacific/Auckland" }
            }
        }

        val thirdViewModel = WorldClockViewModel(repository())
        waitUntil("Recreated ViewModel should not restore a removed city") {
            thirdViewModel.state.value.items.none { it.zoneId == "Pacific/Auckland" } &&
                "Pacific/Auckland" !in thirdViewModel.state.value.favorites
        }
    }

    @Test
    fun legacyCityNameFavoriteIsMigratedToStableZoneId() = runBlocking {
        val first = repository()
        first.save(AppDefaults.defaultWorldClocks())
        first.saveFavorites(setOf("Asia/Tokyo"))

        val viewModel = WorldClockViewModel(repository())
        waitUntil("Canonical Tokyo favorite should be observed") {
            "Asia/Tokyo" in viewModel.state.value.favorites
        }
        assertTrue("Canonical city favorite should remain a zone ID", "Asia/Tokyo" in viewModel.state.value.favorites)
    }

    private fun waitUntil(description: String, timeoutMs: Long = 5_000L, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (!condition() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(25L)
        assertTrue(description, condition())
    }
}
