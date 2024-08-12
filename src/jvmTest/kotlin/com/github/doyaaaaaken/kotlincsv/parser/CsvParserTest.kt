package com.github.doyaaaaaken.kotlincsv.parser

import com.github.doyaaaaaken.kotlincsv.util.CSVParseFormatException
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class CsvParserTest : WordSpec({
    val parser = CsvParser('"', ',', '"', ParserNullFieldIndicator.NEITHER)
    val lineTerminators = listOf("\n", "\u2028", "\u2029", "\u0085", "\r", "\r\n")

    "CsvParser.parseRow" should {
        "parseEmptyRow" {
            parser.parseRow("") shouldBe emptyList()
        }
        "return null if line is on the way of csv row" {
            parser.parseRow("a,\"b") shouldBe null
        }
    }

    "ParseStateMachine logic" should {
        "parse delimiter at the start of row" {
            parser.parseRow(",a") shouldBe listOf("", "a")
        }
        "parse line terminator at the start of row" {
            lineTerminators.forEach { lt ->
                parser.parseRow(lt) shouldBe listOf("")
            }
        }
        "parse row with delimiter at the end" {
            parser.parseRow("a,") shouldBe listOf("a", "")
        }
        "parse line terminator after quote end" {
            lineTerminators.forEach { lt ->
                parser.parseRow("""a,"b"$lt""") shouldBe listOf("a", "b")
            }
        }
        "parse line terminator after delimiter" {
            lineTerminators.forEach { lt ->
                parser.parseRow("a,$lt") shouldBe listOf("a", "")
            }
        }
        "parse line terminator after field" {
            lineTerminators.forEach { lt ->
                parser.parseRow("a$lt") shouldBe listOf("a")
            }
        }
        "parse escape character after field" {
            parser.parseRow("a\"\"") shouldBe listOf("a\"")
        }
        "throw exception when parsing 2 rows" {
            lineTerminators.forEach { lt ->
                shouldThrow<CSVParseFormatException> {
                    parser.parseRow("a${lt}b")
                }
            }
        }
        "thrown exception message contains correct rowNum and colIndex" {
            val ex1 = shouldThrow<CSVParseFormatException> {
                parser.parseRow("a,\"\"failed")
            }
            val ex2 = shouldThrow<CSVParseFormatException> {
                parser.parseRow("a,\"\"failed", 2)
            }

            assertSoftly {
                ex1.rowNum shouldBe 1
                ex1.colIndex shouldBe 4
                ex1.char shouldBe 'f'

                ex2.rowNum shouldBe 2
                ex2.colIndex shouldBe 4
                ex2.char shouldBe 'f'
            }
        }
    }
})
