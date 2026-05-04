package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import com.jsoizo.kotlincsv.parser.ParseStateMachine
import com.jsoizo.kotlincsv.util.CSVParseFormatException as LegacyCsvParseFormatException

/**
 * Lazily parse a [Sequence] of [Char] into a [Sequence] of CSV rows
 * (`List<String>`).
 *
 * The parsing is driven by a fresh [ParseStateMachine] per row; the state
 * machine is recreated each time a row completes (state transitions to END).
 * Empty input produces an empty sequence.
 *
 * Exceptions thrown by [ParseStateMachine] (currently the legacy
 * [LegacyCsvParseFormatException] type) are converted to the new
 * [CsvParseFormatException] type so callers see a consistent exception
 * hierarchy.
 */
internal fun parseRows(
    chars: Sequence<Char>,
    dialect: CsvDialect,
): Sequence<List<String>> = sequence {
    val iter = chars.iterator()
    if (!iter.hasNext()) return@sequence

    val quoteChar = dialect.quoteChar
    val delimiter = dialect.delimiter
    val escapeChar = dialect.escapeChar

    var stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar)
    var current: Char = iter.next()
    var skipCount = 0L
    var rowNum = 1L
    var stateMachineHasInput = false

    while (true) {
        val nextCh: Char? = if (iter.hasNext()) iter.next() else null

        if (skipCount > 0L) {
            skipCount--
        } else {
            skipCount = try {
                stateMachine.read(current, nextCh, rowNum) - 1L
            } catch (e: LegacyCsvParseFormatException) {
                throw convertException(e)
            }
            stateMachineHasInput = true

            if (stateMachine.isLineComplete()) {
                stateMachine.getResult()?.let { yield(it) }
                rowNum++
                stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar)
                stateMachineHasInput = false
            }
        }

        if (nextCh == null) break
        current = nextCh
    }

    if (stateMachineHasInput) {
        stateMachine.getResult()?.let { yield(it) }
    }
}

private fun convertException(e: LegacyCsvParseFormatException): CsvParseFormatException {
    val original = e.message?.substringBefore(" [rowNum = ")
        ?: "Exception happened on parsing csv"
    return CsvParseFormatException(
        rowNum = e.rowNum,
        colIndex = e.colIndex,
        char = e.char,
        message = original,
    )
}
