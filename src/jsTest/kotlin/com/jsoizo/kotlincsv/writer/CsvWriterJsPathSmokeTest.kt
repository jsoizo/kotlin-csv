package com.jsoizo.kotlincsv.writer

import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray
import kotlinx.io.readString

class CsvWriterJsPathSmokeTest {

    private fun tempCsvPath(suffix: String): Path =
        Path(SystemTemporaryDirectory, "kotlin-csv-js-writer-$suffix-${Random.nextLong().toString(16)}.csv")

    private fun readText(path: Path): String =
        SystemFileSystem.source(path).buffered().use { it.readString() }

    private fun readBytes(path: Path): ByteArray =
        SystemFileSystem.source(path).buffered().use { it.readByteArray() }

    @Test
    fun write_path_basic_writesFile() {
        val path = tempCsvPath("basic")
        try {
            val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            CsvWriter().write(rows, path)
            readText(path) shouldBe "a,b,c\r\nd,e,f\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun write_path_utf8NonAscii_roundTrips() {
        val path = tempCsvPath("utf8")
        try {
            val rows = listOf(listOf("あ", "い"), listOf("う", "え"))
            CsvWriter().write(rows, path)
            readText(path) shouldBe "あ,い\r\nう,え\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun write_path_prependBom_emitsBomBytes() {
        val path = tempCsvPath("bom")
        try {
            CsvWriter().write(
                listOf(listOf("a", "b", "c")),
                path,
                options = CsvWriteIoOptions(prependBom = true),
            )
            val bytes = readBytes(path)
            bytes.copyOfRange(0, 3).toList() shouldBe
                listOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            bytes.size shouldBe 3 + "a,b,c\r\n".encodeToByteArray().size
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun write_path_supplementaryPlaneEmoji_roundTrips() {
        // U+1F600 surfaces in the input as a UTF-16 surrogate pair; the
        // encoder has to recombine the pair into the original 4-byte UTF-8
        // sequence on disk so a round-trip through Node.js fs preserves the
        // grapheme.
        val path = tempCsvPath("emoji")
        try {
            val emoji = "😀"
            CsvWriter().write(listOf(listOf(emoji, "b")), path)
            readText(path) shouldBe "$emoji,b\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun write_stringPath_overload_resolvesViaPath() {
        val path = tempCsvPath("strpath")
        try {
            val rows = listOf(listOf("x", "y"), listOf("1", "2"))
            CsvWriter().write(rows, path.toString())
            readText(path) shouldBe "x,y\r\n1,2\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }
}
