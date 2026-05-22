package com.jsoizo.kotlincsv

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class CsvDialectTest {

    @Test
    fun construct_withCustomValues_doesNotTriggerRequire() {
        CsvDialect(
            delimiter = ';',
            quoteChar = '\'',
            escapeChar = '\\',
            lineTerminator = "\n",
        )
    }

    @Test
    fun require_rejects_delimiterEqualToQuoteChar() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(delimiter = ',', quoteChar = ',')
        }
        ex.message.shouldNotBeNull() shouldContain ","
    }

    @Test
    fun require_rejects_delimiterEqualToEscapeChar() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(delimiter = ',', escapeChar = ',')
        }
        ex.message.shouldNotBeNull() shouldContain ","
    }

    @Test
    fun require_rejects_delimiterAsReaderLineTerminator() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(delimiter = '\n')
        }
        ex.message.shouldNotBeNull() shouldContain "delimiter"
    }

    @Test
    fun require_rejects_quoteCharAsReaderLineTerminator() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(quoteChar = '\r')
        }
        ex.message.shouldNotBeNull() shouldContain "quoteChar"
    }

    @Test
    fun require_rejects_escapeCharAsReaderLineTerminator() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(escapeChar = '\u2028')
        }
        ex.message.shouldNotBeNull() shouldContain "escapeChar"
    }

    @Test
    fun require_rejects_emptyLineTerminator() {
        val ex = shouldThrow<IllegalArgumentException> {
            CsvDialect(lineTerminator = "")
        }
        ex.message.shouldNotBeNull() shouldContain "lineTerminator"
    }
}
