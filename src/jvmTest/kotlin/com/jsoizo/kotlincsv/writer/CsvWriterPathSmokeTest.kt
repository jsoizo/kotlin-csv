package com.jsoizo.kotlincsv.writer

import io.kotest.matchers.shouldBe
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
}
