package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.CsvWriterConfig
import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CsvWriterDslTest {

    @Test
    fun dslBlock_endToEnd_writesCsv() {
        val writer = csvWriter {
            dialect = CsvDialect.TSV
            outputLastLineTerminator = false
        }
        writer.writeAll(
            listOf(listOf("a", "b"), listOf("c", "d"))
        ) shouldBe "a\tb\nc\td"
    }

    @Test
    fun dslBlock_quoteModeAll() {
        val writer = csvWriter { quoteMode = WriteQuoteMode.ALL }
        writer.writeAll(listOf(listOf("a", "b"))) shouldBe "\"a\",\"b\"\r\n"
    }

    @Test
    fun configFactory_passThrough() {
        val cfg = CsvWriterConfig(outputLastLineTerminator = false)
        csvWriter(cfg).writeAll(listOf(listOf("x"))) shouldBe "x"
    }
}
