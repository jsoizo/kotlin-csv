package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.dialectArb
import com.jsoizo.kotlincsv.fieldChar
import com.jsoizo.kotlincsv.quoteModeArb
import com.jsoizo.kotlincsv.rowsArb
import com.jsoizo.kotlincsv.supplementaryFieldToken
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.take
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class WriterIoPropertyTest {

    private data class BoundaryCase(
        val config: CsvWriterConfig,
        val targetIndex: Int,
        val fieldPrefix: String,
        val pair: String,
        val prefixNoise: String,
    )

    private val generatedCases = Arb.bind(
        Arb.bind(dialectArb, Arb.boolean(), quoteModeArb, ::CsvWriterConfig),
        rowsArb,
    ) { config, rows -> config to rows }
        .take(200, RandomSource.seeded(192L)).toList()

    private val boundaryCases = buildList {
        val pairs = listOf("\uD800\uDC00", "\uDBFF\uDFFF") +
            supplementaryFieldToken.take(2, RandomSource.seeded(192L)).toList()
        val prefixNoise = fieldChar.take(5, RandomSource.seeded(193L)).joinToString("")
        for (dialect in listOf(CsvDialect.RFC4180, CsvDialect.TSV, CsvDialect(escapeChar = '\\'))) {
            for (quoteMode in WriteQuoteMode.entries) {
                for (boundary in listOf(8192, 16384, 24576)) {
                    for (offset in -2..0) {
                        for (fieldPrefix in listOf("", "special,\"\\\t\r\n")) {
                            for (pair in pairs) {
                                add(BoundaryCase(
                                    CsvWriterConfig(dialect = dialect, quoteMode = quoteMode),
                                    boundary + offset,
                                    fieldPrefix,
                                    pair,
                                    prefixNoise,
                                ))
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun write_sink_generatedRowsMatchOneShotUtf8() {
        for ((index, case) in generatedCases.withIndex()) {
            val (config, rows) = case
            val writer = CsvWriter(config)
            val sink = Buffer()

            writer.write(rows.asSequence(), sink)

            assertSameBytes(
                writer.writeAll(rows).encodeToByteArray(),
                sink.readByteArray(),
                "regular generated case=$index config=$config",
            )
        }
    }

    @Test
    fun writeNullable_sink_generatedRowsMatchOneShotUtf8() {
        for ((index, case) in generatedCases.withIndex()) {
            val (config, fields) = case
            val rows = fields.map { row -> listOf(null) + row + null }
            val writer = CsvWriter(config)
            val sink = Buffer()

            writer.writeNullable(rows.asSequence(), sink)

            assertSameBytes(
                writer.writeAllNullable(rows).encodeToByteArray(),
                sink.readByteArray(),
                "nullable generated case=$index config=$config",
            )
        }
    }

    @Test
    fun write_sink_supplementaryPairsAroundChunkBoundariesMatchOneShotUtf8() {
        for (case in boundaryCases) {
            val writer = CsvWriter(case.config)
            val priorRows = listOf(listOf("prior,\"\\\r\n${case.prefixNoise}", "123"))
            val priorFields = listOf("before,\"\\\t", "456")
            val followingRows = listOf(listOf("after", "quote\"slash\\"))
            val serialize = { field: String ->
                writer.writeAll(priorRows + listOf(priorFields + field) + followingRows)
            }
            val field = alignedField(case, serialize)
            val rows = priorRows + listOf(priorFields + field) + followingRows
            val sink = Buffer()

            writer.write(rows, sink)

            assertSameBytes(serialize(field).encodeToByteArray(), sink.readByteArray(), "regular $case")
        }
    }

    @Test
    fun writeNullable_sink_supplementaryPairsAroundChunkBoundariesMatchOneShotUtf8() {
        for (case in boundaryCases) {
            val writer = CsvWriter(case.config)
            val priorRows = listOf(listOf(null, "prior,\"\\\r\n${case.prefixNoise}", null))
            val priorFields = listOf(null, "before,\"\\\t", null)
            val followingRows = listOf(listOf("after", null, "quote\"slash\\"))
            val serialize = { field: String ->
                writer.writeAllNullable(priorRows + listOf(priorFields + field) + followingRows)
            }
            val field = alignedField(case, serialize)
            val rows = priorRows + listOf(priorFields + field) + followingRows
            val sink = Buffer()

            writer.writeNullable(rows, sink)

            assertSameBytes(serialize(field).encodeToByteArray(), sink.readByteArray(), "nullable $case")
        }
    }

    private fun alignedField(case: BoundaryCase, serialize: (String) -> String): String {
        val tail = "z".repeat(16384 + 17)
        val unpadded = case.fieldPrefix + case.pair + tail
        val pairIndex = serialize(unpadded).indexOf(case.pair)
        assertTrue(pairIndex in 0..case.targetIndex, "Cannot align $case, pairIndex=$pairIndex")
        val field = case.fieldPrefix + "a".repeat(case.targetIndex - pairIndex) + case.pair + tail
        val text = serialize(field)
        assertEquals(case.targetIndex, text.indexOf(case.pair), "Serialized pair position for $case")
        assertTrue(
            text.take(case.targetIndex).none { it.isSurrogate() },
            "Earlier surrogates would shift later chunk boundaries for $case",
        )
        return field
    }

    private fun assertSameBytes(expected: ByteArray, actual: ByteArray, context: String) {
        if (expected.contentEquals(actual)) return
        val commonSize = minOf(expected.size, actual.size)
        val mismatch = (0 until commonSize).firstOrNull { expected[it] != actual[it] } ?: commonSize
        fail(
            "$context, expectedBytes=${expected.size}, actualBytes=${actual.size}, " +
                "firstMismatch=$mismatch, expectedByte=${expected.getOrNull(mismatch)}, " +
                "actualByte=${actual.getOrNull(mismatch)}",
        )
    }
}
