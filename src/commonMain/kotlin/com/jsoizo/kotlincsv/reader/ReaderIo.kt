package com.jsoizo.kotlincsv.reader

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readCodePointValue

private const val BOM_CODE_POINT = 0xFEFF
private const val SUPPLEMENTARY_PLANE_START = 0x10000
private const val HIGH_SURROGATE_BASE = 0xD800
private const val LOW_SURROGATE_BASE = 0xDC00
private const val LOW_TEN_BIT_MASK = 0x3FF

/**
 * Read CSV rows from [source] (UTF-8) and pass them to [block].
 * [source] is caller-owned — this function does not close it. The `Sequence`
 * passed to [block] must be consumed inside the block.
 */
fun <T> CsvReader.read(
    source: Source,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = block(read(source.toCharSequence(options.stripBom)))

/** Eagerly read all CSV rows from [source] (UTF-8). [source] is caller-owned. */
fun CsvReader.readAll(
    source: Source,
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = read(source, options) { it.toList() }

private fun Source.toCharSequence(stripBom: Boolean): Sequence<Char> = sequence {
    var first = true
    while (!exhausted()) {
        val codePoint = readCodePointValue()
        if (first) {
            first = false
            if (stripBom && codePoint == BOM_CODE_POINT) continue
        }
        if (codePoint < SUPPLEMENTARY_PLANE_START) {
            yield(codePoint.toChar())
        } else {
            // Supplementary plane: emit a UTF-16 surrogate pair.
            val offset = codePoint - SUPPLEMENTARY_PLANE_START
            yield((HIGH_SURROGATE_BASE + (offset shr 10)).toChar())
            yield((LOW_SURROGATE_BASE + (offset and LOW_TEN_BIT_MASK)).toChar())
        }
    }
}

/**
 * Read CSV rows from the file at [path] (UTF-8) and pass them to [block].
 * The underlying source is closed when [block] returns or throws.
 *
 * The `Sequence` passed to [block] must be consumed inside the block —
 * returning it leaks a handle to a now-closed source.
 */
fun <T> CsvReader.readFromFile(
    path: Path,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = SystemFileSystem.source(path).buffered().use { bufferedSource ->
    read(bufferedSource, options, block)
}

/** Eagerly read all CSV rows from the file at [path] (UTF-8). */
fun CsvReader.readAllFromFile(
    path: Path,
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = readFromFile(path, options) { it.toList() }

/**
 * String-path overload of [readFromFile]. The `Sequence` passed to [block]
 * must be consumed inside the block.
 */
fun <T> CsvReader.readFromFile(
    filePath: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = readFromFile(Path(filePath), options, block)

/** String-path overload of [readAllFromFile]. */
fun CsvReader.readAllFromFile(
    filePath: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = readFromFile(filePath, options) { it.toList() }
