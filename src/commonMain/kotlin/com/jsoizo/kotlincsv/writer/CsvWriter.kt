package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.encodeRows

/**
 * Stateless CSV writer. The same instance can be reused safely across calls
 * and threads — all configuration is captured in [config] and encoding is
 * driven by lazy [Sequence] pipelines.
 *
 * Two encoding entry points are provided:
 * - [write] is the primary core API. Returns a `Sequence<Char>` so the I/O
 *   layer can stream output to a `Sink` without materialising the whole
 *   document in memory. The sequence is cold; iteration triggers encoding.
 * - [writeAll] is an eager wrapper that serialises the entire row list into
 *   a `String` up-front.
 */
class CsvWriter(private val config: CsvWriterConfig = CsvWriterConfig()) {

    /**
     * Encode [rows] into a lazy sequence of characters.
     *
     * A single line terminator is emitted between rows. A trailing terminator
     * after the final row is emitted only when
     * [CsvWriterConfig.outputLastLineTerminator] is `true`. An empty input
     * sequence produces an empty output sequence — no terminator is emitted.
     */
    fun write(rows: Sequence<List<String>>): Sequence<Char> = encodeRows(rows, config)

    /** Eagerly encode [rows] into a single CSV string. */
    fun writeAll(rows: List<List<String>>): String =
        write(rows.asSequence()).joinToString("")
}
