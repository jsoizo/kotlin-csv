package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.reader.internal.CountingInputStream
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CsvReaderJvmIoTest {

    private val sampleCsv = "a,b,c\nd,e,f"
    private val sampleRows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))

    @Test
    fun read_file_utf8_basic_decodesRows() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.writeString(tmp, sampleCsv, Charsets.UTF_8)
            val rows = CsvReader().read(tmp.toFile()) { it.toList() }
            rows shouldBe sampleRows
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun read_file_shiftJis_decodesJapaneseCharacters() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, "あ,い\n".toByteArray(Charset.forName("Shift_JIS")))
            val rows = CsvReader().read(tmp.toFile(), charset = "Shift_JIS") { it.toList() }
            rows shouldBe listOf(listOf("あ", "い"))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun read_file_sjisAlias_resolvesToShiftJis() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.write(tmp, "あ,い\n".toByteArray(Charset.forName("Shift_JIS")))
            val rows = CsvReader().read(tmp.toFile(), charset = "SJIS") { it.toList() }
            rows shouldBe listOf(listOf("あ", "い"))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun read_stream_utf8_basic_decodesRows() {
        val raw = sampleCsv.toByteArray(Charsets.UTF_8)
        val counting = CountingInputStream(ByteArrayInputStream(raw))
        val rows = CsvReader().read(counting) { it.toList() }
        rows shouldBe sampleRows
        // Caller still owns the stream — overload must not close it.
        counting.closeCount shouldBe 0
    }

    @Test
    fun read_stream_takeShortCircuit_doesNotCloseCallerOwnedStream() {
        val raw = "first\nsecond\nthird".toByteArray(Charsets.UTF_8)
        val counting = CountingInputStream(ByteArrayInputStream(raw))
        val first = CsvReader().read(counting) { it.first() }
        first shouldBe listOf("first")
        counting.closeCount shouldBe 0
    }

    @Test
    fun read_stream_takeShortCircuit_stopsPullingBytesEarly() {
        // Lazy-pull contract: when block consumes only the first row, the
        // overload must not drain the entire underlying stream. Padding has
        // to exceed BufferedReader's fill-buffer size (8 KiB) so the early
        // exit is observable as a strict-less-than byte count.
        val firstRow = "first\n"
        val padding = "x".repeat(64 * 1024)
        val payload = (firstRow + padding).toByteArray(Charsets.UTF_8)
        val counting = CountingInputStream(ByteArrayInputStream(payload))
        CsvReader().read(counting) { it.first() }
        (counting.bytesRead < payload.size.toLong()) shouldBe true
    }

    @Test
    fun read_stream_blockThrows_doesNotCloseCallerOwnedStream() {
        val raw = sampleCsv.toByteArray(Charsets.UTF_8)
        val counting = CountingInputStream(ByteArrayInputStream(raw))
        assertFailsWith<IllegalStateException> {
            CsvReader().read(counting) { _ -> error("boom") }
        }
        counting.closeCount shouldBe 0
    }

    @Test
    fun read_stream_utf8BomStrippedByDefault() {
        val raw = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "a,b,c".toByteArray(Charsets.UTF_8)
        val rows = CsvReader().read(ByteArrayInputStream(raw)) { it.toList() }
        rows shouldBe listOf(listOf("a", "b", "c"))
    }

    @Test
    fun read_stream_utf8BomKeptWhenStripBomFalse() {
        val raw = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "a,b,c".toByteArray(Charsets.UTF_8)
        val rows = CsvReader().read(
            ByteArrayInputStream(raw),
            options = CsvReadIoOptions(stripBom = false),
        ) { it.toList() }
        // BOM survives charset decode and ends up as the first char of the
        // first field when stripBom = false.
        rows shouldBe listOf(listOf("\uFEFFa", "b", "c"))
    }

    @Test
    fun read_stream_utf16_bomStrippedAfterCharsetDecode() {
        // UTF-16 encodes its own BOM as bytes; after charset decoding the
        // resulting char stream starts with U+FEFF, which the post-decode
        // strip removes.
        val raw = "a,b,c".toByteArray(Charset.forName("UTF-16"))
        val rows = CsvReader().read(
            ByteArrayInputStream(raw),
            charset = "UTF-16",
        ) { it.toList() }
        rows shouldBe listOf(listOf("a", "b", "c"))
    }

    @Test
    fun read_stream_invalidCharsetName_propagatesUnsupportedCharsetException() {
        // Pin behaviour: Charset.forName failures are not wrapped or swallowed
        // by the I/O overload — they surface to the caller as-is.
        assertFailsWith<UnsupportedCharsetException> {
            CsvReader().read(
                ByteArrayInputStream(ByteArray(0)),
                charset = "definitely-not-a-charset",
            ) { it.toList() }
        }
    }

    @Test
    fun readAll_file_utf8_basic_decodesRows() {
        val tmp = Files.createTempFile("kotlin-csv-jvm-reader", ".csv")
        try {
            Files.writeString(tmp, sampleCsv, Charsets.UTF_8)
            CsvReader().readAll(tmp.toFile()) shouldBe sampleRows
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readAll_stream_utf8_basic_decodesRowsAndDoesNotCloseCallerStream() {
        val raw = sampleCsv.toByteArray(Charsets.UTF_8)
        val counting = CountingInputStream(ByteArrayInputStream(raw))
        CsvReader().readAll(counting) shouldBe sampleRows
        counting.closeCount shouldBe 0
    }
}
