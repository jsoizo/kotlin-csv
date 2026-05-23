package com.jsoizo.kotlincsv.reader

import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.test.Test

class CsvReaderJvmIoTest {

    private val sampleCsv = "a,b,c\nd,e,f"
    private val sampleRows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))

    @Test
    fun readFromFile_file_utf8_basic_decodesRows() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, sampleCsv.toByteArray(Charsets.UTF_8))

            val rows = CsvReader().readFromFile(tmp.toFile()) { it.toList() }

            rows shouldBe sampleRows
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun readFromFile_stringPath_utf8_basic_decodesRows() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, sampleCsv.toByteArray(Charsets.UTF_8))

            val rows = CsvReader().readFromFile(tmp.toString()) { it.toList() }

            rows shouldBe sampleRows
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun readFromFile_file_shiftJis_decodesJapaneseCharacters() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, "あ,い\n".toByteArray(Charset.forName("Shift_JIS")))

            val rows = CsvReader().readFromFile(tmp.toFile(), charset = "Shift_JIS") { it.toList() }

            rows shouldBe listOf(listOf("あ", "い"))
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun readAllFromFile_file_utf8_basic_decodesRows() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, sampleCsv.toByteArray(Charsets.UTF_8))

            CsvReader().readAllFromFile(tmp.toFile()) shouldBe sampleRows
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun read_stream_utf8_basic_decodesRows() {
        val counting = CountingInputStream(ByteArrayInputStream(sampleCsv.toByteArray(Charsets.UTF_8)))

        val rows = CsvReader().read(counting) { it.toList() }

        rows shouldBe sampleRows
        counting.closeCount shouldBe 0
    }

    @Test
    fun readAll_stream_utf8_basic_decodesRows() {
        val counting = CountingInputStream(ByteArrayInputStream(sampleCsv.toByteArray(Charsets.UTF_8)))

        CsvReader().readAll(counting) shouldBe sampleRows
        counting.closeCount shouldBe 0
    }

    private class CountingInputStream(stream: InputStream) : FilterInputStream(stream) {
        var closeCount: Int = 0
            private set

        override fun close() {
            closeCount++
            super.close()
        }
    }
}
