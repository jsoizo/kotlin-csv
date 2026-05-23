package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect

/** Lazily parse a `Sequence<Char>` into a `Sequence<List<String>>` of CSV rows. */
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
            skipCount = stateMachine.read(current, nextCh, rowNum) - 1L
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
        stateMachine.finishFinalRow(rowNum)?.let { yield(it) }
    }
}
