package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.PropTestConfig
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class CanonicalQuoteSoundnessPropertyTest {

    @Test
    fun canonicalQuote_singleFieldRoundTrip() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            dialectArb,
            fieldArb,
        ) { dialect, field ->
            val text = csvWriter {
                this.dialect = dialect
                this.quoteMode = WriteQuoteMode.CANONICAL
                this.outputLastLineTerminator = true
            }.writeAll(listOf(listOf(field)))

            val parsed = csvReader { this.dialect = dialect }.readAll(text)
            parsed shouldBe listOf(listOf(field))
        }
    }
}
