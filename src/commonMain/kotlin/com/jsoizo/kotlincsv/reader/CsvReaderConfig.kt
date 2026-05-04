package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Immutable configuration for [CsvReader].
 *
 * Combines a [CsvDialect] (the CSV format spec) with reader-side processing
 * policies that are not part of the CSV format itself.
 *
 * Parsing is **lazy** — exceptions raised by these policies are thrown at the
 * terminal operation of the returned [Sequence], not at construction.
 *
 * @property dialect CSV format specification (delimiter, quote, escape,
 *   line terminator). Defaults to [CsvDialect.RFC4180].
 * @property skipEmptyLine If `true`, rows that parse to an empty list or to a
 *   single blank field are dropped from the output. Defaults to `false`.
 * @property insufficientFieldsRowBehaviour Strategy when a row has fewer
 *   fields than the first row. Defaults to
 *   [InsufficientFieldsRowBehaviour.ERROR].
 * @property excessFieldsRowBehaviour Strategy when a row has more fields than
 *   the first row. Defaults to [ExcessFieldsRowBehaviour.ERROR].
 */
data class CsvReaderConfig(
    val dialect: CsvDialect = CsvDialect.RFC4180,
    val skipEmptyLine: Boolean = false,
    val insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR,
    val excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR,
)
