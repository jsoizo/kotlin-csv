package com.jsoizo.kotlincsv.reader

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

private const val BOM_CHAR = '\uFEFF'

/**
 * Read CSV rows from [source] using a UTF-8 decode and pass them to [block].
 *
 * The [source] is consumed fully — its bytes are decoded as UTF-8 into a string
 * before parsing begins. If [CsvReadIoOptions.stripBom] is `true` and the
 * decoded text starts with U+FEFF (BOM), that one character is dropped before
 * parsing.
 *
 * Resource ownership of [source] stays with the caller.
 *
 * @return whatever [block] returns.
 */
fun <T> CsvReader.read(
    source: Source,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T {
    val raw = source.readString()
    val text = if (options.stripBom && raw.startsWith(BOM_CHAR)) raw.substring(1) else raw
    return block(read(text.asSequence()))
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
    path: String,
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = read(Path(path), options, block)
