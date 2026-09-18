/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.update

sealed interface ChronaReleaseBlock {
    data class Heading(val level: Int, val text: String) : ChronaReleaseBlock
    data class Bullet(val text: String, val checked: Boolean? = null) : ChronaReleaseBlock
    data class Numbered(val number: Int, val text: String) : ChronaReleaseBlock
    data class Paragraph(val text: String) : ChronaReleaseBlock
    data class Code(val text: String) : ChronaReleaseBlock
}

object ChronaReleaseMarkdownParser {
    fun parse(markdown: String): List<ChronaReleaseBlock> {
        if (markdown.isBlank()) return emptyList()

        val blocks = mutableListOf<ChronaReleaseBlock>()
        val paragraph = mutableListOf<String>()
        var inCode = false
        val codeLines = mutableListOf<String>()

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += ChronaReleaseBlock.Paragraph(paragraph.joinToString(" ").trim())
                paragraph.clear()
            }
        }

        fun flushCode() {
            if (codeLines.isNotEmpty()) {
                blocks += ChronaReleaseBlock.Code(codeLines.joinToString("\n"))
                codeLines.clear()
            }
        }

        markdown.replace("\r\n", "\n").split('\n').forEach { rawLine ->
            val line = rawLine.trimEnd()

            if (line.trim().startsWith("```")) {
                if (inCode) flushCode() else flushParagraph()
                inCode = !inCode
                return@forEach
            }

            if (inCode) {
                codeLines += line
                return@forEach
            }

            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                flushParagraph()
                return@forEach
            }

            val heading = Regex("^(#{1,3})\\s+(.+)$").matchEntire(trimmed)
            if (heading != null) {
                flushParagraph()
                blocks += ChronaReleaseBlock.Heading(
                    level = heading.groupValues[1].length,
                    text = heading.groupValues[2].trim().stripMarkdownInline(),
                )
                return@forEach
            }

            val bullet = Regex("^[-*+]\\s+(?:\\[([ xX])\\]\\s+)?(.+)$").matchEntire(trimmed)
            if (bullet != null) {
                flushParagraph()
                val marker = bullet.groupValues[1]
                blocks += ChronaReleaseBlock.Bullet(
                    text = bullet.groupValues[2].trim().stripMarkdownInline(),
                    checked = marker.takeIf { it.isNotBlank() }?.equals("x", ignoreCase = true),
                )
                return@forEach
            }

            val numbered = Regex("^(\\d+)\\.\\s+(.+)$").matchEntire(trimmed)
            if (numbered != null) {
                flushParagraph()
                blocks += ChronaReleaseBlock.Numbered(
                    number = numbered.groupValues[1].toIntOrNull() ?: 0,
                    text = numbered.groupValues[2].trim().stripMarkdownInline(),
                )
                return@forEach
            }

            paragraph += trimmed.stripMarkdownInline()
        }

        if (inCode) flushCode()
        flushParagraph()
        return blocks
    }
}

private fun String.stripMarkdownInline(): String = this
    .replace(Regex("!\\[([^]]*)\\]\\([^)]*\\)"), "$1")
    .replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")
    .replace(Regex("`([^`]+)`"), "$1")
    .replace("**", "")
    .replace("__", "")
    .replace(Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)"), "$1")
    .replace(Regex("(?<!_)_([^_]+)_(?!_)"), "$1")
    .trim()
