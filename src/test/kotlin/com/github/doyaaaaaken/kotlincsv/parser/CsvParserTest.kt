package com.github.doyaaaaaken.kotlincsv.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CsvParserTest : StringSpec() {
    init {
        val parser = CsvParser('"', ',', '"')
        "parseEmptyRow" {
            parser.parseRow("") shouldBe emptyList()
        }
        "return null if line is on the way of csv row" {
            parser.parseRow("a,\"b") shouldBe null
        }
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
    }
}
