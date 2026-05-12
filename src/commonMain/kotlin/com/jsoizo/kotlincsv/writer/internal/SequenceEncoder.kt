package com.jsoizo.kotlincsv.writer.internal

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.writer.CsvWriterConfig
import com.jsoizo.kotlincsv.writer.WriteQuoteMode

/**
 * Lazily encode CSV rows into a `Sequence<Char>`. A trailing line terminator
 * is emitted only when [CsvWriterConfig.outputLastLineTerminator] is `true`.
 */
internal fun encodeRows(
    rows: Sequence<List<String>>,
    config: CsvWriterConfig,
): Sequence<Char> = sequence {
    val dialect = config.dialect
    val lineTerminator = dialect.lineTerminator
    val iter = rows.iterator()
    if (!iter.hasNext()) return@sequence

    yieldAll(encodeRow(iter.next(), dialect, config.quoteMode))
    while (iter.hasNext()) {
        yieldAll(lineTerminator.asSequence())
        yieldAll(encodeRow(iter.next(), dialect, config.quoteMode))
    }
    if (config.outputLastLineTerminator) {
        yieldAll(lineTerminator.asSequence())
    }
}

private fun encodeRow(
    row: List<String>,
    dialect: CsvDialect,
    quoteMode: WriteQuoteMode,
): Sequence<Char> = sequence {
    val delimiter = dialect.delimiter
    var first = true
    for (field in row) {
        if (!first) yield(delimiter)
        yieldAll(encodeField(field, dialect, quoteMode))
        first = false
    }
}

private fun encodeField(
    field: String,
    dialect: CsvDialect,
    quoteMode: WriteQuoteMode,
): Sequence<Char> = sequence {
    val quoteChar = dialect.quoteChar
    val escapeChar = dialect.escapeChar

    val shouldQuote = when (quoteMode) {
        WriteQuoteMode.ALL -> true
        WriteQuoteMode.CANONICAL ->
            needsCanonicalQuote(field, quoteChar, escapeChar, dialect.delimiter, dialect.lineTerminator)
        WriteQuoteMode.NON_NUMERIC -> !isDecimalNumber(field)
    }

    if (shouldQuote) yield(quoteChar)
    if (escapeChar == quoteChar) {
        // RFC 4180 §2.7 doubling style.
        for (ch in field) {
            if (ch == quoteChar) yield(quoteChar)
            yield(ch)
        }
    } else {
        // Explicit escape style (CSV extension).
        for (ch in field) when (ch) {
            quoteChar, escapeChar -> { yield(escapeChar); yield(ch) }
            else                  -> yield(ch)
        }
    }
    if (shouldQuote) yield(quoteChar)
}

private fun needsCanonicalQuote(
    field: String,
    quoteChar: Char,
    escapeChar: Char,
    delimiter: Char,
    lineTerminator: String,
): Boolean {
    val ltFirst = lineTerminator.firstOrNull()
    val explicitEscape = escapeChar != quoteChar
    for (ch in field) {
        if (ch == quoteChar || ch == delimiter) return true
        if (explicitEscape && ch == escapeChar) return true
        if (ch == '\n' || ch == '\r' || ch == '\u2028' || ch == '\u2029' || ch == '\u0085') return true
        if (ltFirst != null && ch == ltFirst) return true
    }
    return false
}

private fun isDecimalNumber(field: String): Boolean {
    if (field.isEmpty()) return false
    var foundDot = false
    for (ch in field) {
        when {
            ch == '.' && !foundDot -> foundDot = true
            ch in '0'..'9' -> Unit
            else -> return false
        }
    }
    return true
}
