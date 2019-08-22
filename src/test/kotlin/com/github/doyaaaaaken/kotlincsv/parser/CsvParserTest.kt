package com.github.doyaaaaaken.kotlincsv.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CsvParserTest : StringSpec() {
    init {
        "parseEmptyRow" {
            val parser = CsvParser('"', ',', '"')
            parser.parseRow("") shouldBe emptyList()
        }
    }
}
