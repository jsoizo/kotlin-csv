package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Mutable builder used as the receiver of the `csvReader { ... }` DSL block.
 *
 * The class is `public` because Kotlin requires types in the signature of a
 * public function (the DSL lambda receiver) to be public. The constructor is
 * `internal` so the builder can only be instantiated through the DSL entry
 * point — direct construction is not a supported use case in v2.
 *
 * To build a [CsvReaderConfig] without the DSL, use the [CsvReaderConfig]
 * constructor with named arguments or [CsvReaderConfig.copy].
 */
class CsvReaderConfigBuilder internal constructor() {
    var dialect: CsvDialect = CsvDialect.RFC4180
    var skipEmptyLine: Boolean = false
    var insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour =
        InsufficientFieldsRowBehaviour.ERROR
    var excessFieldsRowBehaviour: ExcessFieldsRowBehaviour =
        ExcessFieldsRowBehaviour.ERROR

    internal fun build(): CsvReaderConfig = CsvReaderConfig(
        dialect = dialect,
        skipEmptyLine = skipEmptyLine,
        insufficientFieldsRowBehaviour = insufficientFieldsRowBehaviour,
        excessFieldsRowBehaviour = excessFieldsRowBehaviour,
    )
}
