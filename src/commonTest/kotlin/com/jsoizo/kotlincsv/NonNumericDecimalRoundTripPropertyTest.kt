package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class NonNumericDecimalRoundTripPropertyTest {

    @Test
    fun nonNumeric_decimalField_roundTrip() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            anyDialectArb,
            Arb.boolean(),
            decimalRowsArb,
        ) { dialect, outputLastLineTerminator, rows ->
            val text = csvWriter {
                this.dialect = dialect
                this.quoteMode = WriteQuoteMode.NON_NUMERIC
                this.outputLastLineTerminator = outputLastLineTerminator
            }.writeAll(rows)

            val parsed = csvReader { this.dialect = dialect }.readAll(text)
            parsed shouldBe rows
        }
    }
}
