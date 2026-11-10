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
 * @property nullFieldIndicator Which empty fields nullable reader APIs expose
 *   as `null`. Defaults to [CsvNullFieldIndicator.NEITHER].
 */
data class CsvReaderConfig(
    val dialect: CsvDialect = CsvDialect.RFC4180,
    val skipEmptyLine: Boolean = false,
    val insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR,
    val excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR,
    val nullFieldIndicator: CsvNullFieldIndicator = CsvNullFieldIndicator.NEITHER,
) {
    @Deprecated("Binary compatibility with 2.0.0", level = DeprecationLevel.HIDDEN)
    constructor(
        dialect: CsvDialect = CsvDialect.RFC4180,
        skipEmptyLine: Boolean = false,
        insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR,
        excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR,
    ) : this(dialect, skipEmptyLine, insufficientFieldsRowBehaviour, excessFieldsRowBehaviour, CsvNullFieldIndicator.NEITHER)

    @Deprecated("Binary compatibility with 2.0.0", level = DeprecationLevel.HIDDEN)
    fun copy(
        dialect: CsvDialect = this.dialect,
        skipEmptyLine: Boolean = this.skipEmptyLine,
        insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = this.insufficientFieldsRowBehaviour,
        excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = this.excessFieldsRowBehaviour,
    ): CsvReaderConfig = copy(dialect, skipEmptyLine, insufficientFieldsRowBehaviour, excessFieldsRowBehaviour, nullFieldIndicator)
}
