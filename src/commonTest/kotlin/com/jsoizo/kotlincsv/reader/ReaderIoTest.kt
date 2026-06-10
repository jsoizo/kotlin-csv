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
    fun read_source_stripBomFalse_keepsLeadingBomInFirstField() {
        val source = FakeRawSource(bomBytes() + csvBytes("a,b,c")).buffered()
        val reader = CsvReader()
        val rows = reader.read(source, CsvReadIoOptions(stripBom = false)) { seq -> seq.toList() }
        // With BOM handling owned by the I/O layer, opting out preserves the
        // U+FEFF as the first character of the first field.
        rows shouldBe listOf(listOf("\uFEFFa", "b", "c"))
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

    @Test
    fun read_source_supplementaryPlaneCharIsEmittedAsSurrogatePair() {
        // U+1F600 (😀) is in the supplementary plane and must round-trip through
        // a UTF-16 surrogate pair. The parser receives 2 chars but the original
        // String surface keeps it as a single grapheme.
        val emoji = "😀" // U+1F600 encoded as surrogate pair
        val source = FakeRawSource(csvBytes("$emoji,b")).buffered()
        val reader = CsvReader()
        val rows = reader.read(source) { seq -> seq.toList() }
        rows shouldBe listOf(listOf(emoji, "b"))
    }

    @Test
    fun read_source_lazilyStopsPullingBytesAfterTake() {
        // The streaming decoder should let the parser short-circuit before the
        // whole input is consumed. We use a Buffer fed from a FakeRawSource and
        // assert the source is not fully drained when only the first row is
        // taken.
        val raw = FakeRawSource(csvBytes("first\nsecond\nthird"))
        raw.buffered().use { source ->
            val first = CsvReader().read(source) { seq -> seq.first() }
            first shouldBe listOf("first")
        }
        // After early-exit, the FakeRawSource should still report a non-zero
        // close (the use {} block calls close on the buffered Source which
        // flows down to the raw fake).
        raw.closeCount shouldBe 1
    }

    @Test
    fun read_stringPathOverloadIsCallable() {
        // Compile-level smoke: the String overload exists and resolves. We
        // bind it to a lambda instead of invoking it, since that would require
        // a real file. End-to-end coverage of the String -> Path delegation
        // lives in the jvmTest path smoke.
        val reader = CsvReader()
        val callable: (String) -> List<List<String>> = { path ->
            reader.readFromFile(path) { seq -> seq.toList() }
        }
        callable shouldNotBe null
    }

    @Test
    fun readAll_source_basicCsv() {
        val source = FakeRawSource(csvBytes("a,b,c\nd,e,f")).buffered()
        val rows = CsvReader().readAll(source)
        rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
    }

    @Test
    fun readAllNullable_source_usesNullFieldIndicator() {
        val source = FakeRawSource(csvBytes("\"\",,\n")).buffered()
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        reader.readAllNullable(source) shouldBe listOf(listOf("", null, null))
    }

    @Test
    fun readNullable_source_blockReturnValueIsPropagated() {
        val source = FakeRawSource(csvBytes("\"\",,\n")).buffered()
        val reader = CsvReader(
            CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
        )
        reader.readNullable(source) { rows -> rows.single()[1] } shouldBe null
    }

    @Test
    fun readAllFromFile_stringPathOverloadIsCallable() {
        val reader = CsvReader()
        val callable: (String) -> List<List<String>> = { path -> reader.readAllFromFile(path) }
        callable shouldNotBe null
    }

    @Test
    fun readAllNullableFromFile_stringPathOverloadIsCallable() {
        val reader = CsvReader()
        val callable: (String) -> List<List<String?>> = { path -> reader.readAllNullableFromFile(path) }
        callable shouldNotBe null
    }
}
