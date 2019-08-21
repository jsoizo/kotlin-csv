package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.Const
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import io.kotlintest.shouldThrow
import java.io.File

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
                    charset = Charsets.ISO_8859_1
                    quoteChar = '\''
                    delimiter = '\t'
                    escapeChar = '"'
                }
                val reader = CsvReader(context)
                reader.charset shouldBe Charsets.ISO_8859_1
                reader.quoteChar shouldBe '\''
                reader.delimiter shouldBe '\t'
                reader.escapeChar shouldBe '"'
            }
        }

        "read method (with String argument)" should {
            "read simple csv" {
                val result = csvReader().read(
                        """a,b,c
                        |d,e,f
                    """.trimMargin()
                )
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read csv with line separater" {
                val result = csvReader().read(
                        """a,b,c,"x","y
                            | hoge"
                        |d,e,f,g,h
                    """.trimMargin()
                )
                result shouldBe listOf(listOf("a", "b", "c", "x", """y
                    | hoge""".trimMargin()), listOf("d", "e", "f", "g", "h"))
            }
        }

        "read method (with InputStream argument)" should {
            "read simple csv" {
                val file = readTestDataFile("simple.csv")
                val result = csvReader().read(file.inputStream())
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
        }

        "read method (with File argument)" should {
            "read simple csv" {
                val result = csvReader().read(readTestDataFile("simple.csv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read tsv file" {
                val result = csvReader {
                    delimiter = '\t'
                }.read(readTestDataFile("simple.tsv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
            }
            "read csv with empty field" {
                val result = csvReader().read(readTestDataFile("empty-fields.csv"))
                result shouldBe listOf(listOf("a", "", "b", "", "c", ""), listOf("d", "", "e", "", "f", ""))
            }
            "read csv with escaped field" {
                val result = csvReader().read(readTestDataFile("escape.csv"))
                result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "\"e", "f"))
            }
            "read csv with line breaks enclosed in double quotes" {
                val result = csvReader().read(readTestDataFile("line-breaks.csv"))
                result shouldBe listOf(listOf("a", "b\nb", "c"), listOf("\nd", "e", "f"))
            }
            "read csv with custom quoteChar and delimiter" {
                val result = csvReader {
                    delimiter = '#'
                    quoteChar = '$'
                }.read(readTestDataFile("hash-separated-dollar-quote.csv"))
                result shouldBe listOf(listOf("Foo ", "Bar ", "Baz "), listOf("a", "b", "c"))
            }
            "read csv with custom escape character" {
                val result = csvReader {
                    escapeChar = '\\'
                }.read(readTestDataFile("backslash-escape.csv"))
                result shouldBe listOf(listOf("\"a\"", "\"This is a test\""), listOf("\"b\"", "This is a \"second\" test"))
            }
            "read csv with BOM" {
                val result = csvReader {
                    escapeChar = '\\'
                }.read(readTestDataFile("bom.csv"))
                result shouldBe listOf(listOf("a", "b", "c"))
            }
            //refs https://github.com/tototoshi/scala-csv/issues/22
            "read csv with \u2028 field" {
                val result = csvReader().read(readTestDataFile("unicode2028.csv"))
                result shouldBe listOf(listOf("\u2028"))
            }
            "throw exception when reading malformed csv" {
                shouldThrow<MalformedCSVException> {
                    csvReader().read(readTestDataFile("malformed.csv"))
                }
            }
        }

        "readWithHeader method" should {
            val expected = listOf(
                    mapOf("h1" to "a", "h2" to "b", "h3" to "c"),
                    mapOf("h1" to "d", "h2" to "e", "h3" to "f")
            )

            "read simple csv file" {
                val file = readTestDataFile("with-header.csv")
                val result = csvReader().readWithHeader(file)
                result shouldBe expected
            }

            "read from String" {
                val data = """h1,h2,h3
                    |a,b,c
                    |d,e,f
                """.trimMargin()
                val result = csvReader().readWithHeader(data)
                result shouldBe expected
            }

            "read from InputStream" {
                val file = readTestDataFile("with-header.csv")
                val result = csvReader().readWithHeader(file.inputStream())
                result shouldBe expected
            }

            "read from String containing line break" {
                val data = """h1,"h
                    |2",h3
                    |a,b,c
                """.trimMargin()
                val result = csvReader().readWithHeader(data)
                val h2 = """h
                    |2""".trimMargin()
                result shouldBe listOf(mapOf("h1" to "a", h2 to "b", "h3" to "c"))
            }
        }
    }
}

private fun readTestDataFile(fileName: String): File {
    return File("src/test/resources/testdata/csv/$fileName")
}
