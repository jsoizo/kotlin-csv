package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException

internal data class ParsedCsvField(
    val value: String,
    val quoted: Boolean
)

/**
 * Pull-based CSV row parser: reads chars from [cursor] on demand and returns
 * one logical row per [nextRow] call. Replaces the previous push-style state
 * machine — two-char sequences (`\r\n`, doubled quotes, escape pairs) are
 * consumed inline, so no lookahead/skip protocol leaks to the drivers.
 *
 * Exception coordinates follow the previous implementation: `rowNum` is the
 * 1-based logical row (quoted line breaks don't increment it), `colIndex` is
 * the 0-based position of the offending char within the row.
 */
internal class RowParser(
    dialect: CsvDialect,
    private val cursor: CharCursor,
) {
    private val quoteChar = dialect.quoteChar
    private val escapeChar = dialect.escapeChar
    private val quoteCode = dialect.quoteChar.code
    private val delimiterCode = dialect.delimiter.code
    private val escapeCode = dialect.escapeChar.code

    private var rowNum = 1L
    private var col = 0L

    private val fields = ArrayList<ParsedCsvField>()
    private val sb = StringBuilder()

    /** Parse and return the next row, or null at EOF. */
    fun nextRow(): List<ParsedCsvField>? {
        if (cursor.cur == CharCursor.EOF) return null
        fields.clear()
        col = 0L
        while (parseField()) {
            // delimiter consumed; next field follows
        }
        rowNum++
        return fields.toList()
    }

    private fun next(): Int {
        col++
        return cursor.advance()
    }

    /** Parse one field; returns true if the row continues after a delimiter. */
    private fun parseField(): Boolean =
        if (cursor.cur == quoteCode) {
            // When escapeChar == quoteChar, quote wins at field start (RFC 4180).
            next()
            parseQuotedField()
        } else {
            parseUnquotedField()
        }

    private fun parseUnquotedField(): Boolean {
        while (true) {
            val c = cursor.cur
            when {
                c == CharCursor.EOF -> {
                    flushField(quoted = false)
                    return false
                }
                // When escapeChar == quoteChar this arm also enforces RFC 4180
                // strict doubling for a quoteChar in the middle of a field.
                c == escapeCode -> appendUnquotedEscape()
                c == delimiterCode -> {
                    next()
                    flushField(quoted = false)
                    return true
                }
                isLineTerminator(c) -> {
                    consumeLineTerminator(c)
                    flushField(quoted = false)
                    return false
                }
                else -> {
                    sb.append(c.toChar())
                    next()
                }
            }
        }
    }

    private fun parseQuotedField(): Boolean {
        while (true) {
            val c = cursor.cur
            when {
                c == CharCursor.EOF -> throw CsvParseFormatException(
                    rowNum,
                    col,
                    quoteChar,
                    "end of quote doesn't exist"
                )
                c == escapeCode && escapeCode != quoteCode -> appendQuotedEscape()
                c == quoteCode -> {
                    if (next() == quoteCode) {
                        sb.append(quoteChar)
                        next()
                    } else {
                        return parseAfterQuoteEnd()
                    }
                }
                else -> {
                    sb.append(c.toChar())
                    next()
                }
            }
        }
    }

    private fun parseAfterQuoteEnd(): Boolean {
        val c = cursor.cur
        return when {
            c == CharCursor.EOF -> {
                flushField(quoted = true)
                false
            }
            c == delimiterCode -> {
                next()
                flushField(quoted = true)
                true
            }
            isLineTerminator(c) -> {
                consumeLineTerminator(c)
                flushField(quoted = true)
                false
            }
            else -> throw CsvParseFormatException(
                rowNum,
                col,
                c.toChar(),
                "must appear delimiter or line terminator after quote end"
            )
        }
    }

    /**
     * Consume an escapeChar and the char it escapes at an unquoted position.
     * Accepts escapeChar and quoteChar only (issue #168); when
     * `escapeChar == quoteChar` the accepted set degenerates to a single
     * value, preserving RFC 4180 strict doubling.
     */
    private fun appendUnquotedEscape() {
        val escCol = col
        val n = next()
        if (n != escapeCode && n != quoteCode) {
            throw CsvParseFormatException(
                rowNum,
                escCol,
                escapeChar,
                "escape character must be followed by escapeChar($escapeChar) or quoteChar($quoteChar)"
            )
        }
        sb.append(n.toChar())
        next()
    }

    /** Same as [appendUnquotedEscape] but inside quotes (escapeChar != quoteChar). */
    private fun appendQuotedEscape() {
        val escCol = col
        val n = next()
        if (n == CharCursor.EOF) {
            throw CsvParseFormatException(rowNum, escCol, escapeChar, "end of quote doesn't exist")
        }
        if (n != escapeCode && n != quoteCode) {
            throw CsvParseFormatException(
                rowNum,
                escCol,
                escapeChar,
                "escape character must appear consecutively twice"
            )
        }
        sb.append(n.toChar())
        next()
    }

    private fun isLineTerminator(c: Int): Boolean =
        c == '\n'.code || c == '\r'.code || c == '\u2028'.code || c == '\u2029'.code || c == '\u0085'.code

    private fun consumeLineTerminator(c: Int) {
        next()
        if (c == '\r'.code && cursor.cur == '\n'.code) next()
    }

    private fun flushField(quoted: Boolean) {
        fields.add(ParsedCsvField(sb.toString(), quoted))
        sb.clear()
    }
}
