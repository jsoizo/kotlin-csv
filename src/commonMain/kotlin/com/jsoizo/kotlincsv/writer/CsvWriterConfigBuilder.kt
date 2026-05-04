package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Mutable builder used as the receiver of the `csvWriter { ... }` DSL block.
 *
 * The class is `public` because Kotlin requires types in the signature of a
 * public function (the DSL lambda receiver) to be public. The constructor is
 * `internal` so the builder can only be instantiated through the DSL entry
 * point — direct construction is not a supported use case in v2.
 *
 * To build a [CsvWriterConfig] without the DSL, use the [CsvWriterConfig]
 * constructor with named arguments or [CsvWriterConfig.copy].
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
