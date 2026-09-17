/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.command.CommandParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {
    @Test
    fun worldAliasesNavigateToWorldClock() {
        assertEquals(ClockCommand.WorldClock, CommandParser.parse("world"))
        assertEquals(ClockCommand.WorldClock, CommandParser.parse(" worldclock "))
    }

    @Test
    fun homeAliasStaysOnMainClock() {
        assertEquals(ClockCommand.ClockView, CommandParser.parse("home"))
    }
}
