package com.jsoizo.kotlincsv.reader

/**
 * I/O-layer options for [CsvReader].
 *
 * @property stripBom Drop a leading U+FEFF after charset decoding. Defaults
 *   to `true` so BOM-prefixed UTF-8 files (e.g. produced by Excel) parse cleanly.
 */
data class CsvReadIoOptions(
    val stripBom: Boolean = true,
)
