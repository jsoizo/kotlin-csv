package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.CsvDialect
import kotlin.jvm.JvmOverloads

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
 * @property ignoreLeadingWhitespace Ignore ASCII spaces and tabs before an opening
 *   quote at the start of a field. Unquoted contents are preserved. Defaults to `false`.
 */
@ConsistentCopyVisibility
data class CsvReaderConfig private constructor(
    val dialect: CsvDialect,
    val skipEmptyLine: Boolean,
    val insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour,
    val excessFieldsRowBehaviour: ExcessFieldsRowBehaviour,
    val nullFieldIndicator: CsvNullFieldIndicator,
    val ignoreLeadingWhitespace: Boolean,
) {
    // A single public Kotlin constructor/copy keeps untyped callable references
    // unambiguous. The generated six-field copy follows the private constructor.
    @JvmOverloads
    constructor(
        dialect: CsvDialect = CsvDialect.RFC4180,
        skipEmptyLine: Boolean = false,
        insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.ERROR,
        excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = ExcessFieldsRowBehaviour.ERROR,
        nullFieldIndicator: CsvNullFieldIndicator = CsvNullFieldIndicator.NEITHER,
    ) : this(dialect, skipEmptyLine, insufficientFieldsRowBehaviour, excessFieldsRowBehaviour, nullFieldIndicator, false)

    fun copy(
        dialect: CsvDialect = this.dialect,
        skipEmptyLine: Boolean = this.skipEmptyLine,
        insufficientFieldsRowBehaviour: InsufficientFieldsRowBehaviour = this.insufficientFieldsRowBehaviour,
        excessFieldsRowBehaviour: ExcessFieldsRowBehaviour = this.excessFieldsRowBehaviour,
        nullFieldIndicator: CsvNullFieldIndicator = this.nullFieldIndicator,
    ): CsvReaderConfig = CsvReaderConfig(
        dialect, skipEmptyLine, insufficientFieldsRowBehaviour, excessFieldsRowBehaviour,
        nullFieldIndicator, ignoreLeadingWhitespace,
    )

    /** Return a configuration with only the quoted-field whitespace option changed. */
    fun withIgnoreLeadingWhitespace(ignoreLeadingWhitespace: Boolean): CsvReaderConfig = CsvReaderConfig(
        dialect, skipEmptyLine, insufficientFieldsRowBehaviour, excessFieldsRowBehaviour,
        nullFieldIndicator, ignoreLeadingWhitespace,
    )
}
