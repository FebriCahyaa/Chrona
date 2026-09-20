/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.alarm.AlarmStateManager
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AlarmStateManagerMockKTest {
    @Test
    fun rescheduleUsesInjectedRepositoryAndScheduler() = runBlocking {
        val repository = mockk<AlarmRepository>()
        val scheduler = mockk<AlarmSchedulerGateway>(relaxed = true)
        val alarms = listOf(
            AlarmItem(
                id = 7L,
                time = LocalTime.of(7, 30),
                label = "Morning",
                enabled = true,
                repeatDays = emptySet(),
            ),
        )
        coEvery { repository.load() } returns alarms

        AlarmStateManager(repository, scheduler).rescheduleAll()

        coVerify(exactly = 1) { repository.load() }
        verify(exactly = 1) { scheduler.rescheduleAll(alarms) }
    }
}
