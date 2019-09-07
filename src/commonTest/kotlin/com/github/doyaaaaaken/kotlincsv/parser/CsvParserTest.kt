package com.github.doyaaaaaken.kotlincsv.parser

import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CsvParserTest {

    private val parser = CsvParser('"', ',', '"')

    private val lineTerminators = listOf("\n", "\u2028", "\u2029", "\u0085", "\r", "\r\n")

    @Test
    fun `parseRow method should parseEmptyRow`() {
        assertEquals(parser.parseRow(""), emptyList())
    }

    @Test
    fun `parseRow method should return null if line is on the way of csv row`() {
        assertNull(parser.parseRow("a,\"b"))
    }

    @Test
    fun `ParseStateMachine logic should parse delimiter at the start of row`() {
        assertEquals(parser.parseRow(",a"), listOf("", "a"))
    }

    @Test
    fun `ParseStateMachine logic should parse line terminator at the start of row`() {
        lineTerminators.forEach { lt ->
            assertEquals(parser.parseRow(lt), listOf(""))
        }
    }

    @Test
    fun `ParseStateMachine logic should parse row with delimiter at the end`() {
        assertEquals(parser.parseRow("a,"), listOf("a", ""))
    }

    @Test
    fun `ParseStateMachine logic should parse line terminator after quote end`() {
        lineTerminators.forEach { lt ->
            assertEquals(parser.parseRow("""a,"b"$lt"""), listOf("a", "b"))
        }
    }

    @Test
    fun `ParseStateMachine logic should parse line terminator after delimiter`() {
        lineTerminators.forEach { lt ->
            assertEquals(parser.parseRow("a,$lt"), listOf("a", ""))
        }
    }

    @Test
    fun `ParseStateMachine logic should parse line terminator after field`() {
        lineTerminators.forEach { lt ->
            assertEquals(parser.parseRow("a$lt"), listOf("a"))
        }
    }

    @Test
    fun `ParseStateMachine logic should parse escape character after field`() {
        assertEquals(parser.parseRow("a\"\""), listOf("a\""))
    }

    @Test
    fun `ParseStateMachine logic should throw exception when parsing 2 rows`() {
        lineTerminators.forEach { lt ->
            assertFailsWith(MalformedCSVException::class) {
                parser.parseRow("a${lt}b")
            }
        }
    }
}
