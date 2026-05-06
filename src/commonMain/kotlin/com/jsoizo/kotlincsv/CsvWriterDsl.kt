package com.jsoizo.kotlincsv

import com.jsoizo.kotlincsv.writer.CsvWriter
import com.jsoizo.kotlincsv.writer.CsvWriterConfig
import com.jsoizo.kotlincsv.writer.CsvWriterConfigBuilder

/**
 * DSL entry point for [CsvWriter]. The builder is frozen into an immutable
 * [CsvWriterConfig] before the writer is created.
 */
fun csvWriter(init: CsvWriterConfigBuilder.() -> Unit = {}): CsvWriter =
    CsvWriter(CsvWriterConfigBuilder().apply(init).build())

/** Construct a [CsvWriter] from a pre-built [CsvWriterConfig]. */
fun csvWriter(config: CsvWriterConfig): CsvWriter = CsvWriter(config)
