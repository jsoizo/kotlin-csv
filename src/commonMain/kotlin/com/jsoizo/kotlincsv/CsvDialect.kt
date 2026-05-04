package com.jsoizo.kotlincsv

/**
 * Immutable value object describing a CSV format (the "dialect").
 *
 * Captures the four characters/strings that define the CSV format itself
 * and are shared between reader and writer: [delimiter], [quoteChar],
 * [escapeChar], and [lineTerminator].
 *
 * Construction is validated via `require` and throws
 * [IllegalArgumentException] on inconsistent combinations.
 *
 * @property delimiter Field separator character (default `,`).
 *   Must differ from both [quoteChar] and [escapeChar].
 * @property quoteChar Character used to enclose fields that contain
 *   the delimiter, line terminator, or the quote character itself
 *   (default `"`).
 * @property escapeChar Character used to escape [quoteChar] inside a
 *   quoted field (default `"`). The writer's output rule depends on
 *   whether this equals [quoteChar]:
 *   - `escapeChar == quoteChar` (default): doubling style as defined
 *     by RFC 4180 §2.7. A literal quote becomes two quote characters
 *     (e.g. `a"b` -> `"a""b"`).
 *   - `escapeChar != quoteChar`: explicit escape style (a CSV
 *     extension). A literal quote becomes `<escapeChar><quoteChar>`,
 *     and a literal `escapeChar` becomes `<escapeChar><escapeChar>`
 *     (e.g. with `escapeChar = '\'`, `a"b\c` -> `"a\"b\\c"`).
 * @property lineTerminator Row separator written between records
 *   (default `"\r\n"`, RFC 4180). This field is consulted only by the
 *   **writer**. The reader auto-detects line terminators (LF / CRLF /
 *   U+2028 / U+2029 / U+0085) regardless of this value.
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
        /**
         * RFC 4180 dialect: `,` delimiter, `"` quote and escape, `\r\n`
         * line terminator. Equivalent to the no-arg [CsvDialect] default.
         */
        val RFC4180: CsvDialect = CsvDialect()

        /**
         * Tab-separated values dialect: `\t` delimiter, `"` quote and
         * escape, `\n` line terminator.
         */
        val TSV: CsvDialect = CsvDialect(
            delimiter = '\t',
            lineTerminator = "\n",
        )
    }
}
