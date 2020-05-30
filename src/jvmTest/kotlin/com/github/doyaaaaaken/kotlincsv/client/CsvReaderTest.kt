package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import com.github.doyaaaaaken.kotlincsv.util.CSVParseFormatException
import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import java.io.File
import kotlin.test.assertEquals

/**
 * @author doyaaaaaken
 */
class CsvReaderTest : WordSpec() {

    init {
        "CsvReader class constructor" should {
            "be created with no argument" {
                val reader = CsvReader()
                reader.charset shouldBe Const.defaultCharset
            }
            "be created with CsvReaderContext argument" {
                val context = CsvReaderContext().apply {
                    charset = Charsets.ISO_8859_1.name()
                    quoteChar = '\''
                    delimiter = '\t'
                    escapeChar = '"'
                    skipEmptyLine = true
                }
                val reader = CsvReader(context)
                reader.charset shouldBe Charsets.ISO_8859_1.name()
                reader.quoteChar shouldBe '\''
                reader.delimiter shouldBe '\t'
                reader.escapeChar shouldBe '"'
                reader.skipEmptyLine shouldBe true
            }
        }

        "readAll method (with String argument)" should {
            "read simple csv" {
                val result = csvReader().readAll(
                        """a,b,c
                        |d,e,f
                    """.trimMargin()
                )
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read csv with line separater" {
                val result = csvReader().readAll(
                        """a,b,c,"x","y
                            | hoge"
                        |d,e,f,g,h
                    """.trimMargin()
                )
                result shouldBe listOf(listOf("a", "b", "c", "x", """y
                    | hoge""".trimMargin()), listOf("d", "e", "f", "g", "h"))
            }
            "get failed rowNum and colIndex when exception happened on parsing CSV" {
                val reader = csvReader()
                val ex1 = shouldThrow<CSVParseFormatException> {
                    reader.readAll("a,\"\"failed")
                }
                assertEquals(1, ex1.rowNum)
                assertEquals(4, ex1.colIndex)
                assertEquals('f', ex1.char)

                val ex2 = shouldThrow<CSVParseFormatException> {
                    reader.readAll("a,b\nc,\"\"failed")
                }
                assertEquals(2, ex2.rowNum)
                assertEquals(4, ex2.colIndex)
                assertEquals('f', ex2.char)

                val ex3 = shouldThrow<CSVParseFormatException> {
                    reader.readAll("a,\"b\nb\"\nc,\"\"failed")
                }
                assertEquals(3, ex3.rowNum)
                assertEquals(4, ex3.colIndex)
                assertEquals('f', ex3.char)
            }
        }

        "readAll method (with InputStream argument)" should {
            "read simple csv" {
                val file = readTestDataFile("simple.csv")
                val result = csvReader().readAll(file.inputStream())
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
        }

        "readAll method (with File argument)" should {
            "read simple csv" {
                val result = csvReader().readAll(readTestDataFile("simple.csv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read tsv file" {
                val result = csvReader {
                    delimiter = '\t'
                }.readAll(readTestDataFile("simple.tsv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read csv with empty field" {
                val result = csvReader().readAll(readTestDataFile("empty-fields.csv"))
                result shouldBe listOf(listOf("a", "", "b", "", "c", ""), listOf("d", "", "e", "", "f", ""))
            }
            "read csv with escaped field" {
                val result = csvReader().readAll(readTestDataFile("escape.csv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "\"e", "f"))
            }
            "read csv with line breaks enclosed in double quotes" {
                val result = csvReader().readAll(readTestDataFile("line-breaks.csv"))
                result shouldBe listOf(listOf("a", "b\nb", "c"), listOf("\nd", "e", "f"))
            }
            "read csv with custom quoteChar and delimiter" {
                val result = csvReader {
                    delimiter = '#'
                    quoteChar = '$'
                }.readAll(readTestDataFile("hash-separated-dollar-quote.csv"))
                result shouldBe listOf(listOf("Foo ", "Bar ", "Baz "), listOf("a", "b", "c"))
            }
            "read csv with custom escape character" {
                val result = csvReader {
                    escapeChar = '\\'
                }.readAll(readTestDataFile("backslash-escape.csv"))
                result shouldBe listOf(listOf("\"a\"", "\"This is a test\""), listOf("\"b\"", "This is a \"second\" test"))
            }
            "read csv with BOM" {
                val result = csvReader {
                    escapeChar = '\\'
                }.readAll(readTestDataFile("bom.csv"))
                result shouldBe listOf(listOf("a", "b", "c"))
            }
            //refs https://github.com/tototoshi/scala-csv/issues/22
            "read csv with \u2028 field" {
                val result = csvReader().readAll(readTestDataFile("unicode2028.csv"))
                result shouldBe listOf(listOf("\u2028"))
            }
            "read csv with empty lines" {
                val result = csvReader {
                    skipEmptyLine = true
                }.readAll(readTestDataFile("empty-line.csv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read csv with quoted empty line field" {
                val result = csvReader {
                    skipEmptyLine = true
                }.readAll(readTestDataFile("quoted-empty-line.csv"))
                result shouldBe listOf(listOf("a", "b", "c\n\nc"), listOf("d", "e", "f"))
            }
            "throw exception when reading malformed csv" {
                shouldThrow<MalformedCSVException> {
                    csvReader().readAll(readTestDataFile("malformed.csv"))
                }
            }
            "throw exception when reading csv with different fields num on each row" {
                val ex = shouldThrow<CSVFieldNumDifferentException> {
                    csvReader().readAll(readTestDataFile("different-fields-num.csv"))
                }
                ex.fieldNum shouldBe 3
                ex.fieldNumOnFailedRow shouldBe 2
                ex.csvRowNum shouldBe 2
            }
            "should not throw exception when reading csv with different fields num on each row with expected number of columns" {
                val expected = listOf(listOf("a", "b", "c"))
                val ex  = csvReader {
                    skipMissMatchedRow = true
                }.readAll(readTestDataFile("different-fields-num.csv"))

                ex shouldBe expected
                ex.size shouldBe 1

                val expected2 = listOf(listOf("a", "b"))
                val ex2  = csvReader {
                    skipMissMatchedRow = true
                }.readAll(readTestDataFile("different-fields-num2.csv"))

                ex2 shouldBe expected2
                ex2.size shouldBe 1

            }
            "should not throw exception when reading csv with header and different fields num on each row" {
                val expected = listOf(
                        mapOf("h1" to "a", "h2" to "b", "h3" to "c"),
                        mapOf("h1" to "g", "h2" to "h", "h3" to "i")
                )
                val ex  = csvReader {
                    skipMissMatchedRow = true
                }.readAllWithHeader(readTestDataFile("with-header-different-size-row.csv"))

                ex.size shouldBe  2
                expected shouldBe  ex
            }
        }

        "readAllWithHeader method" should {
            val expected = listOf(
                    mapOf("h1" to "a", "h2" to "b", "h3" to "c"),
                    mapOf("h1" to "d", "h2" to "e", "h3" to "f")
            )

            "read simple csv file" {
                val file = readTestDataFile("with-header.csv")
                val result = csvReader().readAllWithHeader(file)
                result shouldBe expected
            }

            "read from String" {
                val data = """h1,h2,h3
                    |a,b,c
                    |d,e,f
                """.trimMargin()
                val result = csvReader().readAllWithHeader(data)
                result shouldBe expected
            }

            "read from InputStream" {
                val file = readTestDataFile("with-header.csv")
                val result = csvReader().readAllWithHeader(file.inputStream())
                result shouldBe expected
            }

            "read from String containing line break" {
                val data = """h1,"h
                    |2",h3
                    |a,b,c
                """.trimMargin()
                val result = csvReader().readAllWithHeader(data)
                val h2 = """h
                    |2""".trimMargin()
                result shouldBe listOf(mapOf("h1" to "a", h2 to "b", "h3" to "c"))
            }
        }

        "open method (with fileName argument)" should {
            val rows = csvReader().open("src/jvmTest/resources/testdata/csv/simple.csv") {
                val row1 = readNext()
                val row2 = readNext()
                listOf(row1, row2)
            }
            rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        }

        "open method (with InputStream argument)" should {
            val file = readTestDataFile("simple.csv")
            val rows = csvReader().open(file.inputStream()) {
                val row1 = readNext()
                val row2 = readNext()
                listOf(row1, row2)
            }
            rows shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        }
    }
}

private fun readTestDataFile(fileName: String): File {
    return File("src/jvmTest/resources/testdata/csv/$fileName")
}
