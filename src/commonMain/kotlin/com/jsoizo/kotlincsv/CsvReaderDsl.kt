package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.CsvReader
import com.jsoizo.kotlincsv.reader.CsvReaderConfig
import com.jsoizo.kotlincsv.reader.CsvReaderConfigBuilder

/**
 * DSL entry point for constructing a [CsvReader].
 *
 * Inside [init], assign properties on the [CsvReaderConfigBuilder] receiver.
 * The builder is collapsed into an immutable [CsvReaderConfig] before the
 * reader is created, so the resulting [CsvReader] is unaffected by later
 * mutation of the builder reference.
 *
 * Example:
 * ```
 * val reader = csvReader {
 *     dialect = CsvDialect.TSV
 *     skipEmptyLine = true
 * }
 * ```
 */
fun csvReader(init: CsvReaderConfigBuilder.() -> Unit = {}): CsvReader =
    CsvReader(CsvReaderConfigBuilder().apply(init).build())

/**
 * Construct a [CsvReader] from a pre-built [CsvReaderConfig]. Useful when the
 * configuration is composed elsewhere (e.g. from a config object copy chain)
 * and reused across multiple readers.
 */
fun csvReader(config: CsvReaderConfig): CsvReader = CsvReader(config)
