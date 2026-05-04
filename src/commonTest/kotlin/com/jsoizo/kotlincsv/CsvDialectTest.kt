package com.jsoizo.kotlincsv

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CsvDialectTest {

    @Test
    fun construct_withCustomValues_doesNotTriggerRequire() {
        CsvDialect(
            delimiter = ';',
            quoteChar = '\'',
            escapeChar = '\\',
            lineTerminator = "\n",
        )
    }

    @Test
    fun require_rejects_delimiterEqualToQuoteChar() {
        val ex = assertFailsWith<IllegalArgumentException> {
            CsvDialect(delimiter = ',', quoteChar = ',')
        }
        assertTrue(
            ex.message!!.contains(","),
            "expected message to mention violating value, was: ${ex.message}",
        )
    }

    @Test
    fun require_rejects_delimiterEqualToEscapeChar() {
        val ex = assertFailsWith<IllegalArgumentException> {
            CsvDialect(delimiter = ',', escapeChar = ',')
        }
        assertTrue(
            ex.message!!.contains(","),
            "expected message to mention violating value, was: ${ex.message}",
        )
    }

    @Test
    fun require_rejects_emptyLineTerminator() {
        val ex = assertFailsWith<IllegalArgumentException> {
            CsvDialect(lineTerminator = "")
        }
        assertTrue(
            ex.message!!.contains("lineTerminator"),
            "expected message to mention violating field, was: ${ex.message}",
        )
    }
}
