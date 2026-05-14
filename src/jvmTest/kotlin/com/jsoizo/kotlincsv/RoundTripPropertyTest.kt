package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.take
import kotlin.test.Test

class RoundTripPropertyTest {

    private data class TestCase(
        val dialect: CsvDialect,
        val quoteMode: WriteQuoteMode,
        val outputLastLineTerminator: Boolean,
        val rows: List<List<String>>,
    )

    private val caseArb: Arb<TestCase> = Arb.bind(
        dialectArb,
        quoteModeArb,
        Arb.boolean(),
        rowsArb,
        ::TestCase,
    )

    @Test
    fun roundTrip_property() {
        val rs = RandomSource.seeded(0L)
        val iterations = 500
        val cases = caseArb.take(iterations, rs).toList()

        for (case in cases) {
            // Final row of a single empty field with no trailing terminator
            // produces zero output and cannot round-trip. ALL still quotes it.
            if (!case.outputLastLineTerminator &&
                case.quoteMode != WriteQuoteMode.ALL &&
                case.rows.last() == listOf("")
            ) continue

            val text = csvWriter {
                this.dialect = case.dialect
                this.quoteMode = case.quoteMode
                this.outputLastLineTerminator = case.outputLastLineTerminator
            }.writeAll(case.rows)

            val parsed = try {
                csvReader { this.dialect = case.dialect }.readAll(text)
            } catch (e: Throwable) {
                throw AssertionError(
                    "Parse threw on round-trip.\n" + describe(case, text, null),
                    e,
                )
            }

            try {
                parsed shouldBe case.rows
            } catch (e: AssertionError) {
                throw AssertionError(
                    "Round-trip mismatch.\n" + describe(case, text, parsed),
                    e,
                )
            }
        }
    }

    private fun describe(
        case: TestCase,
        text: String,
        parsed: List<List<String>>?,
    ): String = buildString {
        appendLine("  dialect=${case.dialect}")
        appendLine("  quoteMode=${case.quoteMode}")
        appendLine("  outputLastLineTerminator=${case.outputLastLineTerminator}")
        appendLine("  rows=${case.rows.map { row -> row.map { escape(it) } }}")
        appendLine("  text=${escape(text)}")
        if (parsed != null) {
            appendLine("  parsed=${parsed.map { row -> row.map { escape(it) } }}")
        }
    }

    private fun escape(s: String): String = buildString {
        append('"')
        for (ch in s) when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (ch.code in 0x20..0x7E) append(ch)
            else append("\\u").append(ch.code.toString(16).padStart(4, '0'))
        }
        append('"')
    }
}
