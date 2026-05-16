package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CsvReaderTest {

    @Test
    fun readAll_basic() {
        val reader = CsvReader()
        reader.readAll("a,b,c\nd,e,f") shouldBe listOf(
            listOf("a", "b", "c"),
            listOf("d", "e", "f"),
        )
    }

    @Test
    fun read_returnsSequence_lazyEvaluation() {
        val reader = CsvReader()
        val seq = reader.read("a\nb\nc".asSequence())
        seq.take(2).toList() shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun readAll_emptyInput() {
        CsvReader().readAll("") shouldBe emptyList()
    }

    @Test
    fun skipEmptyLine_false_keepsEmptyRows() {
        val reader = CsvReader(CsvReaderConfig(skipEmptyLine = false))
        reader.readAll("a\n\nb") shouldBe listOf(listOf("a"), listOf(""), listOf("b"))
    }

    @Test
    fun skipEmptyLine_true_dropsEmptyRows() {
        val reader = CsvReader(CsvReaderConfig(skipEmptyLine = true))
        reader.readAll("a\n\nb") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun skipEmptyLine_dropsBlankSingleField() {
        val reader = CsvReader(CsvReaderConfig(skipEmptyLine = true))
        reader.readAll("a\n   \nb") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun insufficient_ERROR_throws() {
        val reader = CsvReader(CsvReaderConfig())
        shouldThrow<CsvFieldNumDifferentException> {
            reader.readAll("a,b,c\nd,e")
        }
    }

    @Test
    fun insufficient_IGNORE_skipsRow() {
        val reader = CsvReader(
            CsvReaderConfig(insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE)
        )
        reader.readAll("a,b,c\nd,e\nf,g,h") shouldBe listOf(
            listOf("a", "b", "c"),
            listOf("f", "g", "h"),
        )
    }

    @Test
    fun insufficient_EMPTY_STRING_pads() {
        val reader = CsvReader(
            CsvReaderConfig(insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING)
        )
        reader.readAll("a,b,c\nd,e") shouldBe listOf(
            listOf("a", "b", "c"),
            listOf("d", "e", ""),
        )
    }

    @Test
    fun excess_ERROR_throws() {
        val reader = CsvReader(CsvReaderConfig())
        shouldThrow<CsvFieldNumDifferentException> {
            reader.readAll("a,b\nc,d,e")
        }
    }

    @Test
    fun excess_IGNORE_skipsRow() {
        val reader = CsvReader(
            CsvReaderConfig(excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.IGNORE)
        )
        reader.readAll("a,b\nc,d,e\nf,g") shouldBe listOf(
            listOf("a", "b"),
            listOf("f", "g"),
        )
    }

    @Test
    fun excess_TRIM_truncates() {
        val reader = CsvReader(
            CsvReaderConfig(excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM)
        )
        reader.readAll("a,b\nc,d,e") shouldBe listOf(
            listOf("a", "b"),
            listOf("c", "d"),
        )
    }

    @Test
    fun tsvDialect() {
        val reader = CsvReader(CsvReaderConfig(dialect = CsvDialect.TSV))
        reader.readAll("a\tb\nc\td") shouldBe listOf(
            listOf("a", "b"),
            listOf("c", "d"),
        )
    }

    @Test
    fun sequenceLaziness_noThrowOnConstruction() {
        val reader = CsvReader(CsvReaderConfig())
        val badInput = "a,b,c\nd,e".asSequence()
        // Just constructing the sequence pipeline must not throw.
        val seq = reader.read(badInput)
        // Terminal operation triggers the exception.
        shouldThrow<CsvFieldNumDifferentException> {
            seq.toList()
        }
    }

    @Test
    fun sequenceLaziness_takeBeforeBadRow_doesNotThrow() {
        val reader = CsvReader(CsvReaderConfig())
        val seq = reader.read("a,b,c\nd,e".asSequence())
        seq.take(1).toList() shouldBe listOf(listOf("a", "b", "c"))
    }

    // --- Unquoted-field escape (issue #168) ---

    private val explicitEscapeReader =
        CsvReader(CsvReaderConfig(dialect = CsvDialect(escapeChar = '\\')))

    @Test
    fun unquotedEscape_doubledEscape_inField() {
        explicitEscapeReader.readAll("x,a\\\\b\n") shouldBe listOf(listOf("x", "a\\b"))
    }

    @Test
    fun unquotedEscape_atRowStart() {
        explicitEscapeReader.readAll("\\\\b\n") shouldBe listOf(listOf("\\b"))
    }

    @Test
    fun unquotedEscape_quoteCharEscaped_atRowStart() {
        explicitEscapeReader.readAll("\\\"y\n") shouldBe listOf(listOf("\"y"))
    }

    @Test
    fun unquotedEscape_quoteCharEscaped_afterDelimiter() {
        explicitEscapeReader.readAll("x,\\\"y\n") shouldBe listOf(listOf("x", "\"y"))
    }

    @Test
    fun unquotedEscape_quoteCharEscaped_inField() {
        explicitEscapeReader.readAll("a\\\"b\n") shouldBe listOf(listOf("a\"b"))
    }

    @Test
    fun unquotedEscape_escapeAtEof_throws() {
        shouldThrow<CsvParseFormatException> { explicitEscapeReader.readAll("a\\") }
    }

    @Test
    fun unquotedEscape_followedByOrdinaryChar_throws() {
        shouldThrow<CsvParseFormatException> { explicitEscapeReader.readAll("a\\c") }
    }

    @Test
    fun unquotedEscape_followedByCr_throws() {
        shouldThrow<CsvParseFormatException> { explicitEscapeReader.readAll("a\\\r\n") }
    }

    @Test
    fun unquotedEscape_followedByLf_throws() {
        shouldThrow<CsvParseFormatException> { explicitEscapeReader.readAll("a\\\n") }
    }

    @Test
    fun unquotedEscape_multipleRows() {
        explicitEscapeReader.readAll("a\\\\b\nc\\\\d\n") shouldBe listOf(
            listOf("a\\b"),
            listOf("c\\d"),
        )
    }
}
