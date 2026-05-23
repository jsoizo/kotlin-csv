package com.jsoizo.kotlincsv

/**
 * Immutable CSV format specification shared by reader and writer.
 *
 * When `escapeChar == quoteChar` (the default) the writer uses RFC 4180 §2.7
 * doubling style. When `escapeChar != quoteChar` it uses explicit escape
 * style — a CSV extension where literal `quoteChar` and `escapeChar` are
 * each prefixed with `escapeChar`.
 *
 * @property delimiter Field separator (default `,`). Reader line terminator
 *   characters are not allowed.
 * @property quoteChar Field-enclosing character (default `"`). Reader line
 *   terminator characters are not allowed.
 * @property escapeChar Character used to escape [quoteChar] inside a quoted
 *   field (default `"`). Reader line terminator characters are not allowed.
 * @property lineTerminator Row separator written by the writer (default
 *   `"\r\n"`). Ignored by the reader, which auto-detects line terminators.
 * @throws IllegalArgumentException if [delimiter] equals [quoteChar] or
 *   [escapeChar], if [delimiter], [quoteChar], or [escapeChar] is a reader
 *   line terminator character, or if [lineTerminator] is empty.
 */
data class CsvDialect(
    val delimiter: Char = ',',
    val quoteChar: Char = '"',
    val escapeChar: Char = '"',
    val lineTerminator: String = "\r\n",
) {
    init {
        require(delimiter != quoteChar) {
            "delimiter and quoteChar must be different (got '$delimiter')"
        }
        require(delimiter != escapeChar) {
            "delimiter and escapeChar must be different (got '$delimiter')"
        }
        require(!delimiter.isReaderLineTerminator()) {
            "delimiter must not be a reader line terminator character (got ${delimiter.displayName()})"
        }
        require(!quoteChar.isReaderLineTerminator()) {
            "quoteChar must not be a reader line terminator character (got ${quoteChar.displayName()})"
        }
        require(!escapeChar.isReaderLineTerminator()) {
            "escapeChar must not be a reader line terminator character (got ${escapeChar.displayName()})"
        }
        require(lineTerminator.isNotEmpty()) {
            "lineTerminator must not be empty"
        }
    }

    companion object {
        /** RFC 4180: `,` delimiter, `"` quote/escape, `\r\n` terminator. */
        val RFC4180: CsvDialect = CsvDialect()

        /** TSV: `\t` delimiter, `"` quote/escape, `\n` terminator. */
        val TSV: CsvDialect = CsvDialect(
            delimiter = '\t',
            lineTerminator = "\n",
        )
    }
}

private fun Char.isReaderLineTerminator(): Boolean =
    this == '\n' || this == '\r' || this == '\u2028' || this == '\u2029' || this == '\u0085'

private fun Char.displayName(): String =
    when (this) {
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\u2028' -> "\\u2028"
        '\u2029' -> "\\u2029"
        '\u0085' -> "\\u0085"
        else -> "'$this'"
    }
