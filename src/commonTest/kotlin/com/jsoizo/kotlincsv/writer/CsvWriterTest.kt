package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.reader.CsvNullFieldIndicator
import com.jsoizo.kotlincsv.reader.CsvReader
import com.jsoizo.kotlincsv.reader.CsvReaderConfig
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CsvWriterTest {

    // -------- basics --------

    @Test
    fun writeAll_basic() {
        CsvWriter().writeAll(
            listOf(
                listOf("a", "b", "c"),
                listOf("d", "e", "f"),
            )
        ) shouldBe "a,b,c\r\nd,e,f\r\n"
    }

    @Test
    fun writeAll_multiRow_outputLast_true() {
        CsvWriter().writeAll(
            listOf(listOf("a", "b"), listOf("c", "d"))
        ) shouldBe "a,b\r\nc,d\r\n"
    }

    @Test
    fun writeAll_multiRow_outputLast_false() {
        val writer = CsvWriter(CsvWriterConfig(outputLastLineTerminator = false))
        writer.writeAll(
            listOf(listOf("a", "b"), listOf("c", "d"))
        ) shouldBe "a,b\r\nc,d"
    }

    @Test
    fun writeAll_emptyRows_outputLast_true() {
        CsvWriter().writeAll(emptyList()) shouldBe ""
    }

    @Test
    fun writeAll_emptyRows_outputLast_false() {
        val writer = CsvWriter(CsvWriterConfig(outputLastLineTerminator = false))
        writer.writeAll(emptyList()) shouldBe ""
    }

    @Test
    fun writeAll_singleRow_outputLast_true() {
        CsvWriter().writeAll(listOf(listOf("a", "b"))) shouldBe "a,b\r\n"
    }

    @Test
    fun writeAll_singleRow_outputLast_false() {
        val writer = CsvWriter(CsvWriterConfig(outputLastLineTerminator = false))
        writer.writeAll(listOf(listOf("a", "b"))) shouldBe "a,b"
    }

    @Test
    fun writeAll_emptyFields() {
        CsvWriter().writeAll(listOf(listOf("", "", ""))) shouldBe ",,\r\n"
    }

    @Test
    fun writeAllNullable_nullFieldsAreUnquotedEmptyFields() {
        CsvWriter().writeAllNullable(listOf(listOf(null, "x", ""))) shouldBe ",x,\r\n"
    }

    @Test
    fun writeAllNullable_emptyRows() {
        CsvWriter().writeAllNullable(emptyList()) shouldBe ""
    }

    @Test
    fun writeAll_emptyRow() {
        CsvWriter().writeAll(listOf(emptyList())) shouldBe "\r\n"
    }

    @Test
    fun writeAll_emptyRowsConsecutive() {
        CsvWriter().writeAll(
            listOf(listOf("a"), emptyList())
        ) shouldBe "a\r\n\r\n"
    }

    // -------- CANONICAL quote --------

    @Test
    fun quote_canonical_quotesDelimiter() {
        CsvWriter().writeAll(listOf(listOf("a,b", "c"))) shouldBe "\"a,b\",c\r\n"
    }

    @Test
    fun quote_canonical_quotesNewline() {
        CsvWriter().writeAll(listOf(listOf("a\nb"))) shouldBe "\"a\nb\"\r\n"
    }

    @Test
    fun quote_canonical_quotesQuoteChar() {
        CsvWriter().writeAll(listOf(listOf("a\"b"))) shouldBe "\"a\"\"b\"\r\n"
    }

    @Test
    fun quote_canonical_doesNotQuotePlain() {
        CsvWriter().writeAll(listOf(listOf("abc"))) shouldBe "abc\r\n"
    }

    // -------- ALL quote --------

    @Test
    fun quote_all_quotesEverything() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
        writer.writeAll(listOf(listOf("a", "b"))) shouldBe "\"a\",\"b\"\r\n"
    }

    @Test
    fun writeAllNullable_quoteAllKeepsNullUnquotedAndQuotesEmptyString() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
        writer.writeAllNullable(listOf(listOf(null, "x", ""))) shouldBe ",\"x\",\"\"\r\n"
    }

    @Test
    fun writeAllNullable_multiRow_outputLast_false() {
        val writer = CsvWriter(
            CsvWriterConfig(
                outputLastLineTerminator = false,
                quoteMode = WriteQuoteMode.ALL,
            )
        )
        writer.writeAllNullable(listOf(listOf(null, ""), listOf("x", null))) shouldBe ",\"\"\r\n\"x\","
    }

    // -------- NON_NUMERIC quote --------

    @Test
    fun quote_nonNumeric_doesNotQuoteIntegers() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf("123"))) shouldBe "123\r\n"
    }

    @Test
    fun quote_nonNumeric_doesNotQuoteDecimals() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf("1.5"))) shouldBe "1.5\r\n"
    }

    @Test
    fun quote_nonNumeric_doesNotQuoteDotOnlyToken() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf("."))) shouldBe ".\r\n"
    }

    @Test
    fun quote_nonNumeric_doesNotQuoteLeadingOrTrailingDotToken() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf(".5", "1."))) shouldBe ".5,1.\r\n"
    }

    @Test
    fun quote_nonNumeric_quotesSignedOrExponentToken() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf("-1", "+1", "1e3"))) shouldBe "\"-1\",\"+1\",\"1e3\"\r\n"
    }

    @Test
    fun quote_nonNumeric_quotesNonNumeric() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf("abc"))) shouldBe "\"abc\"\r\n"
    }

    @Test
    fun quote_nonNumeric_quotesEmpty() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.NON_NUMERIC))
        writer.writeAll(listOf(listOf(""))) shouldBe "\"\"\r\n"
    }

    // -------- escape: doubling (default, escapeChar == quoteChar) --------

    @Test
    fun escape_doubling_default() {
        CsvWriter().writeAll(listOf(listOf("a\"b"))) shouldBe "\"a\"\"b\"\r\n"
    }

    @Test
    fun escape_doubling_with_quoteModeAll() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
        writer.writeAll(listOf(listOf("a\"b"))) shouldBe "\"a\"\"b\"\r\n"
    }

    // -------- escape: explicit (escapeChar != quoteChar) --------

    @Test
    fun escape_explicit_quoteEscaped() {
        val dialect = CsvDialect(escapeChar = '\\')
        val writer = CsvWriter(CsvWriterConfig(dialect = dialect))
        writer.writeAll(listOf(listOf("a\"b"))) shouldBe "\"a\\\"b\"\r\n"
    }

    @Test
    fun escape_explicit_escapeCharEscaped() {
        val dialect = CsvDialect(escapeChar = '\\')
        val writer = CsvWriter(CsvWriterConfig(dialect = dialect))
        writer.writeAll(listOf(listOf("a\\b"))) shouldBe "\"a\\\\b\"\r\n"
    }

    @Test
    fun escape_explicit_with_quoteModeAll() {
        val dialect = CsvDialect(escapeChar = '\\')
        val writer = CsvWriter(CsvWriterConfig(dialect = dialect, quoteMode = WriteQuoteMode.ALL))
        writer.writeAll(listOf(listOf("a\"b\\c"))) shouldBe "\"a\\\"b\\\\c\"\r\n"
    }

    @Test
    fun escape_explicit_with_quoteModeNonNumeric() {
        val dialect = CsvDialect(escapeChar = '\\')
        val writer = CsvWriter(
            CsvWriterConfig(dialect = dialect, quoteMode = WriteQuoteMode.NON_NUMERIC)
        )
        writer.writeAll(listOf(listOf("a\"b"))) shouldBe "\"a\\\"b\"\r\n"
    }

    // -------- dialects --------

    @Test
    fun dialect_tsv_basic() {
        val writer = CsvWriter(CsvWriterConfig(dialect = CsvDialect.TSV))
        writer.writeAll(
            listOf(listOf("a", "b"), listOf("c", "d"))
        ) shouldBe "a\tb\nc\td\n"
    }

    // -------- embedded line breaks --------

    @Test
    fun embeddedNewline_inField_isQuoted() {
        CsvWriter().writeAll(listOf(listOf("a\nb"))) shouldBe "\"a\nb\"\r\n"
    }

    @Test
    fun embeddedCrLf_inField_isQuoted() {
        CsvWriter().writeAll(listOf(listOf("a\r\nb"))) shouldBe "\"a\r\nb\"\r\n"
    }

    // -------- laziness --------

    @Test
    fun write_returnsLazySequence() {
        // Infinite source — if write() were not lazy, take() would never terminate.
        val rows = generateSequence(0) { it + 1 }.map { listOf(it.toString()) }
        val partial = CsvWriter().write(rows).take(3).joinToString("")
        partial shouldBe "0\r\n"
    }

    @Test
    fun writeNullable_returnsLazySequence() {
        val rows = generateSequence(0) { it + 1 }.map { listOf<String?>(null, it.toString()) }
        val partial = CsvWriter().writeNullable(rows).take(4).joinToString("")
        partial shouldBe ",0\r\n"
    }

    @Test
    fun nullableRoundTrip_quoteAllDistinguishesNullAndEmptyString() {
        val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        val rows = listOf(listOf(null, "", "x"))

        reader.readAllNullable(writer.writeAllNullable(rows)) shouldBe rows
    }
}
