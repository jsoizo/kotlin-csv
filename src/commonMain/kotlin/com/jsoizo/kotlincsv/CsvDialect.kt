package com.jsoizo.kotlincsv

/**
 * Immutable CSV format specification shared by reader and writer.
 *
 * When `escapeChar == quoteChar` (the default) the writer uses RFC 4180 §2.7
 * doubling style. When `escapeChar != quoteChar` it uses explicit escape
 * style — a CSV extension where literal `quoteChar` and `escapeChar` are
 * each prefixed with `escapeChar`.
 *
 * @property delimiter Field separator (default `,`).
 * @property quoteChar Field-enclosing character (default `"`).
 * @property escapeChar Character used to escape [quoteChar] inside a quoted
 *   field (default `"`).
 * @property lineTerminator Row separator written by the writer (default
 *   `"\r\n"`). Ignored by the reader, which auto-detects line terminators.
 * @throws IllegalArgumentException if [delimiter] equals [quoteChar] or
 *   [escapeChar], or if [lineTerminator] is empty.
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
