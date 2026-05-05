package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.CsvReadIoOptions
import com.jsoizo.kotlincsv.reader.CsvReader
import com.jsoizo.kotlincsv.reader.internal.FakeRawSource
import com.jsoizo.kotlincsv.reader.read
import com.jsoizo.kotlincsv.writer.CsvWriteIoOptions
import com.jsoizo.kotlincsv.writer.CsvWriter
import com.jsoizo.kotlincsv.writer.internal.FakeRawSink
import com.jsoizo.kotlincsv.writer.write
import io.kotest.matchers.shouldBe
import kotlinx.io.buffered
import kotlin.test.Test

class IoRoundTripTest {

    private val sampleRows = listOf(
        listOf("alpha", "beta", "gamma"),
        listOf("1", "2", "3"),
        listOf("with,comma", "with\"quote", "plain"),
    )

    @Test
    fun roundTrip_noBom_writeThenRead_recoversInputRows() {
        val sinkRaw = FakeRawSink()
        sinkRaw.buffered().use { sink ->
            CsvWriter().write(sampleRows, sink)
        }
        val sourceRaw = FakeRawSource(sinkRaw.snapshot())
        val readBack = sourceRaw.buffered().use { source ->
            CsvReader().read(source) { seq -> seq.toList() }
        }
        readBack shouldBe sampleRows
    }

    @Test
    fun roundTrip_withBomPrependedAndStripped_recoversInputRows() {
        val sinkRaw = FakeRawSink()
        sinkRaw.buffered().use { sink ->
            CsvWriter().write(sampleRows, sink, CsvWriteIoOptions(prependBom = true))
        }
        // Body should start with the UTF-8 BOM bytes.
        sinkRaw.snapshot().copyOfRange(0, 3).toList() shouldBe
            listOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

        val sourceRaw = FakeRawSource(sinkRaw.snapshot())
        val readBack = sourceRaw.buffered().use { source ->
            CsvReader().read(source, CsvReadIoOptions(stripBom = true)) { seq -> seq.toList() }
        }
        readBack shouldBe sampleRows
    }
}
