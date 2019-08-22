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
    }
}
