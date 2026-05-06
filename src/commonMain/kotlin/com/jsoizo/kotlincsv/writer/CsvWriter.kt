package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.encodeRows

/**
 * Stateless CSV writer. [write] is the lazy core API; [writeAll] is the
 * eager wrapper.
 */
class CsvWriter(val config: CsvWriterConfig = CsvWriterConfig()) {

    /** Encode [rows] into a cold `Sequence<Char>`. Iteration triggers encoding. */
    fun write(rows: Sequence<List<String>>): Sequence<Char> = encodeRows(rows, config)

    /** Eagerly encode [rows] into a single CSV string. */
    fun writeAll(rows: List<List<String>>): String =
        write(rows.asSequence()).joinToString("")
}
