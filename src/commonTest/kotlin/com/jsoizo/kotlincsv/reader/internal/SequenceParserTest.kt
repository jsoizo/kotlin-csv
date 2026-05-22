package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SequenceParserTest {

    private val rfc4180 = CsvDialect.RFC4180

    private fun parse(text: String, dialect: CsvDialect = rfc4180): List<List<String>> =
        parseRows(text.asSequence(), dialect).toList()

    @Test
    fun emptyInput_yieldsEmptySequence() {
        parse("") shouldBe emptyList()
    }

    @Test
    fun singleRow_noTrailingTerminator() {
        parse("a,b,c") shouldBe listOf(listOf("a", "b", "c"))
    }

    @Test
    fun singleRow_endingWithQuote() {
        parse("\"a\"") shouldBe listOf(listOf("a"))
    }

    @Test
    fun lf_terminator() {
        parse("a,b\nc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun crlf_terminator() {
        parse("a,b\r\nc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun cr_only_terminator() {
        parse("a,b\rc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun lineSeparator_U2028() {
        parse("a,b c,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun paragraphSeparator_U2029() {
        parse("a,b c,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun nextLine_U0085() {
        parse("a,bc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun newlineInsideQuote_keptAsField() {
        parse("\"a\nb\",c") shouldBe listOf(listOf("a\nb", "c"))
    }

    @Test
    fun doubleQuoteEscape() {
        parse("\"a\"\"b\"") shouldBe listOf(listOf("a\"b"))
    }

    @Test
    fun escapeCharStyle_backslash() {
        val dialect = CsvDialect(escapeChar = '\\')
        parse("\"a\\\"b\\\\c\"", dialect) shouldBe listOf(listOf("a\"b\\c"))
    }

    @Test
    fun trailingNewline_doesNotProduceExtraEmptyRow() {
        parse("a\n") shouldBe listOf(listOf("a"))
    }

    @Test
    fun emptyRow_singleNewline() {
        parse("\n") shouldBe listOf(listOf(""))
    }

    @Test
    fun multipleEmptyFields() {
        parse(",,") shouldBe listOf(listOf("", "", ""))
    }

    @Test
    fun bomCharacter_passedThroughByParser() {
        // Parser no longer treats U+FEFF specially; BOM handling is owned by
        // the I/O layer (CsvReadIoOptions.stripBom). The parser sees BOM as
        // an ordinary data character.
        parse("\uFEFFa,b") shouldBe listOf(listOf("\uFEFFa", "b"))
    }

    @Test
    fun malformedQuote_throwsNewExceptionType() {
        shouldThrow<CsvParseFormatException> {
            parse("\"abc\"x")
        }
    }

    @Test
    fun malformedExceptionPreservesPositionAndChar() {
        val ex = shouldThrow<CsvParseFormatException> {
            parse("\"abc\"x")
        }
        ex.rowNum shouldBe 1L
        ex.char shouldBe 'x'
    }

    @Test
    fun tsvDialect() {
        parse("a\tb\nc\td", CsvDialect.TSV) shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }

    @Test
    fun crlf_atRowStart_inStartState() {
        // START state immediately receives CR with LF as next, exercising the
        // CRLF skip-second-byte branch before any field characters arrive.
        parse("\r\nrow") shouldBe listOf(listOf(""), listOf("row"))
    }

    @Test
    fun escapeChar_inFieldState_invalidEscape_throws() {
        // Default dialect: escapeChar == quoteChar == '"'. Once a field starts
        // unquoted, encountering a lone '"' is not a valid double-quote escape
        // and must be reported via CsvParseFormatException.
        shouldThrow<CsvParseFormatException> { parse("a\"b") }
    }

    @Test
    fun delimiterState_thenU2028() {
        parse(", b") shouldBe listOf(listOf("", ""), listOf("b"))
    }

    @Test
    fun delimiterState_thenU2029() {
        parse(", b") shouldBe listOf(listOf("", ""), listOf("b"))
    }

    @Test
    fun delimiterState_thenU0085() {
        parse(",b") shouldBe listOf(listOf("", ""), listOf("b"))
    }

    @Test
    fun delimiterState_thenCr() {
        parse(",\rb") shouldBe listOf(listOf("", ""), listOf("b"))
    }

    @Test
    fun delimiterState_thenCrLf() {
        parse(",\r\nb") shouldBe listOf(listOf("", ""), listOf("b"))
    }

    @Test
    fun quoteEnd_thenU2028() {
        parse("\"a\" b") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun quoteEnd_thenU2029() {
        parse("\"a\" b") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun quoteEnd_thenU0085() {
        parse("\"a\"b") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun quoteEnd_thenCr() {
        parse("\"a\"\rb") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun quoteEnd_thenCrLf() {
        parse("\"a\"\r\nb") shouldBe listOf(listOf("a"), listOf("b"))
    }

    @Test
    fun escapeCharDifferent_invalidEscape_throws() {
        // With escapeChar='\\' inside a quoted field, the escape must be
        // followed by either another '\\' or the quote char. 'x' is neither.
        val dialect = CsvDialect(escapeChar = '\\')
        shouldThrow<CsvParseFormatException> { parse("\"a\\xb\"", dialect) }
    }

    @Test
    fun escapeCharDifferent_escapeAtEof_throws() {
        // Same dialect: '\\' immediately before EOF leaves the parser unable
        // to determine the escaped char. Must surface as a parse error.
        val dialect = CsvDialect(escapeChar = '\\')
        shouldThrow<CsvParseFormatException> { parse("\"a\\", dialect) }
    }

    @Test
    fun unterminatedQuote_atEof_yieldsNoFinalRow() {
        // The state machine ends in QUOTED_FIELD, so getResult() returns null
        // and the in-flight row is dropped rather than emitted partial.
        parse("\"abc") shouldBe emptyList()
    }

    private fun chunkedReader(text: String): (CharArray) -> Int {
        var offset = 0
        return { buffer ->
            if (offset >= text.length) {
                0
            } else {
                val end = minOf(text.length, offset + buffer.size)
                val written = end - offset
                for (i in 0 until written) buffer[i] = text[offset + i]
                offset = end
                written
            }
        }
    }

    private fun parseChunked(
        text: String,
        bufferSize: Int = 8192,
        stripBom: Boolean = false,
        dialect: CsvDialect = rfc4180,
    ): List<List<String>> =
        parseRowsFromChunks(chunkedReader(text), dialect, stripBom, bufferSize).toList()

    @Test
    fun chunked_emptyInput_yieldsEmptySequence() {
        // First readInto returns 0 — early-return path.
        parseChunked("") shouldBe emptyList()
    }

    @Test
    fun chunked_smallBuffer_forcesChunkBoundaryAcrossRows() {
        // bufferSize=4 forces the parser to swap chunks several times mid-row
        // and to consult the cross-buffer lookahead path.
        val rows = parseChunked("a,bbb\nccc,d\nef,gh", bufferSize = 4)
        rows shouldBe listOf(listOf("a", "bbb"), listOf("ccc", "d"), listOf("ef", "gh"))
    }

    @Test
    fun chunked_bufferBoundaryLandsOnCrLf() {
        // Tail CR is the last char of one chunk; LF starts the next. The CR
        // branch must consult lookahead via the next-buffer slot to swallow LF.
        val rows = parseChunked("ab\r\ncd", bufferSize = 3)
        rows shouldBe listOf(listOf("ab"), listOf("cd"))
    }

    @Test
    fun chunked_requireBufferSizeAtLeastTwo() {
        shouldThrow<IllegalArgumentException> {
            parseRowsFromChunks(chunkedReader("a"), rfc4180, bufferSize = 1).toList()
        }
    }

    @Test
    fun chunked_stripsBomWhenRequested() {
        // bufferSize chosen so BOM and the first field share the leading chunk.
        val rows = parseChunked("﻿a,b", bufferSize = 4, stripBom = true)
        rows shouldBe listOf(listOf("a", "b"))
    }

    @Test
    fun chunked_preservesBomWhenStripDisabled() {
        // Default stripBom = false — exercises the default-parameter call.
        val rows = parseRowsFromChunks(chunkedReader("﻿a"), rfc4180).toList()
        rows shouldBe listOf(listOf("﻿a"))
    }

    @Test
    fun chunked_unterminatedQuote_atChunkBoundary_yieldsNoFinalRow() {
        // tail-flush path runs but getResult() returns null (QUOTED_FIELD state).
        parseChunked("\"abc", bufferSize = 2) shouldBe emptyList()
    }
}
