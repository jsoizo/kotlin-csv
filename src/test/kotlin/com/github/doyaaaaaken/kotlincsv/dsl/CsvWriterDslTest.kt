package com.github.doyaaaaaken.kotlincsv.dsl

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter

/**
 * @author doyaaaaaken
 */
class CsvWriterDslTest : StringSpec({
    "csvWriter method should work as global method with no argument" {
        val writer = csvWriter()
        writer.shouldBeTypeOf<CsvWriter>()
    }
    "csvWriter method should work as dsl" {
        val writer = csvWriter {
            charset = Charsets.ISO_8859_1
            delimiter = '\t'
//            quoteChar = '\''
            lineTerminator = "\n"
        }
        writer.charset shouldBe Charsets.ISO_8859_1
        writer.delimiter shouldBe '\t'
//        writer.quoteChar shouldBe '\''
        writer.lineTerminator shouldBe "\n"
    }
})
