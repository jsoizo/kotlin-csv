package com.jsoizo.kotlincsv.reader

import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.writeString

class CsvReaderJsPathSmokeTest {

    private fun tempCsvPath(suffix: String): Path =
        Path(SystemTemporaryDirectory, "kotlin-csv-js-reader-$suffix-${Random.nextLong().toString(16)}.csv")

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
            writeFile(path, "\uFEFFa,b,c")
            val rows = CsvReader().readFromFile(path) { it.toList() }
            rows shouldBe listOf(listOf("a", "b", "c"))
        } finally {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun readFromFile_path_supplementaryPlaneEmoji_decodesAsSurrogatePair() {
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
