package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldThrow
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author doyaaaaaken
 */
class CsvFileWriterTest : WordSpec() {

    private val testFileName = "test.csv"

    override fun afterTest(testCase: TestCase, result: TestResult) {
        File(testFileName).delete()
        super.afterTest(testCase, result)
    }

    private fun readTestFile(charset: Charset = Charsets.UTF_8): String {
        return File(testFileName).readText(charset)
    }

    init {
        "writeRow method" should {
            "write any primitive types" {
                val row = listOf("String", 'C', 1, 2L, 3.45, true, null)
                val expected = "String,C,1,2,3.45,true,\r\n"
                csvWriter().open(testFileName) {
                    writeRow(row)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
            "write java.time.LocalDate and java.time.LocalDateTime types" {
                val row = listOf(
                        LocalDate.of(2019, 8, 19),
                        LocalDateTime.of(2020, 9, 20, 14, 32, 21)
                )
                val expected = "2019-08-19,2020-09-20T14:32:21\r\n"
                csvWriter().open(testFileName) {
                    writeRow(row)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
        }
        "writeAll method" should {
            "write Sequence data" {
                val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f")).asSequence()
                val expected = "a,b,c\r\nd,e,f\r\n"
                csvWriter().open(testFileName) {
                    writeRows(rows)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
            "write escaped field when a field contains quoteChar in it" {
                val rows = listOf(listOf("a", "\"b", "c"), listOf("d", "e", "f\""))
                val expected = "a,\"\"\"b\",c\r\nd,e,\"f\"\"\"\r\n"
                csvWriter().open(testFileName) {
                    writeRows(rows)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
            "write escaped field when a field contains delimiter in it" {
                val rows = listOf(listOf("a", ",b", "c"), listOf("d", "e", "f,"))
                val expected = "a,\",b\",c\r\nd,e,\"f,\"\r\n"
                csvWriter().open(testFileName) {
                    writeRows(rows)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
            "write quoted field when a field contains cr or lf in it" {
                val rows = listOf(listOf("a", "\nb", "c"), listOf("d", "e", "f\r\n"))
                val expected = "a,\"\nb\",c\r\nd,e,\"f\r\n\"\r\n"
                csvWriter().open(testFileName) {
                    writeRows(rows)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
        }
        "close method" should {
            "throw Exception when stream is already closed" {
                val row = listOf("a", "b")
                shouldThrow<IOException> {
                    csvWriter().open(testFileName) {
                        close()
                        writeRow(row)
                    }
                }
            }
        }
        "flush method" should {
            "flush stream" {
                val row = listOf("a", "b")
                csvWriter().open(testFileName) {
                    writeRow(row)
                    flush()
                    val actual = readTestFile()
                    actual shouldBe "a,b\r\n"
                }
            }
        }
    }
}
