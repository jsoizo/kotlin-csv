package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

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
            charset = Charsets.ISO_8859_1.name()
            delimiter = '\t'
            nullCode = "NULL"
            lineTerminator = "\n"
            outputLastLineTerminator = false
            quote {
                char = '\''
                mode = WriteQuoteMode.ALL
            }
        }
        assertSoftly {
            writer.charset shouldBe Charsets.ISO_8859_1.name()
            writer.delimiter shouldBe '\t'
            writer.nullCode shouldBe "NULL"
            writer.lineTerminator shouldBe "\n"
            writer.outputLastLineTerminator shouldBe false
            writer.quote.char shouldBe '\''
            writer.quote.mode = WriteQuoteMode.ALL
        }
    }
})
