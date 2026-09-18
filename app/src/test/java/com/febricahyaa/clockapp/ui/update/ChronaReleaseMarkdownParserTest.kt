/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChronaReleaseMarkdownParserTest {
    @Test
    fun parsesHeadingsBulletsAndParagraphs() {
        val blocks = ChronaReleaseMarkdownParser.parse(
            """# Chrona 1.0

Released **today**.

## Fixed
- Alarm `save` flow
- [x] Timer reset""",
        )

        assertIs<ChronaReleaseBlock.Heading>(blocks[0])
        assertEquals("Chrona 1.0", (blocks[0] as ChronaReleaseBlock.Heading).text)
        assertIs<ChronaReleaseBlock.Paragraph>(blocks[1])
        assertEquals("Released today.", (blocks[1] as ChronaReleaseBlock.Paragraph).text)
        assertIs<ChronaReleaseBlock.Heading>(blocks[2])
        assertIs<ChronaReleaseBlock.Bullet>(blocks[3])
        assertEquals("Alarm save flow", (blocks[3] as ChronaReleaseBlock.Bullet).text)
        assertIs<ChronaReleaseBlock.Bullet>(blocks[4])
        assertTrue((blocks[4] as ChronaReleaseBlock.Bullet).checked == true)
    }

    @Test
    fun parsesFencedCodeAsSingleBlock() {
        val blocks = ChronaReleaseMarkdownParser.parse(
            """```text
./gradlew test
```""",
        )

        val code = assertIs<ChronaReleaseBlock.Code>(blocks.single())
        assertEquals("./gradlew test", code.text)
    }
}
