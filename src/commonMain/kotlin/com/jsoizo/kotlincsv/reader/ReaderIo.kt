package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import kotlinx.io.IOException
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
 * (Node.js) the underlying `kotlinx-io` `FileSource` reads the whole file
 * into memory via `fs.readFileSync` on the first call, so streaming is
 * effectively JVM-only — the `Sequence` shape is preserved on JS for API
 * uniformity but yields from an in-memory buffer.
 * If [CsvReadIoOptions.stripBom] is `true` and the very first code point is
 * U+FEFF (BOM), it is dropped.
 *
 * Resource ownership of [source] stays with the caller; [block] is invoked
 * while [source] is still open so iteration can pull bytes on demand.
 *
 * The [Sequence] passed to [block] must be consumed inside the block.
 * Returning it leaks a handle to a source that will be closed (for [Path] /
 * `String` overloads) or unmanaged (for the [Source] overload) once [block]
 * exits, and later iteration may fail with [IOException] or surface garbage.
 * For eager loading use [readAll].
 *
 * @return whatever [block] returns.
 * @throws IOException on terminal operation, when [source] fails to deliver
 *   bytes (read error, premature end of stream, ...).
 * @throws CsvParseFormatException on terminal operation, when the decoded
 *   character stream violates the CSV format.
 * @throws CsvFieldNumDifferentException on terminal operation, when a row's
 *   field count violates the configured row-count behaviour.
 */
fun <T> CsvReader.read(
    source: Source,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = block(read(source.toCharSequence(options.stripBom)))

/**
 * Eagerly read all CSV rows from [source] using a UTF-8 decode.
 *
 * Equivalent to `read(source, options) { it.toList() }`. Use when you want
 * a fully materialised `List<List<String>>` and do not need to short-circuit
 * mid-stream. Resource ownership of [source] stays with the caller — this
 * overload neither opens nor closes it.
 *
 * @throws IOException when [source] fails to deliver bytes.
 * @throws CsvParseFormatException when the decoded character stream violates
 *   the CSV format.
 * @throws CsvFieldNumDifferentException when a row's field count violates the
 *   configured row-count behaviour.
 */
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
 * Read CSV rows from the file at [path] using a UTF-8 decode and pass them to
 * [block]. The underlying source is closed when [block] returns or throws.
 *
 * On JVM, [path] is interpreted via the system filesystem. On JS (Node.js),
 * the underlying `kotlinx-io` `FileSource` loads the entire file into memory
 * via `fs.readFileSync` on the first read; streaming behaviour is therefore
 * JVM-only despite the [Sequence] return type of the inner read.
 *
 * The [Sequence] passed to [block] must be consumed inside the block —
 * returning it from [block] leaks a handle to a now-closed source. For eager
 * loading use [readAll].
 *
 * @return whatever [block] returns.
 * @throws IOException when [path] cannot be opened, or on terminal operation
 *   when the file fails to deliver bytes.
 * @throws CsvParseFormatException on terminal operation, when the file
 *   contents violate the CSV format.
 * @throws CsvFieldNumDifferentException on terminal operation, when a row's
 *   field count violates the configured row-count behaviour.
 */
fun <T> CsvReader.read(
    path: Path,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = SystemFileSystem.source(path).buffered().use { bufferedSource ->
    read(bufferedSource, options, block)
}

/**
 * Eagerly read all CSV rows from the file at [path] using a UTF-8 decode.
 *
 * Equivalent to `read(path, options) { it.toList() }`. The underlying source
 * is opened and closed inside this call, and the returned list is safe to
 * consume after the call returns.
 *
 * @throws IOException when [path] cannot be opened or the file fails to
 *   deliver bytes.
 * @throws CsvParseFormatException when the file contents violate the CSV
 *   format.
 * @throws CsvFieldNumDifferentException when a row's field count violates the
 *   configured row-count behaviour.
 */
fun CsvReader.readAll(
    path: Path,
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = read(path, options) { it.toList() }

/**
 * Convenience overload that builds a [Path] from a string. Lets callers avoid
 * importing `kotlinx.io.files.Path`. Relative paths follow `SystemFileSystem`
 * platform behaviour (typically the current working directory).
 *
 * The same JS in-memory-load caveat as the [Path] overload applies.
 *
 * The [Sequence] passed to [block] must be consumed inside the block —
 * returning it from [block] leaks a handle to a now-closed source. For eager
 * loading use [readAll].
 *
 * @return whatever [block] returns.
 * @throws IOException when [filePath] cannot be opened, or on terminal
 *   operation when the file fails to deliver bytes.
 * @throws CsvParseFormatException on terminal operation, when the file
 *   contents violate the CSV format.
 * @throws CsvFieldNumDifferentException on terminal operation, when a row's
 *   field count violates the configured row-count behaviour.
 */
fun <T> CsvReader.read(
    filePath: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = read(Path(filePath), options, block)

/**
 * Eagerly read all CSV rows from the file at [filePath] using a UTF-8 decode.
 *
 * Equivalent to `read(filePath, options) { it.toList() }`. The underlying
 * source is opened and closed inside this call, and the returned list is
 * safe to consume after the call returns.
 *
 * @throws IOException when [filePath] cannot be opened or the file fails to
 *   deliver bytes.
 * @throws CsvParseFormatException when the file contents violate the CSV
 *   format.
 * @throws CsvFieldNumDifferentException when a row's field count violates the
 *   configured row-count behaviour.
 */
fun CsvReader.readAll(
    filePath: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = read(filePath, options) { it.toList() }
