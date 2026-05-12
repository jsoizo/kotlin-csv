package com.jsoizo.kotlincsv

import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class EncodeIdempotencePropertyTest {

    @Test
    fun encodeIdempotence_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            dialectArb,
            quoteModeArb,
            Arb.boolean(),
            rowsArb,
        ) { dialect, quoteMode, outputLastLineTerminator, rows ->
            if (!outputLastLineTerminator && rows.last().all { it.isEmpty() }) {
                return@checkAll
            }

            val writer = csvWriter {
                this.dialect = dialect
                this.quoteMode = quoteMode
                this.outputLastLineTerminator = outputLastLineTerminator
            }
            val firstEncode = writer.writeAll(rows)
            val parsed = csvReader { this.dialect = dialect }.readAll(firstEncode)
            val secondEncode = writer.writeAll(parsed)
            secondEncode shouldBe firstEncode
        }
    }
}
