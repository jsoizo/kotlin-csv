package com.jsoizo.kotlincsv.reader

/**
 * I/O-layer options applied when a [CsvReader] reads from a `Source` or `Path`.
 *
 * @property stripBom Whether to strip a leading U+FEFF (BOM) from the decoded text
 *   before parsing. Defaults to `true` so that BOM-prefixed UTF-8 files produced by
 *   Excel and similar tools are read transparently.
 */
data class CsvReadIoOptions(
    val stripBom: Boolean = true,
)
