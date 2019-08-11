package org.doyaaaaaken.kotlincsv.parser

import java.lang.RuntimeException
import java.util.*

class CsvParser {

    private val BOM = '\uFEFF'

    @UseExperimental(kotlin.ExperimentalStdlibApi::class)
    fun parseLine(line: String, quoteChar: Char, delimiter: Char, escapeChar: Char): List<String>? {
        val chars = line.toCharArray()
        val fields = Vector<String>()
        var field = StringBuilder()
        var state = ParseState.START
        var pos = 0
        val charsLength = chars.size

        if (chars.isNotEmpty() && chars[0] == BOM) {
            pos += 1
        }

        while (state != ParseState.END && pos < charsLength) {
            val c = chars[pos]
            when (state) {
                ParseState.START -> {
                    when (c) {
                        quoteChar -> {
                            state = ParseState.QUOTE_START
                            pos += 1
                        }
                        delimiter -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.DELIMITER
                            pos += 1
                        }
                        '\n', '\u2028', '\u2029', '\u0085' -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        '\r' -> {
                            if (pos + 1 < charsLength && chars[1] == '\n') {
                                pos += 1
                            }
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        else -> {
                            field.append(c)
                            state = ParseState.FIELD
                            pos += 1
                        }
                    }
                }
                ParseState.DELIMITER -> {
                    when (c) {
                        quoteChar -> {
                            state = ParseState.QUOTE_START
                            pos += 1
                        }
                        escapeChar -> {
                            if (pos + 1 < charsLength
                                    && (chars[pos + 1] == escapeChar || chars[pos + 1] == delimiter)) {
                                field.append(chars[pos + 1])
                                state = ParseState.FIELD
                                pos += 2
                            } else {
                                throw MalformedCSVException(chars.concatToString())
                            }
                        }
                        delimiter -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.DELIMITER
                            pos += 1
                        }
                        '\n', '\u2028', '\u2029', '\u0085' -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        '\r' -> {
                            if (pos + 1 < charsLength && chars[1] == '\n') {
                                pos += 1
                            }
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        else -> {
                            field.append(c)
                            state = ParseState.FIELD
                            pos += 1
                        }
                    }
                }
                ParseState.FIELD -> {
                    when (c) {
                        escapeChar -> {
                            if (pos + 1 < charsLength) {
                                if (chars[pos + 1] == escapeChar
                                        || chars[pos + 1] == delimiter) {
                                    field.append(chars[pos + 1])
                                    state = ParseState.FIELD
                                    pos += 2
                                } else {
                                    throw MalformedCSVException(chars.concatToString())
                                }
                            } else {
                                state = ParseState.QUOTE_END
                                pos += 1
                            }
                        }
                        delimiter -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.DELIMITER
                            pos += 1
                        }
                        '\n', '\u2028', '\u2029', '\u0085' -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        '\r' -> {
                            if (pos + 1 < charsLength && chars[1] == '\n') {
                                pos += 1
                            }
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        else -> {
                            field.append(c)
                            state = ParseState.FIELD
                            pos += 1
                        }
                    }
                }
                ParseState.QUOTE_START -> {
                    if (c == escapeChar && escapeChar != quoteChar) {
                        if (pos + 1 < charsLength) {
                            if (chars[pos + 1] == escapeChar
                                    || chars[pos + 1] == quoteChar) {
                                field.append(chars[pos + 1])
                                state = ParseState.QUOTED_FIELD
                                pos += 2
                            } else {
                                throw MalformedCSVException(chars.concatToString())
                            }
                        } else {
                            throw MalformedCSVException(chars.concatToString())
                        }
                    } else if (c == quoteChar) {
                        if (pos + 1 < charsLength && chars[pos + 1] == quoteChar) {
                            field.append(quoteChar)
                            state = ParseState.QUOTED_FIELD
                            pos += 2
                        } else {
                            state = ParseState.QUOTE_END
                            pos += 1
                        }
                    } else {
                        field.append(c)
                        state = ParseState.QUOTED_FIELD
                        pos += 1
                    }
                }
                ParseState.QUOTE_END -> {
                    when (c) {
                        delimiter -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.DELIMITER
                            pos += 1
                        }
                        '\n', '\u2028', '\u2029', '\u0085' -> {
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        '\r' -> {
                            if (pos + 1 < charsLength && chars[1] == '\n') {
                                pos += 1
                            }
                            fields.add(field.toString())
                            field = StringBuilder()
                            state = ParseState.END
                            pos += 1
                        }
                        else -> {
                            throw MalformedCSVException(chars.concatToString())
                        }
                    }
                }
                ParseState.QUOTED_FIELD -> {
                    if (c == escapeChar && escapeChar != quoteChar) {
                        if (pos + 1 < charsLength) {
                            if (chars[pos + 1] == escapeChar
                                    || chars[pos + 1] == quoteChar) {
                                field.append(chars[pos + 1])
                                state = ParseState.QUOTED_FIELD
                                pos += 2
                            } else {
                                throw MalformedCSVException(chars.concatToString())
                            }
                        } else {
                            throw MalformedCSVException(chars.concatToString())
                        }
                    } else if (c == quoteChar) {
                        if (pos + 1 < charsLength && chars[pos + 1] == quoteChar) {
                            field.append(quoteChar)
                            state = ParseState.QUOTED_FIELD
                            pos += 2
                        } else {
                            state = ParseState.QUOTE_END
                            pos += 1
                        }
                    } else {
                        field.append(c)
                        state = ParseState.QUOTED_FIELD
                        pos += 1
                    }
                }
                ParseState.END -> {
                    throw MalformedCSVException("unexpected error")
                }
            }
        }
        return when (state) {
            ParseState.DELIMITER -> {
                fields.add("")
                fields.toList()
            }
            ParseState.QUOTED_FIELD -> {
                null
            }
            else -> {
                // When no crlf at end of file
                when (state) {
                    ParseState.FIELD, ParseState.QUOTE_END -> {
                        fields.add(field.toString())
                    }
                    else -> {
                    }
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

class MalformedCSVException(message: String) : RuntimeException(message)
