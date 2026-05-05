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
}
