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
    fun readAllNullable_emptyInput() {
        CsvReader().readAllNullable("") shouldBe emptyList()
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
    fun skipEmptyLine_fieldCountErrorRowNum_countsFilteredCsvRows() {
        val reader = CsvReader(CsvReaderConfig(skipEmptyLine = true))
        val ex = shouldThrow<CsvFieldNumDifferentException> {
            reader.readAll("a,b\n\nc")
        }
        ex.rowNum shouldBe 2L
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

    @Test
    fun readAllNullable_defaultKeepsEmptyStrings() {
        val reader = CsvReader()
        reader.readAllNullable("\"col1\",\"col2\"\n\"\",") shouldBe listOf(
            listOf("col1", "col2"),
            listOf("", ""),
        )
    }

    @Test
    fun readAllNullable_emptySeparators_issue81Sample() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        reader.readAllNullable("\"col1\",\"col2\"\n\"\",") shouldBe listOf(
            listOf("col1", "col2"),
            listOf("", null),
        )
    }

    @Test
    fun readAllNullable_emptyQuotes() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_QUOTES)
        )
        reader.readAllNullable("\"\",") shouldBe listOf(listOf(null, ""))
    }

    @Test
    fun readAllNullable_bothEmptyKinds() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.BOTH)
        )
        reader.readAllNullable(",\"\"") shouldBe listOf(listOf(null, null))
    }

    @Test
    fun readAllNullable_emptySeparators_leadingMiddleTrailingAndEof() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        reader.readAllNullable(",a,\nb,,") shouldBe listOf(
            listOf(null, "a", null),
            listOf("b", null, null),
        )
    }

    @Test
    fun readAllNullable_insufficientEmptyStringPaddingStaysEmptyString() {
        val reader = CsvReader(
            CsvReaderConfig(
                insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING,
                nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS,
            )
        )
        reader.readAllNullable("a,b,c\nd,e") shouldBe listOf(
            listOf("a", "b", "c"),
            listOf("d", "e", ""),
        )
    }

    @Test
    fun readAllNullable_skipEmptyLineTrue_dropsEmptyRows() {
        val reader = CsvReader(CsvReaderConfig(skipEmptyLine = true))
        reader.readAllNullable("a\n\nb") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun readAllNullable_insufficientErrorThrows() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        shouldThrow<CsvFieldNumDifferentException> {
            reader.readAllNullable("a,b,c\nd,")
        }
    }

    @Test
    fun readAllNullable_insufficientIgnoreSkipsRow() {
        val reader = CsvReader(
            CsvReaderConfig(
                insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE,
                nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS,
            )
        )
        reader.readAllNullable("a,b,c\nd,\nf,g,h") shouldBe listOf(
            listOf("a", "b", "c"),
            listOf("f", "g", "h"),
        )
    }

    @Test
    fun readAllNullable_excessErrorThrows() {
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        shouldThrow<CsvFieldNumDifferentException> {
            reader.readAllNullable("a,b\nc,,d")
        }
    }

    @Test
    fun readAllNullable_excessIgnoreSkipsRow() {
        val reader = CsvReader(
            CsvReaderConfig(
                excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.IGNORE,
                nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS,
            )
        )
        reader.readAllNullable("a,b\nc,,d\ne,") shouldBe listOf(
            listOf("a", "b"),
            listOf("e", null),
        )
    }

    @Test
    fun readAllNullable_excessTrimTruncatesRow() {
        val reader = CsvReader(
            CsvReaderConfig(
                excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM,
                nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS,
            )
        )
        reader.readAllNullable("a,b\nc,,d") shouldBe listOf(
            listOf("a", "b"),
            listOf("c", null),
        )
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
