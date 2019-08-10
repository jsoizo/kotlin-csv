package org.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import org.doyaaaaaken.kotlincsv.util.Const

class CsvReaderTest : StringSpec({
    "CsvReader should be created by class constructor with no argument" {
        val reader = CsvReader()
        reader.charset shouldBe Const.defaultCharset
    }
    "CsvReader should be created by class constructor with CsvReaderContext argument" {
        val context = CsvReaderContext().apply {
            charset = Charsets.ISO_8859_1
            delimiter = '\t'
            withHeader = false
        }
        val reader = CsvReader(context)
        reader.charset shouldBe Charsets.ISO_8859_1
        reader.delimiter shouldBe '\t'
        reader.withHeader shouldBe false
    }
})
