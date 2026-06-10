package com.jsoizo.kotlincsv.writer

import io.kotest.matchers.shouldBe
import kotlinx.io.files.Path as KxPath
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test

class CsvWriterPathSmokeTest {

    @Test
    fun writeToFile_stringPath_writesRealTempFile() {
        // End-to-end smoke that exercises the String -> Path -> Sink delegation
        // chain against a real on-disk file. Mirrors CsvReaderPathSmokeTest.
        val tmp = Files.createTempFile("kotlin-csv-writer-smoke", ".csv")
        try {
            val writer = CsvWriter()
            writer.writeToFile(listOf(listOf("a", "b", "c"), listOf("d", "e", "f")), tmp.toString())
            Files.readString(tmp) shouldBe "a,b,c\r\nd,e,f\r\n"
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun writeNullableToFile_stringPath_writesNullFieldsToRealTempFile() {
        val tmp = Files.createTempFile("kotlin-csv-writer-smoke-nullable", ".csv")
        try {
            val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
            writer.writeNullableToFile(listOf(listOf<String?>(null, "", "x")), tmp.toString())
            Files.readString(tmp) shouldBe ",\"\",\"x\"\r\n"
        } finally {
            tmp.deleteIfExists()
        }
    }

    @Test
    fun writeNullableToFile_kotlinxIoPath_writesNullFieldsToRealTempFile() {
        val tmp = Files.createTempFile("kotlin-csv-writer-smoke-nullable-path", ".csv")
        try {
            val writer = CsvWriter(CsvWriterConfig(quoteMode = WriteQuoteMode.ALL))
            val path = KxPath(tmp.toString())
            writer.writeNullableToFile(sequenceOf(listOf<String?>(null, "", "x")), path)
            Files.readString(tmp) shouldBe ",\"\",\"x\"\r\n"

            writer.writeNullableToFile(listOf(listOf<String?>(null, "y")), path)
            Files.readString(tmp) shouldBe ",\"y\"\r\n"
        } finally {
            tmp.deleteIfExists()
        }
    }
}
