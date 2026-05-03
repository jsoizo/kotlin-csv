package com.jsoizo.kotlincsv.exceptions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CsvExceptionsTest {

    @Test
    fun malformedCsvException_holdsMessage() {
        val ex = MalformedCsvException("boom")
        assertEquals("boom", ex.message)
        assertIs<RuntimeException>(ex)
    }

    @Test
    fun csvParseFormatException_extendsMalformedAndFormatsMessage() {
        val ex = CsvParseFormatException(rowNum = 3L, colIndex = 7L, char = '"')
        assertIs<MalformedCsvException>(ex)
        assertEquals(3L, ex.rowNum)
        assertEquals(7L, ex.colIndex)
        assertEquals('"', ex.char)
        assertEquals(
            "Exception happened on parsing csv [rowNum = 3, colIndex = 7, char = \"]",
            ex.message
        )
    }

    @Test
    fun csvParseFormatException_acceptsCustomMessage() {
        val ex = CsvParseFormatException(
            rowNum = 1L,
            colIndex = 2L,
            char = 'x',
            message = "custom"
        )
        assertEquals("custom [rowNum = 1, colIndex = 2, char = x]", ex.message)
    }

    @Test
    fun csvFieldNumDifferentException_holdsFieldsAndFormatsMessage() {
        val ex = CsvFieldNumDifferentException(
            expectedFieldCount = 3,
            actualFieldCount = 2,
            rowNum = 5L
        )
        assertIs<MalformedCsvException>(ex)
        assertEquals(3, ex.expectedFieldCount)
        assertEquals(2, ex.actualFieldCount)
        assertEquals(5L, ex.rowNum)
        assertTrue(ex.message!!.contains("Fields num seems to be 3"))
        assertTrue(ex.message!!.contains("5th csv row"))
        assertTrue(ex.message!!.contains("fields num is 2"))
    }
}
