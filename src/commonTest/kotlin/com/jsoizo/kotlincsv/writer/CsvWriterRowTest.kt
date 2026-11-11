package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvWriterRowTest {
    private val dialects = listOf(
        CsvDialect.RFC4180,
        CsvDialect.TSV,
        CsvDialect(delimiter = ';', quoteChar = '\'', escapeChar = '\\', lineTerminator = "\n"),
        CsvDialect(lineTerminator = "<END>"),
    )

    @Test
    fun writeRowMatchesOneRowWriteAllForEveryConfiguration() {
        val rows = listOf(
            emptyList(), listOf(""), listOf("", ""), listOf("a", "b"),
            listOf("a,b;\tc", "a\"b'c\\d", "a\r\nb\nc"),
            listOf("\u2028\u2029\u0085", "\uD83D\uDE00", "<END>"),
            listOf("123", "1.5", "-2", "NaN"), listOf(" leading ", "trailing\t"),
        )
        for (dialect in dialects) for (mode in WriteQuoteMode.entries) for (last in listOf(false, true)) {
            val writer = CsvWriter(CsvWriterConfig(dialect, last, mode))
            for (row in rows) {
                assertEquals(writer.writeAll(listOf(row)), writer.writeRow(row), "$dialect/$mode/$last/$row")
            }
        }
    }

    @Test
    fun writeNullableRowMatchesOneRowWriteAllNullableForEveryConfiguration() {
        val rows = listOf(
            emptyList(), listOf(null), listOf(null, "", "a"), listOf("", null, ""),
            listOf(null, null), listOf("a,b;\tc", "a\"b'c\\d", "a\r\nb\nc"),
            listOf("\uD83D\uDE00", null, "<END>"), listOf("12", null, "1.5"),
        )
        for (dialect in dialects) for (mode in WriteQuoteMode.entries) for (last in listOf(false, true)) {
            val writer = CsvWriter(CsvWriterConfig(dialect, last, mode))
            for (row in rows) {
                assertEquals(writer.writeAllNullable(listOf(row)), writer.writeNullableRow(row), "$dialect/$mode/$last/$row")
            }
        }
    }

    @Test
    fun singleRecordOutputsPreserveQuotingNullAndEmptyRowSemantics() {
        val writer = CsvWriter()
        assertEquals("a,\"b,c\",\"d\"\"e\",\"f\r\ng\"\r\n", writer.writeRow(listOf("a", "b,c", "d\"e", "f\r\ng")))
        assertEquals("\r\n", writer.writeRow(emptyList()))
        assertEquals("\r\n", writer.writeNullableRow(emptyList()))
        val quoteAll = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
        assertEquals(",\"\",\"value\"\r\n", quoteAll.writeNullableRow(listOf(null, "", "value")))
        assertEquals("\"\"\r\n", quoteAll.writeRow(listOf("")))
        val noTerminator = CsvWriter(CsvWriterConfig(outputLastLineTerminator = false))
        assertEquals("", noTerminator.writeRow(emptyList()))
        assertEquals("", noTerminator.writeNullableRow(listOf(null)))
        assertEquals("a,b", noTerminator.writeRow(listOf("a", "b")))
    }

    @Test
    fun repeatedCallsPreserveInputsAndExistingMultiRowOutput() {
        val row = mutableListOf("a", "\uD83D\uDE00", "b,c")
        val nullable = mutableListOf(null, "", "a")
        val original = row.toList()
        val originalNullable = nullable.toList()
        val writer = CsvWriter()
        val multi = listOf(listOf("a", "b"), listOf("c", "d"))
        repeat(2) {
            assertEquals("a,\uD83D\uDE00,\"b,c\"\r\n", writer.writeRow(row))
            assertEquals(",,a\r\n", writer.writeNullableRow(nullable))
            assertEquals("a,b\r\nc,d\r\n", writer.writeAll(multi))
        }
        assertEquals(original, row)
        assertEquals(originalNullable, nullable)
    }
}
