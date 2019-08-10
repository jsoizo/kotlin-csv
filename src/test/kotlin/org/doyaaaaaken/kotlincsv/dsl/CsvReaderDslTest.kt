package org.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.doyaaaaaken.kotlincsv.client.CsvReader
import java.lang.RuntimeException

class CsvReaderDslTest : StringSpec({
    "csvReader method should work as global method with no argument" {
        val reader = csvReader()
        reader.shouldBeTypeOf<CsvReader>()
    }
    "csvReader method should work as dsl" {
        throw RuntimeException("xxx")
        val reader = csvReader {
            charset = Charsets.ISO_8859_1
            delimiter = '\t'
            withHeader = false
        }
        reader.charset shouldBe Charsets.ISO_8859_1
        reader.delimiter shouldBe '\t'
        reader.withHeader shouldBe false
    }
})
