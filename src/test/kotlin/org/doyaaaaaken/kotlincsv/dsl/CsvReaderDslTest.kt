package org.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.StringSpec
import org.doyaaaaaken.kotlincsv.CsvReader

class CsvReaderDslTest : StringSpec({
    "csvReader method should work as dsl" {
        val reader = csvReader {
            charset = Charsets.ISO_8859_1
        }
        reader.shouldBeTypeOf<CsvReader>()
    }
})
