package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

/**
 * Test if CSV written by CSVWriter can be read by CSVReader.
 */
class CsvReadWriteCompatibilityTest : StringSpec() {

    private val testFileName = "compatibility-test.csv"

    override fun afterTest(testCase: TestCase, result: TestResult) {
        File(testFileName).delete()
        super.afterTest(testCase, result)
    }

    init {
        "CSVReader and CSVWriter are compatible" {
            val data = listOf(
                    listOf("a", "bb", "ccc"),
                    listOf("d", "ee", "fff")
            )
            csvWriter().open(testFileName) {
                writeAll(data)
            }
            val actual = csvReader().readAll(File(testFileName))
            actual shouldBe data
        }
    }
}
