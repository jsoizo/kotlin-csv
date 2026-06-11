package com.jsoizo.kotlincsv.reader.internal

import com.jsoizo.kotlincsv.CsvDialect

private const val BOM_CHAR_CODE = 0xFEFF

/** Lazily parse a `Sequence<Char>` into a `Sequence<List<String>>` of CSV rows. */
internal fun parseRows(
    chars: Sequence<Char>,
    dialect: CsvDialect,
): Sequence<List<String>> =
    parseRowsWithMetadata(chars, dialect).map { row -> row.map { it.value } }

/** Lazily parse a `Sequence<Char>` into CSV rows with quoted-field metadata. */
internal fun parseRowsWithMetadata(
    chars: Sequence<Char>,
    dialect: CsvDialect,
): Sequence<List<ParsedCsvField>> = sequence {
    val parser = RowParser(dialect, IteratorCharCursor(chars.iterator()))
    while (true) {
        val row = parser.nextRow() ?: break
        yield(row)
    }
}

/**
 * Lazily parse CSV rows from a chunked char source.
 *
 * [readInto] fills the supplied `CharArray` and returns the number of chars
 * written (0 signals EOF). The chunk size is controlled by the caller via the
 * size of the buffer it receives.
 *
 * This path avoids the per-character `sequence { yield(c) }` overhead of
 * [parseRows] (Continuation allocation per char) by walking the buffer
 * directly inside [ChunkCharCursor]. Per-row yields remain — the public API
 * is still a lazy `Sequence<List<String>>`.
 */
internal fun parseRowsFromChunks(
    readInto: (CharArray) -> Int,
    dialect: CsvDialect,
    stripBom: Boolean = false,
    bufferSize: Int = 8192,
): Sequence<List<String>> =
    parseRowsWithMetadataFromChunks(readInto, dialect, stripBom, bufferSize).map { row -> row.map { it.value } }

/** Lazily parse chunked CSV rows with quoted-field metadata. */
internal fun parseRowsWithMetadataFromChunks(
    readInto: (CharArray) -> Int,
    dialect: CsvDialect,
    stripBom: Boolean,
    bufferSize: Int = 8192,
): Sequence<List<ParsedCsvField>> = sequence {
    require(bufferSize >= 2) { "bufferSize must be >= 2 to hold a UTF-16 surrogate pair" }

    val cursor = ChunkCharCursor(readInto, bufferSize)
    if (stripBom && cursor.cur == BOM_CHAR_CODE) cursor.advance()

    val parser = RowParser(dialect, cursor)
    while (true) {
        val row = parser.nextRow() ?: break
        yield(row)
    }
}
