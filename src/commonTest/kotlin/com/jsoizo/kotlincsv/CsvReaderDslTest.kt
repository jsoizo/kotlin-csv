package com.jsoizo.kotlincsv

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CsvReaderDslTest {

    @Test
    fun dslBlock_endToEnd_parsesCsv() {
        val reader = csvReader { skipEmptyLine = true }
        reader.readAll("a,b\n\nc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }
}
