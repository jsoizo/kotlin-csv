package com.jsoizo.kotlincsv

import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class UnquotedEscapePropertyTest {

    private val dialect = CsvDialect(escapeChar = '\\')

    private val unquotedSafeChar: Arb<Char> = fieldChar.filter { ch ->
        ch != ',' && ch != '"' &&
            ch != '\n' && ch != '\r' &&
            ch != '\u2028' && ch != '\u2029' && ch != '\u0085'
    }

    private val unquotedFieldArb: Arb<String> =
        Arb.list(unquotedSafeChar, 0..16).map { it.joinToString("") }

    private fun serialize(field: String): String = buildString {
        for (ch in field) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                else -> append(ch)
            }
        }
        append('\n')
    }

    @Test
    fun unquotedEscape_singleFieldRoundTrip() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            unquotedFieldArb,
        ) { field ->
            val text = serialize(field)
            val parsed = csvReader { this.dialect = this@UnquotedEscapePropertyTest.dialect }.readAll(text)
            parsed shouldBe listOf(listOf(field))
        }
    }
}
