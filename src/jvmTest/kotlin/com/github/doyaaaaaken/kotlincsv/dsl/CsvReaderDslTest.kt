package com.github.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.specs.StringSpec
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe

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
            enableLogging = true
        }
        reader.charset shouldBe Charsets.ISO_8859_1.name()
        reader.quoteChar shouldBe '\''
        reader.delimiter shouldBe '\t'
        reader.skipEmptyLine shouldBe true
        reader.skipMissMatchedRow shouldBe true
        reader.enableLogging shouldBe true
    }
})
