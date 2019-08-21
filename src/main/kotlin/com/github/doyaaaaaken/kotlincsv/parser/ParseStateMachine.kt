package com.github.doyaaaaaken.kotlincsv.parser

import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import java.util.*

/**
 * @author doyaaaaaaken
 */
internal class ParseStateMachine(
        private val quoteChar: Char,
        private val delimiter: Char,
        private val escapeChar: Char
) {

    private val BOM = '\uFEFF'

    private var state = ParseState.START

    private val fields = Vector<String>()

    private var field = StringBuilder()

    private var pos = 0

    fun read(ch: Char, nextCh: Char?): Int {
        val prevPos = pos
        when (state) {
            ParseState.START -> {
                when (ch) {
                    BOM -> Unit
                    quoteChar -> state = ParseState.QUOTE_START
                    delimiter -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        fields.add(field.toString())
                        field = StringBuilder()
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
                        if (nextCh != null) {
                            if (nextCh == escapeChar || nextCh == delimiter) {
                                field.append(nextCh)
                                state = ParseState.FIELD
                                pos += 1
                            } else {
                                throw MalformedCSVException("$pos")
                            }
                        } else {
                            state = ParseState.QUOTE_END
                        }
                    }
                    delimiter -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        fields.add(field.toString())
                        field = StringBuilder()
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
                    escapeChar -> {
                        if (nextCh == escapeChar || nextCh == delimiter) {
                            field.append(nextCh)
                            state = ParseState.FIELD
                            pos += 1
                        } else {
                            throw MalformedCSVException("$pos")
                        }
                    }
                    delimiter -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') pos += 1
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    else -> {
                        field.append(ch)
                        state = ParseState.FIELD
                    }
                }
                pos += 1
            }
            ParseState.QUOTE_START -> {
                if (ch == escapeChar && escapeChar != quoteChar) {
                    if (nextCh != null) {
                        if (nextCh == escapeChar
                                || nextCh == quoteChar) {
                            field.append(nextCh)
                            state = ParseState.QUOTED_FIELD
                            pos += 1
                        } else {
                            throw MalformedCSVException("$pos")
                        }
                    } else {
                        throw MalformedCSVException("$pos")
                    }
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
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.DELIMITER
                    }
                    '\n', '\u2028', '\u2029', '\u0085' -> {
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    '\r' -> {
                        if (nextCh == '\n') {
                            pos += 1
                        }
                        fields.add(field.toString())
                        field = StringBuilder()
                        state = ParseState.END
                    }
                    else -> {
                        throw MalformedCSVException("$pos")
                    }
                }
                pos += 1
            }
            ParseState.QUOTED_FIELD -> {
                if (ch == escapeChar && escapeChar != quoteChar) {
                    if (nextCh != null) {
                        if (nextCh == escapeChar
                                || nextCh == quoteChar) {
                            field.append(nextCh)
                            state = ParseState.QUOTED_FIELD
                            pos += 1
                        } else {
                            throw MalformedCSVException("$pos")
                        }
                    } else {
                        throw MalformedCSVException("$pos")
                    }
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
            ParseState.END -> {
                throw MalformedCSVException("unexpected error")
            }
        }
        return pos - prevPos
    }

    fun getResult(): List<String>? {
        return when (state) {
            ParseState.DELIMITER -> {
                fields.add("")
                fields.toList()
            }
            ParseState.QUOTED_FIELD -> null
            else -> {
                // When no crlf at end of file
                if (state == ParseState.FIELD || state == ParseState.QUOTE_END) {
                    fields.add(field.toString())
                }
                fields.toList()
            }
        }
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
