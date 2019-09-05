package com.github.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.specs.StringSpec
import com.github.doyaaaaaken.kotlincsv.client.CsvReader

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
            charset = Charsets.ISO_8859_1
            quoteChar = '\''
            delimiter = '\t'
            escapeChar = '"'
        }
        reader.charset shouldBe Charsets.ISO_8859_1
        reader.quoteChar shouldBe '\''
        reader.delimiter shouldBe '\t'
        reader.escapeChar shouldBe '"'
    }
})
