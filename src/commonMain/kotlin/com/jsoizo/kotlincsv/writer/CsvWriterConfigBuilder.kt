package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Receiver of the `csvWriter { ... }` DSL block. Public so it can appear in
 * the lambda signature; constructor is internal so callers go through the
 * DSL entry point.
 */
class CsvWriterConfigBuilder internal constructor() {
    var dialect: CsvDialect = CsvDialect.RFC4180
    var outputLastLineTerminator: Boolean = true
    var quoteMode: WriteQuoteMode = WriteQuoteMode.CANONICAL

    internal fun build(): CsvWriterConfig = CsvWriterConfig(
        dialect = dialect,
        outputLastLineTerminator = outputLastLineTerminator,
        quoteMode = quoteMode,
    )
}
