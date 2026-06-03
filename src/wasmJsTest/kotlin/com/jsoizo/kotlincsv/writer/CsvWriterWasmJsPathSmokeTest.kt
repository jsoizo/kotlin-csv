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

class CsvWriterWasmJsPathSmokeTest {

    private fun tempCsvPath(suffix: String): Path =
        Path(SystemTemporaryDirectory, "kotlin-csv-wasmjs-writer-$suffix-${Random.nextLong().toString(16)}.csv")

    private fun readText(path: Path): String =
        SystemFileSystem.source(path).buffered().use { it.readString() }

    private fun readBytes(path: Path): ByteArray =
        SystemFileSystem.source(path).buffered().use { it.readByteArray() }

    @Test
    fun writeToFile_path_basic_writesFile() {
        val path = tempCsvPath("basic")
        try {
            val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            CsvWriter().writeToFile(rows, path)
            readText(path) shouldBe "a,b,c\r\nd,e,f\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun writeToFile_path_utf8NonAscii_roundTrips() {
        val path = tempCsvPath("utf8")
        try {
            val rows = listOf(listOf("あ", "い"), listOf("う", "え"))
            CsvWriter().writeToFile(rows, path)
            readText(path) shouldBe "あ,い\r\nう,え\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun writeToFile_path_prependBom_emitsBomBytes() {
        val path = tempCsvPath("bom")
        try {
            CsvWriter().writeToFile(
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
    fun writeToFile_path_supplementaryPlaneEmoji_roundTrips() {
        val path = tempCsvPath("emoji")
        try {
            val emoji = "😀"
            CsvWriter().writeToFile(listOf(listOf(emoji, "b")), path)
            readText(path) shouldBe "$emoji,b\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun writeToFile_stringPath_overload_resolvesViaPath() {
        val path = tempCsvPath("strpath")
        try {
            val rows = listOf(listOf("x", "y"), listOf("1", "2"))
            CsvWriter().writeToFile(rows, path.toString())
            readText(path) shouldBe "x,y\r\n1,2\r\n"
        } finally {
            SystemFileSystem.delete(path)
        }
    }
}
