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
 * Read CSV rows from [source] using a UTF-8 decode and pass them to [block].
 *
 * On JVM, bytes are decoded one Unicode code point at a time and pushed
 * through a lazy `Sequence<Char>`, so the parser can short-circuit (`take`,
 * `first`, ...) and stop pulling bytes from [source] mid-stream. On JS
 * (Node.js) the underlying `kotlinx-io` `FileSource` loads the whole file at
 * the first read, so streaming is effectively JVM-only — the `Sequence` shape
 * is preserved on JS for API uniformity but yields from an in-memory buffer.
 * If [CsvReadIoOptions.stripBom] is `true` and the very first code point is
 * U+FEFF (BOM), it is dropped.
 *
 * Resource ownership of [source] stays with the caller; [block] is invoked
 * while [source] is still open so iteration can pull bytes on demand.
 *
 * @return whatever [block] returns.
 */
fun <T> CsvReader.read(
    source: Source,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = block(read(source.toCharSequence(options.stripBom)))

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
 * Read CSV rows from the file at [path] using a UTF-8 decode and pass them to
 * [block]. The underlying source is closed when [block] returns or throws.
 *
 * On JVM, [path] is interpreted via the system filesystem. On JS (Node.js),
 * the entire file is loaded into memory by `kotlinx-io` before parsing
 * starts; streaming behaviour is therefore JVM-only despite the [Sequence]
 * return type of the inner read.
 *
 * @return whatever [block] returns.
 */
fun <T> CsvReader.read(
    path: Path,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = SystemFileSystem.source(path).buffered().use { bufferedSource ->
    read(bufferedSource, options, block)
}

/**
 * Convenience overload that builds a [Path] from a string. Lets callers avoid
 * importing `kotlinx.io.files.Path`. Relative paths follow `SystemFileSystem`
 * platform behaviour (typically the current working directory).
 */
fun <T> CsvReader.read(
    filePath: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = read(Path(filePath), options, block)
