package org.doyaaaaaken.kotlincsv

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

class CsvReaderTest : StringSpec({
    "CsvReader should be created by class constructor with no argument" {
        val reader = CsvReader()
        //check if default charset is set
        reader.charset shouldBe Charsets.UTF_8
    }
    "CsvReader should be created by class constructor with CsvReaderContext argument" {
        val context = CsvReaderContext().apply {
            charset = Charsets.ISO_8859_1
        }
        val reader = CsvReader(context)
        reader.charset shouldBe Charsets.ISO_8859_1
    }
})
