package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.CsvReaderConfig
import com.jsoizo.kotlincsv.reader.ExcessFieldsRowBehaviour
import com.jsoizo.kotlincsv.reader.InsufficientFieldsRowBehaviour
import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.PropTestConfig
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class ReaderFieldCountPolicyPropertyTest {

    @Test
    fun emptyStringAndTrim_normalizeRowsToFirstRowWidth_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            variableWidthRowsArb,
        ) { rows ->
            val reader = csvReader(
                CsvReaderConfig(
                    insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING,
                    excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM,
                )
            )

            val text = encodeRowsForPolicyTest(rows)
            val parsed = reader.readAll(text)
            val expectedWidth = rows.first().size

            parsed.map { it.size } shouldBe List(rows.size) { expectedWidth }
            parsed shouldBe rows.map { row ->
                when {
                    row.size < expectedWidth -> row + List(expectedWidth - row.size) { "" }
                    row.size > expectedWidth -> row.take(expectedWidth)
                    else -> row
                }
            }
        }
    }

    @Test
    fun ignorePoliciesKeepOnlyRowsMatchingFirstRowWidth_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            variableWidthRowsArb,
        ) { rows ->
            val reader = csvReader(
                CsvReaderConfig(
                    insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE,
                    excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.IGNORE,
                )
            )

            val text = encodeRowsForPolicyTest(rows)
            val parsed = reader.readAll(text)
            val expectedWidth = rows.first().size

            parsed shouldBe rows.filter { it.size == expectedWidth }
        }
    }

    private fun encodeRowsForPolicyTest(rows: List<List<String>>): String =
        csvWriter {
            outputLastLineTerminator = false
            quoteMode = WriteQuoteMode.ALL
        }.writeAll(rows)
}
