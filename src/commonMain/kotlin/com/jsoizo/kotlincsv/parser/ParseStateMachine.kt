package com.jsoizo.kotlincsv.parser

import com.jsoizo.kotlincsv.util.CSVParseFormatException
import com.jsoizo.kotlincsv.util.Const

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
                    Const.BOM -> Unit
                    quoteChar -> state = ParseState.QUOTE_START
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
                    escapeChar -> {
                        if (nextCh != escapeChar) throw CSVParseFormatException(
                            rowNum,
                            pos,
                            ch,
                            "must appear escapeChar($escapeChar) after escapeChar($escapeChar)"
                        )
                        field.append(nextCh)
                        state = ParseState.FIELD
                        pos += 1
                    }
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
                    if (nextCh == null) throw CSVParseFormatException(rowNum, pos, ch, "end of quote doesn't exist")
                    if (nextCh != escapeChar && nextCh != quoteChar) throw CSVParseFormatException(
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
                    else -> throw CSVParseFormatException(
                        rowNum,
                        pos,
                        ch,
                        "must appear delimiter or line terminator after quote end"
                    )
                }
                pos += 1
            }
            ParseState.END -> throw CSVParseFormatException(rowNum, pos, ch, "unexpected error")
        }
        return pos - prevPos
    }

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
