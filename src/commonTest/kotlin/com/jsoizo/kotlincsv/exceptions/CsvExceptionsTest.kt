package com.jsoizo.kotlincsv.exceptions

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class CsvExceptionsTest {

    @Test
    fun malformedCsvException_holdsMessage() {
        val ex = MalformedCsvException("boom")
        ex.message shouldBe "boom"
        ex.shouldBeInstanceOf<RuntimeException>()
    }

    @Test
    fun csvParseFormatException_extendsMalformedAndFormatsMessage() {
        val ex = CsvParseFormatException(rowNum = 3L, colIndex = 7L, char = '"')
        ex.shouldBeInstanceOf<MalformedCsvException>()
        ex.rowNum shouldBe 3L
        ex.colIndex shouldBe 7L
        ex.char shouldBe '"'
        ex.message shouldBe
            "Exception happened on parsing csv [rowNum = 3, colIndex = 7, char = \"]"
    }

    @Test
    fun csvParseFormatException_acceptsCustomMessage() {
        val ex = CsvParseFormatException(
            rowNum = 1L,
            colIndex = 2L,
            char = 'x',
            message = "custom"
        )
        ex.message shouldBe "custom [rowNum = 1, colIndex = 2, char = x]"
    }

    @Test
    fun csvFieldNumDifferentException_holdsFieldsAndFormatsMessage() {
        val ex = CsvFieldNumDifferentException(
            expectedFieldCount = 3,
            actualFieldCount = 2,
            rowNum = 5L
        )
        ex.shouldBeInstanceOf<MalformedCsvException>()
        ex.expectedFieldCount shouldBe 3
        ex.actualFieldCount shouldBe 2
        ex.rowNum shouldBe 5L
        val message = ex.message.shouldNotBeNull()
        message shouldContain "Fields num seems to be 3"
        message shouldContain "5th csv row"
        message shouldContain "fields num is 2"
    }
}
