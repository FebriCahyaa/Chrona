/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.febricahyaa.clockapp.data.AlarmRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChronaHiltGraphTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var alarmRepository: AlarmRepository

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun singletonGraphProvidesAlarmRepository() {
        assertNotNull(alarmRepository)
    }
}
