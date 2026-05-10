package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.take
import kotlin.test.Test

class ParseStateMachineCrashFreedomTest {

    private data class TestCase(val dialect: CsvDialect, val text: String)

    private val textArb: Arb<String> =
        Arb.list(anyChar, 0..64).map { it.joinToString("") }

    private val caseArb: Arb<TestCase> = Arb.bind(dialectArb, textArb, ::TestCase)

    @Test
    fun crashFreedom_property() {
        val rs = RandomSource.seeded(0L)
        val iterations = 500
        val cases = caseArb.take(iterations, rs).toList()

        for (case in cases) {
            try {
                csvReader { this.dialect = case.dialect }.readAll(case.text)
            } catch (_: MalformedCsvException) {
                // OK: declared parse failure
            } catch (e: Throwable) {
                throw AssertionError(
                    "Parser threw unexpected exception type ${e::class.simpleName}.\n" +
                        describe(case),
                    e,
                )
            }
        }
    }

    private fun describe(case: TestCase): String = buildString {
        appendLine("  dialect=${case.dialect}")
        appendLine("  text=${escape(case.text)}")
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
