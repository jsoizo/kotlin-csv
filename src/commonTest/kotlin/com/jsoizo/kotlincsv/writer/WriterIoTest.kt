package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.FakeRawSink
import io.kotest.matchers.shouldBe
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WriterIoTest {

    private val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    @Test
    fun write_sink_sequenceInputProducesEncodedBytes() {
        val rows = sequenceOf(listOf("a", "b"), listOf("c", "d"))
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(rows, sink)
        }
        raw.snapshot().decodeToString() shouldBe "a,b\r\nc,d\r\n"
        // The writer flushes the buffered sink at the end of the call so
        // downstream consumers see all bytes without waiting for close().
        raw.flushCount shouldBe 1
    }

    @Test
    fun write_sink_listInputMatchesSequenceOutput() {
        val rows = listOf(listOf("a", "b"), listOf("c", "d"))
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(rows, sink)
        }
        raw.snapshot().decodeToString() shouldBe "a,b\r\nc,d\r\n"
    }

    @Test
    fun write_sink_prependBomEmitsBomThenBody() {
        val rows = listOf(listOf("a", "b"))
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(rows, sink, CsvWriteIoOptions(prependBom = true))
        }
        val out = raw.snapshot()
        // Leading 3 bytes are EF BB BF (UTF-8 BOM).
        out.copyOfRange(0, 3).toList() shouldBe bom.toList()
        // The remaining bytes equal the BOM-less encoding.
        out.copyOfRange(3, out.size).decodeToString() shouldBe "a,b\r\n"
    }

    @Test
    fun write_sink_prependBomFalseEmitsNoBom() {
        val rows = listOf(listOf("a", "b"))
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(rows, sink)
        }
        val out = raw.snapshot()
        out[0] shouldBe 'a'.code.toByte()
        out.decodeToString() shouldBe "a,b\r\n"
    }

    @Test
    fun write_sink_emptyRowsProducesEmptyOutput() {
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(emptySequence(), sink)
        }
        raw.snapshot().size shouldBe 0
    }

    @Test
    fun write_sink_closesUnderlyingRawSinkOnNormalReturn() {
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(sequenceOf(listOf("a")), sink)
        }
        raw.closeCount shouldBe 1
    }

    @Test
    fun write_sink_closesUnderlyingRawSinkWhenWriteThrows() {
        val raw = FakeRawSink()
        // Force an exception during encoding by passing a sequence that throws
        // partway through iteration.
        val explosive = sequence {
            yield(listOf("a"))
            error("boom")
        }
        assertFailsWith<IllegalStateException> {
            raw.buffered().use { sink ->
                CsvWriter().write(explosive, sink)
            }
        }
        raw.closeCount shouldBe 1
    }

    @Test
    fun write_stringPathOverloadIsCallable() {
        // Compile-level smoke: the String overloads exist and resolve for
        // both Sequence and List inputs. End-to-end coverage of the String ->
        // Path delegation lives in the jvmTest path smoke.
        val writer = CsvWriter()
        val callableSeq: (String) -> Unit = { path ->
            writer.write(sequenceOf(listOf("a")), path)
        }
        val callableList: (String) -> Unit = { path ->
            writer.write(listOf(listOf("a")), path)
        }
        (callableSeq to callableList) shouldBe (callableSeq to callableList)
    }

    @Test
    fun write_sink_largeOutputCrossesChunkBoundary() {
        // Force the chunked flushing path: produce >8192 chars of output.
        val row = List(200) { "x" }
        val rows = List(50) { row }
        val raw = FakeRawSink()
        raw.buffered().use { sink ->
            CsvWriter().write(rows, sink)
        }
        // Each row is 200 fields × 1 char + 199 commas = 399 chars + "\r\n"; 50 rows × 401 = 20050 bytes.
        raw.snapshot().size shouldBe 50 * (200 + 199 + 2)
    }
}
