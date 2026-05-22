package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import com.jsoizo.kotlincsv.reader.internal.parseRows

/**
 * Stateless CSV reader. [read] is the lazy core API; [readAll] is the eager
 * wrapper.
 */
class CsvReader(val config: CsvReaderConfig = CsvReaderConfig()) {

    /**
     * Parse [chars] into a cold `Sequence<List<String>>`. Iteration triggers
     * parsing.
     *
     * @throws CsvParseFormatException on terminal operation when [chars]
     *   violates the CSV format.
     * @throws CsvFieldNumDifferentException on terminal operation when a row
     *   violates the configured field-count policy. Its `rowNum` is a CSV row
     *   number after reader filters such as `skipEmptyLine`.
     */
    fun read(chars: Sequence<Char>): Sequence<List<String>> =
        applyPipeline(parseRows(chars, config.dialect))

    /** Eagerly parse [text] into a list of rows. */
    fun readAll(text: String): List<List<String>> = read(text.asSequence()).toList()

    /**
     * Apply skipEmptyLine filter and field-count policy to a parsed row
     * sequence. Used by I/O wrappers that obtain rows from chunked parsers
     * without routing chars through a `Sequence<Char>`.
     */
    internal fun applyPipeline(parsed: Sequence<List<String>>): Sequence<List<String>> {
        val filtered = if (config.skipEmptyLine) {
            parsed.filter { row -> !isEmptyRow(row) }
        } else {
            parsed
        }
        return applyFieldCountPolicy(filtered)
    }

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
