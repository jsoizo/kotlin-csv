package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException

/**
 * @author doyaaaaaaken
 */
internal class ParseStateMachine(
    private val quoteChar: Char,
    private val delimiter: Char,
    private val escapeChar: Char
) {

    private var state = ParseState.START

    private val fields = ArrayList<String>()

    private var field = StringBuilder()

    private var pos = 0L

    /**
     * Read character and change state
     *
     * @return read character count (1 or 2)
     */
    fun read(ch: Char, nextCh: Char?, rowNum: Long): Long {
        val prevPos = pos
        when (state) {
            ParseState.START -> {
                when (ch) {
                    quoteChar -> state = ParseState.QUOTE_START
                    // When `escapeChar == quoteChar`, the quoteChar arm above wins; this arm is unreachable.
                    escapeChar -> state = handleUnquotedEscape(nextCh, rowNum)
                    delimiter -> {
                        flushField()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        flushField()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        flushField()
                        state = ParseState.END
                    }
                    else -> {
                        field.append(ch)
                        state = ParseState.FIELD
                    }
                }
                pos += 1
            }
            ParseState.FIELD -> {
                when (ch) {
                    escapeChar -> state = handleUnquotedEscape(nextCh, rowNum)
                    delimiter -> {
                        flushField()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        flushField()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        flushField()
                        state = ParseState.END
                    }
                    else -> {
                        field.append(ch)
                        state = ParseState.FIELD
                    }
                }
                pos += 1
            }
            ParseState.DELIMITER -> {
                when (ch) {
                    quoteChar -> state = ParseState.QUOTE_START
                    // When `escapeChar == quoteChar`, the quoteChar arm above wins; this arm is unreachable.
                    escapeChar -> state = handleUnquotedEscape(nextCh, rowNum)
                    delimiter -> {
                        flushField()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        flushField()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        flushField()
                        state = ParseState.END
                    }
                    else -> {
                        field.append(ch)
                        state = ParseState.FIELD
                    }
                }
                pos += 1
            }
            ParseState.QUOTE_START, ParseState.QUOTED_FIELD -> {
                if (ch == escapeChar && escapeChar != quoteChar) {
                    if (nextCh == null) throw CsvParseFormatException(rowNum, pos, ch, "end of quote doesn't exist")
                    if (nextCh != escapeChar && nextCh != quoteChar) throw CsvParseFormatException(
                        rowNum,
                        pos,
                        ch,
                        "escape character must appear consecutively twice"
                    )
                    field.append(nextCh)
                    state = ParseState.QUOTED_FIELD
                    pos += 1
                } else if (ch == quoteChar) {
                    if (nextCh == quoteChar) {
                        field.append(quoteChar)
                        state = ParseState.QUOTED_FIELD
                        pos += 1
                    } else {
                        state = ParseState.QUOTE_END
                    }
                } else {
                    field.append(ch)
                    state = ParseState.QUOTED_FIELD
                }
                pos += 1
            }
            ParseState.QUOTE_END -> {
                when (ch) {
                    delimiter -> {
                        flushField()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        flushField()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        flushField()
                        state = ParseState.END
                    }
                    else -> throw CsvParseFormatException(
                        rowNum,
                        pos,
                        ch,
                        "must appear delimiter or line terminator after quote end"
                    )
                }
                pos += 1
            }
            ParseState.END -> throw CsvParseFormatException(rowNum, pos, ch, "unexpected error")
        }
        return pos - prevPos
    }

    /**
     * `true` after a row terminator has been consumed. Drivers must read the
     * row via [getResult] and create a fresh instance before the next row.
     */
    internal fun isLineComplete(): Boolean = state == ParseState.END

    /**
     * @return return parsed CSV Fields.
     *         return null, if current position is on the way of csv row.
     */
    fun getResult(): List<String>? {
        return when (state) {
            ParseState.DELIMITER -> {
                fields.add("")
                fields.toList()
            }
            ParseState.QUOTED_FIELD -> null
            ParseState.FIELD, ParseState.QUOTE_END -> {
                fields.add(field.toString())
                fields.toList()
            }
            else -> fields.toList()
        }
    }

    private fun flushField() {
        fields.add(field.toString())
        field.clear()
    }

    /**
     * Consume one additional character following an escape character at an
     * unquoted position (START/DELIMITER/FIELD). The caller must already have
     * accounted for the escape char itself in [pos]; this method advances
     * [pos] by 1 more to consume the escaped char, appends it to [field], and
     * returns [ParseState.FIELD].
     *
     * Accepted [nextCh] values are [escapeChar] and [quoteChar]. When
     * `escapeChar == quoteChar` the accepted set degenerates to a single value,
     * preserving the RFC 4180 strict-doubling behaviour for the default
     * dialect.
     */
    private fun handleUnquotedEscape(nextCh: Char?, rowNum: Long): ParseState {
        if (nextCh != escapeChar && nextCh != quoteChar) {
            throw CsvParseFormatException(
                rowNum,
                pos,
                escapeChar,
                "escape character must be followed by escapeChar($escapeChar) or quoteChar($quoteChar)"
            )
        }
        field.append(nextCh)
        pos += 1
        return ParseState.FIELD
    }
}

private enum class ParseState {
    START,
    FIELD,
    DELIMITER,
    END,
    QUOTE_START,
    QUOTE_END,
    QUOTED_FIELD
}
