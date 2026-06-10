package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Receiver of the `csvReader { ... }` DSL block. Public so it can appear in
 * the lambda signature; constructor is internal so callers go through the
 * DSL entry point.
 */
class CsvReaderConfigBuilder internal constructor() {
    var dialect: CsvDialect = CsvDialect.RFC4180
    var skipEmptyLine: Boolean = false
    var insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour =
        InsufficientFieldsRowBehaviour.ERROR
    var excessFieldsRowBehaviour: ExcessFieldsRowBehaviour =
        ExcessFieldsRowBehaviour.ERROR
    var nullFieldIndicator: CsvNullFieldIndicator = CsvNullFieldIndicator.NEITHER

    internal fun build(): CsvReaderConfig = CsvReaderConfig(
        dialect = dialect,
        skipEmptyLine = skipEmptyLine,
        insufficientFieldsRowBehaviour = insufficientFieldsRowBehaviour,
        excessFieldsRowBehaviour = excessFieldsRowBehaviour,
        nullFieldIndicator = nullFieldIndicator,
    )
}
