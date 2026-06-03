package com.jsoizo.kotlincsv.reader

import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.writeString

class CsvReaderWasmJsPathSmokeTest {

    private fun tempCsvPath(suffix: String): Path =
        Path(SystemTemporaryDirectory, "kotlin-csv-wasmjs-reader-$suffix-${Random.nextLong().toString(16)}.csv")

    private fun writeFile(path: Path, content: String) {
        SystemFileSystem.sink(path).buffered().use { sink ->
            sink.writeString(content)
        }
    }

    @Test
    fun readFromFile_path_basic_decodesRows() {
        val path = tempCsvPath("basic")
        try {
            writeFile(path, "a,b,c\nd,e,f")
            val rows = CsvReader().readFromFile(path) { it.toList() }
            rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun readFromFile_path_utf8NonAscii_decodesCorrectly() {
        val path = tempCsvPath("utf8")
        try {
            writeFile(path, "あ,い\nう,え")
            val rows = CsvReader().readFromFile(path) { it.toList() }
            rows shouldBe listOf(listOf("あ", "い"), listOf("う", "え"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun readFromFile_path_stripBomDefault_dropsLeadingBom() {
        val path = tempCsvPath("bom")
        try {
            // U+FEFF written through writeString lands on disk as the EF BB BF
            // UTF-8 BOM sequence; the default stripBom = true should drop it.
            writeFile(path, "\uFEFFa,b,c")
            val rows = CsvReader().readFromFile(path) { it.toList() }
            rows shouldBe listOf(listOf("a", "b", "c"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun readFromFile_path_supplementaryPlaneEmoji_decodesAsSurrogatePair() {
        // U+1F600 sits above U+FFFF, so the UTF-8 -> Char decode has to emit
        // a high/low surrogate pair to round-trip the grapheme through
        // Sequence<Char>. The wasmJs fs path must preserve the 4-byte UTF-8
        // sequence end-to-end.
        val path = tempCsvPath("emoji")
        try {
            val emoji = "😀"
            writeFile(path, "$emoji,b")
            val rows = CsvReader().readFromFile(path) { it.toList() }
            rows shouldBe listOf(listOf(emoji, "b"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun readFromFile_stringPath_overload_resolvesViaPath() {
        val path = tempCsvPath("strpath")
        try {
            writeFile(path, "x,y\n1,2")
            val rows = CsvReader().readFromFile(path.toString()) { it.toList() }
            rows shouldBe listOf(listOf("x", "y"), listOf("1", "2"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }
}
