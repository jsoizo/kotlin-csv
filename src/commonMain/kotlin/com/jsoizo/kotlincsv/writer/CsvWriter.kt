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

    /** Encode nullable [rows]. Null fields are emitted as unquoted empty fields. */
    fun writeNullable(rows: Sequence<List<String?>>): Sequence<Char> = encodeNullableRows(rows, config)

    /** Eagerly encode nullable [rows] into a single CSV string. */
    fun writeAllNullable(rows: List<List<String?>>): String = buildString {
        appendNullableRows(rows.asSequence(), config, this)
    }
}
