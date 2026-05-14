package com.jsoizo.kotlincsv

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CsvReaderDslTest : StringSpec({
    "dslBlock_endToEnd_parsesCsv" {
        val reader = csvReader { skipEmptyLine = true }
        reader.readAll("a,b\n\nc,d") shouldBe listOf(listOf("a", "b"), listOf("c", "d"))
    }
})
