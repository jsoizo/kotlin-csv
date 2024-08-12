package com.jsoizo.kotlincsv.client

import com.jsoizo.kotlincsv.dsl.csvWriter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime

class CsvFileWriterTest : WordSpec({
    val testFileName = "test.csv"

    afterTest { File(testFileName).delete() }

    fun readTestFile(charset: Charset = Charsets.UTF_8): String {
        return File(testFileName).readText(charset)
    }

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
        "write row from variable arguments" {

            val date1 = LocalDate.of(2019, 8, 19)
            val date2 = LocalDateTime.of(2020, 9, 20, 14, 32, 21)

            val expected = "a,b,c\r\n" +
                    "d,e,f\r\n" +
                    "1,2,3\r\n" +
                    "2019-08-19,2020-09-20T14:32:21\r\n"
            csvWriter().open(testFileName) {
                writeRow("a", "b", "c")
                writeRow("d", "e", "f")
                writeRow(1, 2, 3)
                writeRow(date1, date2)
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
        "write no line terminator when row is empty for rows from list" {
            val rows = listOf(listOf("a", "b", "c"), listOf(), listOf("d", "e", "f"))
            val expected = "a,b,c\r\nd,e,f\r\n"
            csvWriter().open(testFileName) {
                writeRows(rows)
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "write no line terminator when row is empty for rows from sequence" {
            val rows = listOf(listOf("a", "b", "c"), listOf(), listOf("d", "e", "f")).asSequence()
            val expected = "a,b,c\r\nd,e,f\r\n"
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
    "suspend writeRow method" should {
        "suspend write any primitive types" {
            val row = listOf("String", 'C', 1, 2L, 3.45, true, null)
            val expected = "String,C,1,2,3.45,true,\r\n"
            csvWriter().openAsync(testFileName) {
                writeRow(row)
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "suspend write row from variable arguments" {

            val date1 = LocalDate.of(2019, 8, 19)
            val date2 = LocalDateTime.of(2020, 9, 20, 14, 32, 21)

            val expected = "a,b,c\r\n" +
                    "d,e,f\r\n" +
                    "1,2,3\r\n" +
                    "2019-08-19,2020-09-20T14:32:21\r\n"
            csvWriter().openAsync(testFileName) {
                writeRow("a", "b", "c")
                writeRow("d", "e", "f")
                writeRow(1, 2, 3)
                writeRow(date1, date2)
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
        "suspend write all Sequence data" {
            val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f")).asSequence()
            val expected = "a,b,c\r\nd,e,f\r\n"
            csvWriter().openAsync(testFileName) {
                writeRows(rows)
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
    }
    "suspend close method" should {
        "throw Exception when stream is already closed" {
            val row = listOf("a", "b")
            shouldThrow<IOException> {
                csvWriter().openAsync(testFileName) {
                    close()
                    writeRow(row)
                }
            }
        }
    }
    "suspend flush method" should {
        "flush stream" {
            val row = listOf("a", "b")
            csvWriter().openAsync(testFileName) {
                writeRow(row)
                flush()
                val actual = readTestFile()
                actual shouldBe "a,b\r\n"
            }
        }
    }
    "validate suspend test as flow" should {
        "execute line" {
            val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f")).asSequence()
            val expected = "a,b,c\r\nd,e,f\r\n"
            csvWriter().openAsync(testFileName) {
                delay(100)
                rows.forEach {
                    delay(100)
                    writeRow(it)
                }
            }
            val actual = readTestFile()
            actual shouldBe expected
        }
    }

})
