package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.CsvWriter
import com.jsoizo.kotlincsv.writer.CsvWriterConfig
import com.jsoizo.kotlincsv.writer.CsvWriterConfigBuilder

/**
 * DSL entry point for constructing a [CsvWriter].
 *
 * Inside [init], assign properties on the [CsvWriterConfigBuilder] receiver.
 * The builder is collapsed into an immutable [CsvWriterConfig] before the
 * writer is created, so the resulting [CsvWriter] is unaffected by later
 * mutation of the builder reference.
 *
 * Example:
 * ```
 * val writer = csvWriter {
 *     dialect = CsvDialect.TSV
 *     outputLastLineTerminator = false
 * }
 * ```
 */
fun csvWriter(init: CsvWriterConfigBuilder.() -> Unit = {}): CsvWriter =
    CsvWriter(CsvWriterConfigBuilder().apply(init).build())

/**
 * Construct a [CsvWriter] from a pre-built [CsvWriterConfig]. Useful when the
 * configuration is composed elsewhere (e.g. from a config object copy chain)
 * and reused across multiple writers.
 */
fun csvWriter(config: CsvWriterConfig): CsvWriter = CsvWriter(config)
