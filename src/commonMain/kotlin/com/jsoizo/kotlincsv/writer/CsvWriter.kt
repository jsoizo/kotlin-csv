package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.appendRows
import com.jsoizo.kotlincsv.writer.internal.appendNullableRows
import com.jsoizo.kotlincsv.writer.internal.encodeRows
import com.jsoizo.kotlincsv.writer.internal.encodeNullableRows

/**
 * Stateless CSV writer. [write] is the lazy core API; [writeAll] is the
 * eager wrapper.
 */
class CsvWriter(val config: CsvWriterConfig = CsvWriterConfig()) {

    /** Encode [rows] into a cold `Sequence<Char>`. Iteration triggers encoding. */
    fun write(rows: Sequence<List<String>>): Sequence<Char> = encodeRows(rows, config)

    /** Eagerly encode [rows] into a single CSV string. */
    fun writeAll(rows: List<List<String>>): String = buildString {
        appendRows(rows.asSequence(), config, this)
    }

    /**
     * Encode one CSV record with the same rules as `writeAll(listOf(row))`.
     * Includes the configured line terminator only when
     * [CsvWriterConfig.outputLastLineTerminator] is `true`. A record may
     * contain quoted line breaks; no I/O resource is opened or closed.
     */
    fun writeRow(row: List<String>): String = writeAll(listOf(row))

    /** Encode nullable [rows]. Null fields are emitted as unquoted empty fields. */
    fun writeNullable(rows: Sequence<List<String?>>): Sequence<Char> = encodeNullableRows(rows, config)

    /** Eagerly encode nullable [rows] into a single CSV string. */
    fun writeAllNullable(rows: List<List<String?>>): String = buildString {
        appendNullableRows(rows.asSequence(), config, this)
    }

    /**
     * Encode one nullable CSV record with the same rules as
     * `writeAllNullable(listOf(row))`, including the final-terminator option.
     * Null fields remain unquoted empty fields, even when all non-null
     * fields are quoted.
     */
    fun writeNullableRow(row: List<String?>): String = writeAllNullable(listOf(row))
}
