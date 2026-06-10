package com.jsoizo.kotlincsv.reader

import io.kotest.matchers.shouldBe
import kotlinx.io.files.Path as KxPath
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test

class CsvReaderPathSmokeTest {

    @Test
    fun readFromFile_stringPath_readsRealTempFile() {
        // End-to-end smoke that exercises the String -> Path -> Source delegation
        // chain against a real on-disk file.
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke", ".csv")
        try {
            Files.writeString(tmp, "a,b,c\nd,e,f")
            val reader = CsvReader()
            val rows = reader.readFromFile(tmp.toString()) { seq -> seq.toList() }
            rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readAllNullableFromFile_stringPath_readsNullFieldsFromRealTempFile() {
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke-nullable", ".csv")
        try {
            Files.writeString(tmp, "\"empty\",\"null\"\n\"\",\n")
            val reader = CsvReader(
                CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
            )
            reader.readAllNullableFromFile(tmp.toString()) shouldBe
                listOf(listOf("empty", "null"), listOf("", null))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readNullableFromFile_kotlinxIoPath_readsNullFieldsFromRealTempFile() {
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke-nullable-path", ".csv")
        try {
            Files.writeString(tmp, "\"empty\",\"null\"\n\"\",\n")
            val reader = CsvReader(
                CsvReaderConfig(nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS)
            )
            val path = KxPath(tmp.toString())
            reader.readNullableFromFile(path) { rows -> rows.toList() } shouldBe
                listOf(listOf("empty", "null"), listOf("", null))
            reader.readAllNullableFromFile(path) shouldBe
                listOf(listOf("empty", "null"), listOf("", null))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readFromFile_kotlinxIoPath_defaultOptions_decodesRows() {
        // Direct call into the kotlinx-io Path overloads with options omitted,
        // covering the default-argument synthetic methods on readFromFile and
        // readAllFromFile.
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke-defaults", ".csv")
        try {
            Files.writeString(tmp, "a,b\nc,d")
            val path = KxPath(tmp.toString())
            CsvReader().readFromFile(path) { it.toList() } shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
            CsvReader().readAllFromFile(path) shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readFromFile_stringPath_inputLargerThanChunkBuffer_drivesKotlinxIoLimitBranch() {
        // The kotlinx-io chunk reader stops one slot before the buffer end, so
        // it has to exit on `index >= limit` at least once. Default buffer is
        // 8 KB; pump well past that to take the limit branch on a real file.
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke-large", ".csv")
        try {
            val rowCount = 3_000
            val csv = buildString {
                repeat(rowCount) { row -> append("r$row,col1,col2,col3\n") }
            }
            Files.writeString(tmp, csv)
            val rows = CsvReader().readFromFile(tmp.toString()) { seq -> seq.toList() }
            rows.size shouldBe rowCount
            rows.first() shouldBe listOf("r0", "col1", "col2", "col3")
            rows.last() shouldBe listOf("r${rowCount - 1}", "col1", "col2", "col3")
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun readFromFile_supplementaryCodePoint_atChunkBoundary_keepsSurrogatePairTogether() {
        // Source.asChunkReader reserves the last slot of the 8 KB buffer so a
        // supplementary code point's UTF-16 surrogate pair never spans two
        // chunks. Lay out the input so 😀's high surrogate lands at the
        // last reservable index (buffer.size - 2): 8189 pad chars + ',' fills
        // indices 0..8189, the high surrogate goes to 8190, the low surrogate
        // to 8191 (the reserved slot). If the limit regressed to
        // buffer.size, writing the low surrogate at 8192 would overflow.
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke-supplementary", ".csv")
        try {
            val pad = "x".repeat(8189)
            val csv = "$pad,😀\n"
            Files.writeString(tmp, csv)
            val rows = CsvReader().readFromFile(tmp.toString()) { it.toList() }
            rows shouldBe listOf(listOf(pad, "😀"))
        } finally {
            tmp.deleteIfExists()
        }
    }
}
