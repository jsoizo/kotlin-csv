package com.jsoizo.kotlincsv.writer

/**
 * I/O-layer options applied when a [CsvWriter] writes to a `Sink` or `Path`.
 *
 * @property prependBom Whether to write a U+FEFF (BOM) before the encoded CSV body.
 *   Defaults to `false`. Enable when the consumer expects a BOM-prefixed UTF-8 stream,
 *   e.g. Excel-friendly output.
 */
data class CsvWriteIoOptions(
    val prependBom: Boolean = false,
)
