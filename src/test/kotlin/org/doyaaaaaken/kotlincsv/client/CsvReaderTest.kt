package org.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import org.doyaaaaaken.kotlincsv.util.Const

class CsvReaderTest : WordSpec({

    "CsvReader class constructor" should {
        "be created with no argument" {
            val reader = CsvReader()
            reader.charset shouldBe Const.defaultCharset
        }
        "be created with CsvReaderContext argument" {
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
    }

    "read method (with String argument)" should {
        //TODO: implement about various type of csv files
        "read csv" {
            val result = CsvReader().read(
                    """a,b,c
                        |d,e,f
                    """.trimMargin()
            )
            result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        }
    }
})
