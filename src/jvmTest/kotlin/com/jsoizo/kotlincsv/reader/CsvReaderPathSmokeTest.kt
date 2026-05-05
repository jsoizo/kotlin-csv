package com.jsoizo.kotlincsv.reader

import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test

class CsvReaderPathSmokeTest {

    @Test
    fun read_stringPath_readsRealTempFile() {
        // End-to-end smoke that exercises the String -> Path -> Source delegation
        // chain against a real on-disk file.
        val tmp = Files.createTempFile("kotlin-csv-reader-smoke", ".csv")
        try {
            Files.writeString(tmp, "a,b,c\nd,e,f")
            val reader = CsvReader()
            val rows = reader.read(tmp.toString()) { seq -> seq.toList() }
            rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        } finally {
            tmp.deleteIfExists()
        }
    }
}
