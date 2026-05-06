package com.jsoizo.kotlincsv.writer

/**
 * I/O-layer options for [CsvWriter].
 *
 * @property prependBom Write a U+FEFF before the encoded body. Defaults to
 *   `false`. Enable for Excel-friendly UTF-8 output.
 */
data class CsvWriteIoOptions(
    val prependBom: Boolean = false,
)
