package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import java.io.File
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
                val expected = "String,C,1,2,3.45,true,null\r\n"
                csvWriter().writeTo(testFileName) {
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
                csvWriter().writeTo(testFileName) {
                    writeRow(row)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
        }
    }
}
