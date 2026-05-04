package com.jsoizo.kotlincsv.writer.internal

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.writer.CsvWriterConfig
import com.jsoizo.kotlincsv.writer.WriteQuoteMode

/**
 * Lazily encode a [Sequence] of CSV rows (`List<String>`) into a [Sequence]
 * of [Char].
 *
 * A single line terminator is emitted between rows. A trailing terminator
 * after the final row is emitted only when [CsvWriterConfig.outputLastLineTerminator]
 * is `true`. An empty input sequence produces an empty output sequence with
 * no terminator at all.
 *
 * The implementation pulls one row at a time using the source sequence's
 * iterator, so the laziness of the input is preserved end-to-end and a
 * downstream `take(n)` consumes only the rows it needs.
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
            needsCanonicalQuote(field, quoteChar, dialect.delimiter, dialect.lineTerminator)
        WriteQuoteMode.NON_NUMERIC -> !isDecimalNumber(field)
    }

    if (shouldQuote) yield(quoteChar)
    if (escapeChar == quoteChar) {
        // RFC 4180 §2.7 doubling style. v1 Writer 互換 (kotlin-csv v1 はこのモードのみ).
        for (ch in field) {
            if (ch == quoteChar) yield(quoteChar)
            yield(ch)
        }
    } else {
        // Explicit escape style — v2 で新規対応 (CSV 標準からの拡張).
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
    delimiter: Char,
    lineTerminator: String,
): Boolean {
    val ltFirst = lineTerminator.firstOrNull()
    for (ch in field) {
        if (ch == quoteChar || ch == delimiter) return true
        if (ch == '\n' || ch == '\r') return true
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
