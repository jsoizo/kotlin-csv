package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

/**
 * @author doyaaaaaken
 */
class CsvReaderDslTest : StringSpec({
    "csvReader method should work as global method with no argument" {
        val reader = csvReader()
        reader.shouldBeTypeOf<CsvReader>()
    }
    "csvReader method should work as dsl" {
        val reader = csvReader {
            charset = Charsets.ISO_8859_1.name()
            quoteChar = '\''
            delimiter = '\t'
            escapeChar = '"'
            skipEmptyLine = true
            skipMissMatchedRow = true
        }
        reader.charset shouldBe Charsets.ISO_8859_1.name()
        reader.quoteChar shouldBe '\''
        reader.delimiter shouldBe '\t'
        reader.skipEmptyLine shouldBe true
        reader.skipMissMatchedRow shouldBe true
    }
})
