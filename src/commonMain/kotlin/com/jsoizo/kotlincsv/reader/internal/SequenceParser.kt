package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect

private const val BOM_CHAR = '\uFEFF'

/** Lazily parse a `Sequence<Char>` into a `Sequence<List<String>>` of CSV rows. */
internal fun parseRows(
    chars: Sequence<Char>,
    dialect: CsvDialect,
): Sequence<List<String>> =
    parseRowsWithMetadata(chars, dialect).map { row -> row.map { it.value } }

/** Lazily parse a `Sequence<Char>` into CSV rows with quoted-field metadata. */
internal fun parseRowsWithMetadata(
    chars: Sequence<Char>,
    dialect: CsvDialect,
): Sequence<List<ParsedCsvField>> = sequence {
    val iter = chars.iterator()
    if (!iter.hasNext()) return@sequence

    val quoteChar = dialect.quoteChar
    val delimiter = dialect.delimiter
    val escapeChar = dialect.escapeChar

    val stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar)
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
                stateMachine.finishRow()?.let { yield(it) }
                rowNum++
                stateMachine.reset()
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

/**
 * Lazily parse CSV rows from a chunked char source.
 *
 * [readInto] fills the supplied `CharArray` and returns the number of chars
 * written (0 signals EOF). The chunk size is controlled by the caller via the
 * size of the buffer it receives.
 *
 * This path avoids the per-character `sequence { yield(c) }` overhead of
 * [parseRows] (Continuation allocation per char) by walking the buffer
 * directly. Per-row yields remain — the public API is still a lazy
 * `Sequence<List<String>>`.
 *
 * Double-buffering is used so the parser can supply the next-char lookahead
 * across chunk boundaries.
 */
internal fun parseRowsFromChunks(
    readInto: (CharArray) -> Int,
    dialect: CsvDialect,
    stripBom: Boolean = false,
    bufferSize: Int = 8192,
): Sequence<List<String>> =
    parseRowsWithMetadataFromChunks(readInto, dialect, stripBom, bufferSize).map { row -> row.map { it.value } }

/** Lazily parse chunked CSV rows with quoted-field metadata. */
internal fun parseRowsWithMetadataFromChunks(
    readInto: (CharArray) -> Int,
    dialect: CsvDialect,
    stripBom: Boolean = false,
    bufferSize: Int = 8192,
): Sequence<List<ParsedCsvField>> = sequence {
    require(bufferSize >= 2) { "bufferSize must be >= 2 to hold a UTF-16 surrogate pair" }

    val bufferA = CharArray(bufferSize)
    val bufferB = CharArray(bufferSize)

    var currentBuffer = bufferA
    var currentLength = readInto(currentBuffer)
    if (currentLength <= 0) return@sequence

    var nextBuffer = bufferB
    var nextLength = readInto(nextBuffer)

    val quoteChar = dialect.quoteChar
    val delimiter = dialect.delimiter
    val escapeChar = dialect.escapeChar
    val machine = ParseStateMachine(quoteChar, delimiter, escapeChar)

    var rowNum = 1L
    var skipCount = 0L
    var machineHasInput = false
    var first = true

    var index = 0
    while (true) {
        if (index >= currentLength) {
            if (nextLength <= 0) break
            val swap = currentBuffer
            currentBuffer = nextBuffer
            nextBuffer = swap
            currentLength = nextLength
            nextLength = readInto(nextBuffer)
            index = 0
        }

        val ch = currentBuffer[index]
        if (first) {
            first = false
            if (stripBom && ch == BOM_CHAR) {
                index++
                continue
            }
        }

        val nextCh: Char? = when {
            index + 1 < currentLength -> currentBuffer[index + 1]
            nextLength > 0 -> nextBuffer[0]
            else -> null
        }

        if (skipCount > 0L) {
            skipCount--
        } else {
            skipCount = machine.read(ch, nextCh, rowNum) - 1L
            machineHasInput = true

            if (machine.isLineComplete()) {
                machine.finishRow()?.let { yield(it) }
                rowNum++
                machine.reset()
                machineHasInput = false
            }
        }

        index++
    }

    if (machineHasInput) {
        machine.finishFinalRow(rowNum)?.let { yield(it) }
    }
}
