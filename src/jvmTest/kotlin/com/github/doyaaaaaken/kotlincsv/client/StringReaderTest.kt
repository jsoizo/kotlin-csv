package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import com.github.doyaaaaaken.kotlincsv.util.CSVParseFormatException
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import com.github.doyaaaaaken.kotlincsv.util.logger.LoggerNop
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class StringReaderTest : WordSpec({
    "readAll method (with String argument)" should {
        "read simple csv" {
            val result = readAll(
                """a,b,c
                        |d,e,f
                    """.trimMargin()
            )
            result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        }
        "read csv with line separator" {
            val result = readAll(
                """a,b,c,"x","y
                            | hoge"
                        |d,e,f,g,h
                    """.trimMargin()
            )
            result shouldBe listOf(
                listOf(
                    "a", "b", "c", "x", """y
                    | hoge""".trimMargin()
                ), listOf("d", "e", "f", "g", "h")
            )
        }
        "get failed rowNum and colIndex when exception happened on parsing CSV" {
            val ex1 = shouldThrow<CSVParseFormatException> {
                readAll("a,\"\"failed")
            }
            val ex2 = shouldThrow<CSVParseFormatException> {
                readAll("a,b\nc,\"\"failed")
            }
            val ex3 = shouldThrow<CSVParseFormatException> {
                readAll("a,\"b\nb\"\nc,\"\"failed")
            }

            assertSoftly {
                ex1.rowNum shouldBe 1
                ex1.colIndex shouldBe 4
                ex1.char shouldBe 'f'

                ex2.rowNum shouldBe 2
                ex2.colIndex shouldBe 4
                ex2.char shouldBe 'f'

                ex3.rowNum shouldBe 3
                ex3.colIndex shouldBe 4
                ex3.char shouldBe 'f'
            }
        }
    }

    "readAll method" should {
        "read simple csv" {
            val result = readAll(readTestDataFile("simple.csv"))
            result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
        }
        "read csv with empty field" {
            val result = readAll(readTestDataFile("empty-fields.csv"))
            result shouldBe listOf(listOf("a", "", "b", "", "c", ""), listOf("d", "", "e", "", "f", ""))
        }
        "read csv with escaped field" {
            val result = readAll(readTestDataFile("escape.csv"))
            result shouldBe listOf(listOf("a", "b", "c"), listOf("d", "\"e", "f"))
        }
        "read csv with line breaks enclosed in double quotes" {
            val result = readAll(readTestDataFile("line-breaks.csv"))
            result shouldBe listOf(listOf("a", "b\nb", "c"), listOf("\nd", "e", "f"))
        }
        //refs https://github.com/tototoshi/scala-csv/issues/22
        "read csv with \u2028 field" {
            val result = readAll(readTestDataFile("unicode2028.csv"))
            result shouldBe listOf(listOf("\u2028"))
        }
        "throw exception when reading malformed csv" {
            shouldThrow<MalformedCSVException> {
                readAll(readTestDataFile("malformed.csv"))
            }
        }
        "throw exception when reading csv with different fields num on each row" {
            val ex = shouldThrow<CSVFieldNumDifferentException> {
                readAll(readTestDataFile("different-fields-num.csv"))
            }
            assertSoftly {
                ex.fieldNum shouldBe 3
                ex.fieldNumOnFailedRow shouldBe 2
                ex.csvRowNum shouldBe 2
            }
        }
    }

    "readAllWithHeader method" should {
        val expected = listOf(
            mapOf("h1" to "a", "h2" to "b", "h3" to "c"),
            mapOf("h1" to "d", "h2" to "e", "h3" to "f")
        )

        "read simple csv file" {
            val file = readTestDataFile("with-header.csv")
            val result = readAllWithHeader(file)
            result shouldBe expected
        }

        "read from String" {
            val data = """h1,h2,h3
                    |a,b,c
                    |d,e,f
                """.trimMargin()
            val result = readAllWithHeader(data)
            result shouldBe expected
        }

        "read from String containing line break" {
            val data = """h1,"h
                    |2",h3
                    |a,b,c
                """.trimMargin()
            val result = readAllWithHeader(data)
            val h2 = """h
                    |2""".trimMargin()
            result shouldBe listOf(mapOf("h1" to "a", h2 to "b", "h3" to "c"))
        }
        "number of fields in a row has to be based on the header #82" {
            val data = "1,2,3\na,b\nx,y,z"

            val exception = shouldThrow<CSVFieldNumDifferentException> {
                readAllWithHeader(data)
            }
            exception.fieldNum shouldBe 3
        }
    }
})

private fun readTestDataFile(fileName: String): String {
    return java.io.File("src/jvmTest/resources/testdata/csv/$fileName").readText()
}

/**
 * read csv data as String, and convert into List<List<String?>>
 */
private fun readAll(data: String): List<List<String?>> {
    return CsvFileReader(CsvReaderContext(), StringReaderImpl(data), LoggerNop).readAllAsSequence().toList()
}

/**
 * read csv data with header, and convert into List<Map<String, String?>>
 */
private fun readAllWithHeader(data: String): List<Map<String, String?>> {
    return CsvFileReader(CsvReaderContext(), StringReaderImpl(data), LoggerNop).readAllWithHeaderAsSequence().toList()
}
