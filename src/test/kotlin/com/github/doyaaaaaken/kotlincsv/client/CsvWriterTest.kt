package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.github.doyaaaaaken.kotlincsv.util.Const
import java.io.File

class CsvWriterTest : WordSpec() {

    init {
        "CsvWriter class constructor" should {
            "be created with no argument" {
                val writer = CsvWriter()
                writer.charset shouldBe Const.defaultCharset
            }
            "be created with CsvWriterContext argument" {
                val context = CsvWriterContext().apply {
                    charset = Charsets.ISO_8859_1
                    delimiter = '\t'
                    quoteChar = '\''
                    lineTerminator = "\n"
                }
                val writer = CsvWriter(context)
                writer.charset shouldBe Charsets.ISO_8859_1
                writer.delimiter shouldBe '\t'
                writer.quoteChar shouldBe '\''
                writer.lineTerminator shouldBe "\n"
            }
        }

        "writeTo method" should {
            //TODO: Delete file after test
            "write simple csv data into file" {
                val row = listOf("a", "b", null)
                csvWriter().writeTo(File("test.csv")) {
                    writeRow(row)
                    writeRow(row)
                    flush()
                }
            }
        }
    }
}
