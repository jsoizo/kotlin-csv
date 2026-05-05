package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.reader.internal.FakeRawSource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ReaderIoTest {

    private fun csvBytes(text: String): ByteArray = text.encodeToByteArray()

    private fun bomBytes(): ByteArray = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    @Test
    fun read_source_basicCsv() {
        val source = FakeRawSource(csvBytes("a,b,c\nd,e,f")).buffered()
        val reader = CsvReader()
        val rows = reader.read(source) { seq -> seq.toList() }
        rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
    }

    @Test
    fun read_source_stripBomDefault_dropsLeadingBom() {
        val source = FakeRawSource(bomBytes() + csvBytes("a,b,c")).buffered()
        val reader = CsvReader()
        val rows = reader.read(source) { seq -> seq.toList() }
        rows shouldBe listOf(listOf("a", "b", "c"))
    }

    @Test
    fun read_source_stripBomFalse_doesNotStripAtIoLayer() {
        // Note: even with stripBom = false, the parser (ParseStateMachine) skips a
        // leading U+FEFF in its START state, so BOM never appears in the first
        // field. This test asserts parity with stripBom = true for the trivial
        // "BOM at file start" case. The I/O-layer stripBom flag becomes
        // user-visible only after the parser stops doing this; that change is
        // tracked separately.
        val source = FakeRawSource(bomBytes() + csvBytes("a,b,c")).buffered()
        val reader = CsvReader()
        val rows = reader.read(source, CsvReadIoOptions(stripBom = false)) { seq -> seq.toList() }
        rows shouldBe listOf(listOf("a", "b", "c"))
    }

    @Test
    fun read_source_blockReturnValueIsPropagated() {
        val source = FakeRawSource(csvBytes("a\nb")).buffered()
        val reader = CsvReader()
        val count: Int = reader.read(source) { seq -> seq.count() }
        count shouldBe 2
    }

    @Test
    fun read_source_closesUnderlyingRawSourceOnNormalReturn() {
        val raw = FakeRawSource(csvBytes("a,b\nc,d"))
        raw.buffered().use { source ->
            CsvReader().read(source) { seq -> seq.toList() }
        }
        raw.closeCount shouldBe 1
    }

    @Test
    fun read_source_closesUnderlyingRawSourceWhenBlockTakesPrefix() {
        val raw = FakeRawSource(csvBytes("a\nb\nc\nd"))
        raw.buffered().use { source ->
            CsvReader().read(source) { seq ->
                seq.take(1).toList() shouldBe listOf(listOf("a"))
            }
        }
        raw.closeCount shouldBe 1
    }

    @Test
    fun read_source_closesUnderlyingRawSourceWhenBlockThrows() {
        val raw = FakeRawSource(csvBytes("a,b\nc,d"))
        assertFailsWith<IllegalStateException> {
            raw.buffered().use { source ->
                CsvReader().read(source) { _ ->
                    error("boom")
                }
            }
        }
        raw.closeCount shouldBe 1
    }

    @Test
    fun read_source_emptyInputProducesNoRows() {
        val source = FakeRawSource(ByteArray(0)).buffered()
        val reader = CsvReader()
        val rows = reader.read(source) { seq -> seq.toList() }
        rows shouldBe emptyList()
    }

    @Test
    fun read_source_bomOnEmptyBodyWithStripBomReturnsEmpty() {
        val source = FakeRawSource(bomBytes()).buffered()
        val reader = CsvReader()
        val rows = reader.read(source) { seq -> seq.toList() }
        rows shouldBe emptyList()
    }

    @Test
    fun fakeRawSource_isUsableMultipleTimesWithDifferentInstances() {
        // Sanity check that closeCount tracking is per-instance, so other tests
        // are not affected by shared state.
        val a = FakeRawSource(csvBytes(""))
        val b = FakeRawSource(csvBytes(""))
        a.close()
        a.closeCount shouldBe 1
        b.closeCount shouldBe 0
        a shouldNotBe b
    }
}
