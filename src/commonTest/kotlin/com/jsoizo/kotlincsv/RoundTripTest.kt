package com.jsoizo.kotlincsv

import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Core-layer round-trip tests: writing rows with [csvWriter] should produce
 * a CSV string that [csvReader] parses back into the original rows.
 */
class RoundTripTest {

    @Test
    fun rfc4180_roundTrip_simple() {
        val rows = listOf(
            listOf("a", "b", "c"),
            listOf("d", "e", "f"),
        )
        val text = csvWriter().writeAll(rows)
        csvReader().readAll(text) shouldBe rows
    }

    @Test
    fun rfc4180_roundTrip_specialCharsInFields() {
        val rows = listOf(
            listOf("plain", "with,comma", "with\"quote"),
            listOf("with\nnewline", "", "trailing"),
        )
        val text = csvWriter().writeAll(rows)
        csvReader().readAll(text) shouldBe rows
    }

    @Test
    fun tsv_roundTrip() {
        val rows = listOf(listOf("a", "b"), listOf("c", "d"))
        val text = csvWriter { dialect = CsvDialect.TSV }.writeAll(rows)
        csvReader { dialect = CsvDialect.TSV }.readAll(text) shouldBe rows
    }

    @Test
    fun explicitEscape_roundTrip() {
        val dialect = CsvDialect(escapeChar = '\\')
        val rows = listOf(listOf("a\"b", "c\\d"))
        val text = csvWriter { this.dialect = dialect }.writeAll(rows)
        csvReader { this.dialect = dialect }.readAll(text) shouldBe rows
    }
}
