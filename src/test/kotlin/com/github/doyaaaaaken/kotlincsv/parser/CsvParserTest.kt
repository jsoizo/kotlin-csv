package com.github.doyaaaaaken.kotlincsv.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class CsvParserTest : WordSpec() {
    init {
        val parser = CsvParser('"', ',', '"')

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
                parser.parseRow("\n") shouldBe listOf("")
                parser.parseRow("\u2028") shouldBe listOf("")
                parser.parseRow("\u2029") shouldBe listOf("")
                parser.parseRow("\u0085") shouldBe listOf("")
                parser.parseRow("\r") shouldBe listOf("")
                parser.parseRow("\r\n") shouldBe listOf("")
            }
            "parse row with delimiter at the end" {
                parser.parseRow("a,") shouldBe listOf("a", "")
            }
            "parse \\r\\n after quote end" {
                parser.parseRow("""a,"b"${"\r"}""") shouldBe listOf("a", "b")
                parser.parseRow("""a,"b"${"\r\n"}""") shouldBe listOf("a", "b")
            }
            "parse line terminator after delimiter" {
                parser.parseRow("a,\n") shouldBe listOf("a", "")
                parser.parseRow("a,\u2028") shouldBe listOf("a", "")
                parser.parseRow("a,\u2029") shouldBe listOf("a", "")
                parser.parseRow("a,\u0085") shouldBe listOf("a", "")
                parser.parseRow("a,\r") shouldBe listOf("a", "")
                parser.parseRow("a,\r\n") shouldBe listOf("a", "")
            }
            "parse line terminator after field" {
                parser.parseRow("a\n") shouldBe listOf("a")
                parser.parseRow("a\u2028") shouldBe listOf("a")
                parser.parseRow("a\u2029") shouldBe listOf("a")
                parser.parseRow("a\u0085") shouldBe listOf("a")
                parser.parseRow("a\r") shouldBe listOf("a")
                parser.parseRow("a\r\n") shouldBe listOf("a")
            }
        }
    }
}
