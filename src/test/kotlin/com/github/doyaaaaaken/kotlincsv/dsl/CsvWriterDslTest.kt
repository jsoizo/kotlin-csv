package com.github.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter

class CsvWriterDslTest : StringSpec({
    "csvWriter method should work as global method with no argument" {
        val reader = csvWriter()
        reader.shouldBeTypeOf<CsvWriter>()
    }
    "csvWriter method should work as dsl" {
        val reader = csvWriter {
            charset = Charsets.ISO_8859_1
            delimiter = '\t'
            quoteChar = '\''
            lineTerminator = "\n"
        }
        reader.charset shouldBe Charsets.ISO_8859_1
        reader.delimiter shouldBe '\t'
        reader.quoteChar shouldBe '\''
        reader.lineTerminator shouldBe "\n"
    }
})
