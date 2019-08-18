package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.github.doyaaaaaken.kotlincsv.util.Const
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import java.io.File

class CsvWriterTest : WordSpec() {

    private val testFileName = "test.csv"

    override fun afterTest(testCase: TestCase, result: TestResult) {
        File(testFileName).delete()
        super.afterTest(testCase, result)
    }

    private fun readTestFile(): String {
        return File(testFileName).readText()
    }

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
            "write simple csv data into file with writing each rows" {
                val row1 = listOf("a", "b", null)
                val row2 = listOf("d", "2", "1.0")
                csvWriter().writeTo(testFileName) {
                    writeRow(row1)
                    writeRow(row2)
                }

                val expected = "a,b,null\r\nd,2,1.0\r\n"
                val actual = readTestFile()
                actual shouldBe expected
            }

            "write simple csv data into file with writing all at one time" {
                val row1 = listOf("a", "b", null)
                val row2 = listOf("d", "2", "1.0")
                csvWriter().writeTo(testFileName).writeAll(listOf(row1, row2))

                val expected = "a,b,null\r\nd,2,1.0\r\n"
                val actual = readTestFile()
                actual shouldBe expected
            }

            "write simple csv data to existing file with appending on tail" {
                val row1 = listOf("a", "b", null)
                val row2 = listOf("d", "2", "1.0")
                csvWriter().writeTo(File(testFileName), true) {
                    writeAll(listOf(row1, row2))
                    writeAll(listOf(row1, row2))
                }

                val expected = "a,b,null\r\nd,2,1.0\r\n"
                val actual = readTestFile()
                actual shouldBe expected + expected
            }

            //TODO: charaset test
        }
    }
}
