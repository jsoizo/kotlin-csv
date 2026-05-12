package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect

/**
 * Immutable configuration for [CsvReader].
 *
 * @property dialect CSV format spec. Defaults to [CsvDialect.RFC4180].
 * @property skipEmptyLine Drop rows that are empty or a single blank field.
 *   Defaults to `false`.
 * @property insufficientFieldsRowBehaviour Strategy for rows with fewer
 *   fields than the first row. Defaults to [InsufficientFieldsRowBehaviour.ERROR].
 * @property excessFieldsRowBehaviour Strategy for rows with more fields than
 *   the first row. Defaults to [ExcessFieldsRowBehaviour.ERROR].
 */
data class CsvReaderConfig(
    val dialect: CsvDialect = CsvDialect.RFC4180,
    val skipEmptyLine: Boolean = false,
    val insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR,
    val excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR,
)
