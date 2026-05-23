package com.jsoizo.kotlincsv.writer

import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import java.io.ByteArrayOutputStream
import java.io.FilterOutputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.test.Test

class CsvWriterJvmIoTest {

    private val sampleRows = listOf(
        listOf("a", "b", "c"),
        listOf("d", "e", "f"),
    )
    private val sampleRowsEncoded = "a,b,c\r\nd,e,f\r\n"

    @Test
    fun writeToFile_file_utf8_basic_writesEncodedBytes() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().writeToFile(sampleRows, tmp.toFile())

            String(Files.readAllBytes(tmp), Charsets.UTF_8) shouldBe sampleRowsEncoded
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun writeToFile_stringPath_utf8_basic_writesEncodedBytes() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().writeToFile(sampleRows, tmp.toString())

            String(Files.readAllBytes(tmp), Charsets.UTF_8) shouldBe sampleRowsEncoded
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun writeToFile_file_shiftJis_encodesJapaneseCharacters() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().writeToFile(listOf(listOf("あ", "い")), tmp.toFile(), charset = "Shift_JIS")

            String(Files.readAllBytes(tmp), Charset.forName("Shift_JIS")) shouldBe "あ,い\r\n"
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    fun write_stream_callerOwnedStream_isNotClosed() {
        val raw = ByteArrayOutputStream()
        val counting = CountingOutputStream(raw)

        CsvWriter().write(sampleRows, counting)

        String(raw.toByteArray(), Charsets.UTF_8) shouldBe sampleRowsEncoded
        counting.closeCount shouldBe 0
    }

    @Test
    fun write_stream_flushesUnderlyingStream() {
        val counting = CountingOutputStream(ByteArrayOutputStream())

        CsvWriter().write(sampleRows, counting)

        counting.flushCount shouldBeGreaterThanOrEqual 1
    }

    @Test
    fun write_stream_prependBomUtf8_emitsEfBbBfPrefix() {
        val raw = ByteArrayOutputStream()

        CsvWriter().write(sampleRows, raw, options = CsvWriteIoOptions(prependBom = true))

        val out = raw.toByteArray()
        out.copyOfRange(0, 3).toList() shouldBe listOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        String(out.copyOfRange(3, out.size), Charsets.UTF_8) shouldBe sampleRowsEncoded
    }

    @Test
    fun write_stream_listOverloadMatchesSequenceOverload() {
        val rawList = ByteArrayOutputStream()
        val rawSeq = ByteArrayOutputStream()

        CsvWriter().write(sampleRows, rawList)
        CsvWriter().write(sampleRows.asSequence(), rawSeq)

        rawList.toByteArray().toList() shouldBe rawSeq.toByteArray().toList()
    }

    private class CountingOutputStream(stream: OutputStream) : FilterOutputStream(stream) {
        var closeCount: Int = 0
            private set
        var flushCount: Int = 0
            private set

        override fun close() {
            closeCount++
            super.close()
        }

        override fun flush() {
            flushCount++
            super.flush()
        }
    }
}
