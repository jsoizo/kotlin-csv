package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.reader.CsvReader
import com.jsoizo.kotlincsv.reader.CsvReaderConfig
import com.jsoizo.kotlincsv.reader.CsvReaderConfigBuilder

/**
 * DSL entry point for [CsvReader]. The builder is frozen into an immutable
 * [CsvReaderConfig] before the reader is created.
 */
fun csvReader(init: CsvReaderConfigBuilder.() -> Unit = {}): CsvReader =
    CsvReader(CsvReaderConfigBuilder().apply(init).build())

/** Construct a [CsvReader] from a pre-built [CsvReaderConfig]. */
fun csvReader(config: CsvReaderConfig): CsvReader = CsvReader(config)
