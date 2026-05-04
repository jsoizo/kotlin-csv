package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Immutable configuration for [CsvWriter].
 *
 * Combines a [CsvDialect] (the CSV format spec) with writer-side encoding
 * policies that are not part of the CSV format itself.
 *
 * @property dialect CSV format specification (delimiter, quote, escape,
 *   line terminator). Defaults to [CsvDialect.RFC4180].
 * @property outputLastLineTerminator If `true`, a trailing line terminator is
 *   emitted after the final row. Defaults to `true`, matching Excel and Google
 *   Sheets which both write a trailing terminator. RFC 4180 §2 allows either.
 * @property quoteMode Strategy for deciding when a field is wrapped in
 *   `quoteChar`s. Defaults to [WriteQuoteMode.CANONICAL].
 */
data class CsvWriterConfig(
    val dialect: CsvDialect = CsvDialect.RFC4180,
    val outputLastLineTerminator: Boolean = true,
    val quoteMode: WriteQuoteMode = WriteQuoteMode.CANONICAL,
)
