package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.CountingOutputStream
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test

class CsvWriterJvmIoTest {

    private val sampleRows = listOf(
        listOf("a", "b", "c"),
        listOf("d", "e", "f"),
    )
    private val sampleRowsEncoded = "a,b,c\r\nd,e,f\r\n"

    @Test
    fun write_file_utf8_basic_writesEncodedBytes() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().write(sampleRows, tmp.toFile())
            Files.readString(tmp, Charsets.UTF_8) shouldBe sampleRowsEncoded
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun write_file_shiftJis_encodesJapaneseCharacters() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().write(listOf(listOf("あ", "い")), tmp.toFile(), charset = "Shift_JIS")
            val text = String(Files.readAllBytes(tmp), Charset.forName("Shift_JIS"))
            text shouldBe "あ,い\r\n"
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun write_file_sjisAlias_resolvesToShiftJis() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-writer", ".csv")
        try {
            CsvWriter().write(listOf(listOf("あ", "い")), tmp.toFile(), charset = "SJIS")
            val text = String(Files.readAllBytes(tmp), Charset.forName("Shift_JIS"))
            text shouldBe "あ,い\r\n"
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun write_stream_callerOwnedStream_isNeitherClosedNorImplicitlyMutated() {
        val raw = ByteArrayOutputStream()
        val counting = CountingOutputStream(raw)
        CsvWriter().write(sampleRows, counting)
        raw.toByteArray().toString(Charsets.UTF_8) shouldBe sampleRowsEncoded
        // Caller owns the stream — overload must not close it.
        counting.closeCount shouldBe 0
    }

    @Test
    fun write_stream_flushesUnderlyingStreamAtLeastOnce() {
        // The writer must flush the OutputStreamWriter at the end so callers
        // see all bytes without waiting for close(). Flushing the writer
        // propagates to the underlying OutputStream, so the count here is at
        // least 1.
        val counting = CountingOutputStream(ByteArrayOutputStream())
        CsvWriter().write(sampleRows, counting)
        counting.flushCount shouldBeGreaterThanOrEqual 1
    }

    @Test
    fun write_stream_prependBomUtf8_emitsEfBbBfPrefix() {
        val raw = ByteArrayOutputStream()
        CsvWriter().write(sampleRows, raw, options = CsvWriteIoOptions(prependBom = true))
        val out = raw.toByteArray()
        out.copyOfRange(0, 3).toList() shouldBe
            listOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        out.copyOfRange(3, out.size).toString(Charsets.UTF_8) shouldBe sampleRowsEncoded
    }

    @Test
    fun write_stream_prependBomShiftJis_emitsEncoderSubstitution() {
        // Shift_JIS has no BOM concept; Java's default unmappable-character
        // policy substitutes U+FEFF with `?` (0x3F). Pin this behaviour so a
        // future change is visible in tests.
        val raw = ByteArrayOutputStream()
        CsvWriter().write(
            listOf(listOf("a")),
            raw,
            charset = "Shift_JIS",
            options = CsvWriteIoOptions(prependBom = true),
        )
        val out = raw.toByteArray()
        out[0] shouldBe '?'.code.toByte()
        out.copyOfRange(1, out.size).toString(Charset.forName("Shift_JIS")) shouldBe "a\r\n"
    }

    @Test
    fun write_stream_listOverloadMatchesSequenceOverload() {
        val rawList = ByteArrayOutputStream()
        CsvWriter().write(sampleRows, rawList)
        val rawSeq = ByteArrayOutputStream()
        CsvWriter().write(sampleRows.asSequence(), rawSeq)
        rawList.toByteArray().toList() shouldBe rawSeq.toByteArray().toList()
    }

    @Test
    fun write_stream_largeOutputCrossesChunkBoundary() {
        // Force the chunked flushing path: produce >8192 chars of output.
        val row = List(200) { "x" }
        val rows = List(50) { row }
        val raw = ByteArrayOutputStream()
        CsvWriter().write(rows, raw)
        // Each row: 200 fields × 1 char + 199 commas = 399 chars + "\r\n"; 50 rows × 401 = 20050 bytes.
        raw.toByteArray().size shouldBe 50 * (200 + 199 + 2)
    }
}
