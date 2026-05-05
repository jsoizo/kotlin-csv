package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import com.jsoizo.kotlincsv.reader.internal.parseRows

/**
 * Stateless CSV reader. The same instance can be reused safely across calls
 * and threads — all configuration is captured in [config] and parsing is
 * driven by lazy [Sequence] pipelines.
 *
 * Two parsing entry points are provided:
 * - [read] is the primary core API. Returns a [Sequence] so callers can
 *   short-circuit (`take(n)`, `first()`) without parsing the whole input.
 *   Exceptions are raised at terminal operations (`forEach`, `toList`, ...).
 * - [readAll] is an eager wrapper that parses the entire input into a
 *   `List<List<String>>` up-front.
 */
class CsvReader(val config: CsvReaderConfig = CsvReaderConfig()) {

    /**
     * Parse [chars] into a lazy sequence of rows.
     *
     * The returned [Sequence] is cold; iteration triggers parsing. Empty input
     * produces an empty sequence. Per [CsvReaderConfig.skipEmptyLine] empty
     * rows may be filtered out, and per the row-count behaviour fields rows
     * with the wrong field count are either dropped, padded, truncated, or
     * cause [CsvFieldNumDifferentException] to be thrown at terminal time.
     *
     * @throws CsvParseFormatException on terminal operation, when [chars]
     *   violates the CSV format (unbalanced quote, illegal escape, ...).
     * @throws CsvFieldNumDifferentException on terminal operation, when a row
     *   has more or fewer fields than the first row and the corresponding
     *   policy is [ExcessFieldsRowBehaviour.ERROR] or
     *   [InsufficientFieldsRowBehaviour.ERROR].
     */
    fun read(chars: Sequence<Char>): Sequence<List<String>> {
        val parsed = parseRows(chars, config.dialect)
        val filtered = if (config.skipEmptyLine) {
            parsed.filter { row -> !isEmptyRow(row) }
        } else {
            parsed
        }
        return applyFieldCountPolicy(filtered)
    }

    /**
     * Eagerly parse [text] into a list of rows.
     *
     * @throws CsvParseFormatException when [text] violates the CSV format.
     * @throws CsvFieldNumDifferentException when a row's field count differs
     *   from the first row and the corresponding policy is
     *   [ExcessFieldsRowBehaviour.ERROR] or
     *   [InsufficientFieldsRowBehaviour.ERROR].
     */
    fun readAll(text: String): List<List<String>> = read(text.asSequence()).toList()

    private fun isEmptyRow(row: List<String>): Boolean =
        row.isEmpty() || (row.size == 1 && row.single().isBlank())

    private fun applyFieldCountPolicy(rows: Sequence<List<String>>): Sequence<List<String>> = sequence {
        var expected: Int? = null
        var rowNum = 0L
        for (row in rows) {
            rowNum++
            val current = expected
            // First row establishes the expected field count for all later rows.
            if (current == null) {
                expected = row.size
                yield(row)
                continue
            }
            when {
                row.size > current -> when (config.excessFieldsRowBehaviour) {
                    ExcessFieldsRowBehaviour.ERROR ->
                        throw CsvFieldNumDifferentException(current, row.size, rowNum)

                    ExcessFieldsRowBehaviour.IGNORE ->
                        Unit

                    ExcessFieldsRowBehaviour.TRIM ->
                        yield(row.subList(0, current))
                }

                row.size < current -> when (config.insufficientFieldsRowBehaviour) {
                    InsufficientFieldsRowBehaviour.ERROR ->
                        throw CsvFieldNumDifferentException(current, row.size, rowNum)

                    InsufficientFieldsRowBehaviour.IGNORE ->
                        Unit

                    InsufficientFieldsRowBehaviour.EMPTY_STRING ->
                        yield(row + List(current - row.size) { "" })
                }

                else -> yield(row)
            }
        }
    }
}
