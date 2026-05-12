package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.CsvReaderConfig
import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalKotest::class)
class SkipEmptyLinePropertyTest {

    private val blankRowArb: Arb<List<String>> = Arb.of(
        emptyList(),
        listOf(""),
        listOf(" "),
        listOf("\t"),
        listOf("\r\n"),
    )

    private val rowsWithBlankRowsArb: Arb<Pair<List<List<String>>, List<List<String>>>> =
        rowsArb.flatMap { dataRows ->
            Arb.list(Arb.int(0..2), dataRows.size + 1..dataRows.size + 1).flatMap { blankCounts ->
                Arb.list(blankRowArb, blankCounts.sum()..blankCounts.sum()).map { blankRows ->
                    val rows = mutableListOf<List<String>>()
                    var blankIndex = 0
                    for (index in dataRows.indices) {
                        repeat(blankCounts[index]) { rows.add(blankRows[blankIndex++]) }
                        rows.add(dataRows[index])
                    }
                    repeat(blankCounts.last()) { rows.add(blankRows[blankIndex++]) }
                    rows.toList() to dataRows
                }
            }
        }

    @Test
    fun skipEmptyLineDropsOnlyEmptyRowsBeforeFieldCountPolicy_property() = runTest {
        checkAll(
            PropTestConfig(seed = 0L, iterations = 500),
            rowsWithBlankRowsArb,
        ) { (rows, dataRows) ->
            val text = csvWriter {
                outputLastLineTerminator = true
                quoteMode = WriteQuoteMode.ALL
            }.writeAll(rows)
            val expectedRows = dataRows.filterNot(::isSkippedRow)

            val parsed = csvReader(CsvReaderConfig(skipEmptyLine = true)).readAll(text)

            parsed shouldBe expectedRows
            parsed.any(::isSkippedRow) shouldBe false
        }
    }

    private fun isSkippedRow(row: List<String>): Boolean =
        row.isEmpty() || (row.size == 1 && row.single().isBlank())
}
