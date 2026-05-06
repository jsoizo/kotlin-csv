package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Immutable configuration for [CsvWriter].
 *
 * @property dialect CSV format spec. Defaults to [CsvDialect.RFC4180].
 * @property outputLastLineTerminator Emit a trailing line terminator after
 *   the final row. Defaults to `true` (matches Excel / Google Sheets).
 * @property quoteMode When to wrap a field in `quoteChar`. Defaults to
 *   [WriteQuoteMode.CANONICAL].
 */
data class CsvWriterConfig(
    val dialect: CsvDialect = CsvDialect.RFC4180,
    val outputLastLineTerminator: Boolean = true,
    val quoteMode: WriteQuoteMode = WriteQuoteMode.CANONICAL,
)
