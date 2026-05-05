package com.jsoizo.kotlincsv.writer

import kotlinx.io.IOException
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

private const val BOM_STRING = "\uFEFF"
private const val WRITE_CHUNK_SIZE = 8192

/**
 * Encode [rows] and write the resulting characters to [sink] as UTF-8.
 *
 * If [CsvWriteIoOptions.prependBom] is `true`, a single U+FEFF is written
 * before the encoded body so the output stream starts with a BOM-prefixed
 * UTF-8 sequence (`EF BB BF`).
 *
 * Resource ownership of [sink] stays with the caller. The sink is flushed at
 * the end of the call so written bytes are visible to downstream consumers.
 *
 * @throws IOException when [sink] fails to accept bytes during encoding or
 *   on the final flush.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    sink: Sink,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    if (options.prependBom) {
        sink.writeString(BOM_STRING)
    }
    val buffer = StringBuilder(WRITE_CHUNK_SIZE)
    for (ch in write(rows)) {
        buffer.append(ch)
        if (buffer.length >= WRITE_CHUNK_SIZE) {
            sink.writeString(buffer.toString())
            buffer.clear()
        }
    }
    if (buffer.isNotEmpty()) {
        sink.writeString(buffer.toString())
    }
    sink.flush()
}

/**
 * Eager `List` overload that delegates to the [Sequence] sink writer.
 *
 * @see write
 */
fun CsvWriter.write(
    rows: List<List<String>>,
    sink: Sink,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), sink, options)

/**
 * Encode [rows] and write the resulting bytes to the file at [path]. The
 * underlying sink is opened in truncate mode (matching the v1 default), is
 * flushed, and is closed when this function returns or throws.
 *
 * @throws IOException when [path] cannot be opened (parent directory missing,
 *   permission denied, ...) or when writing / flushing fails during encoding.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    path: Path,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    SystemFileSystem.sink(path).buffered().use { bufferedSink ->
        write(rows, bufferedSink, options)
    }
}

/**
 * Eager `List` overload that delegates to the [Sequence] path writer.
 *
 * @see write
 */
fun CsvWriter.write(
    rows: List<List<String>>,
    path: Path,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), path, options)

/**
 * Convenience overload that builds a [Path] from a string. Lets callers avoid
 * importing `kotlinx.io.files.Path`. Relative paths follow `SystemFileSystem`
 * platform behaviour.
 *
 * @throws IOException when [filePath] cannot be opened or when writing /
 *   flushing fails during encoding.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    filePath: String,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows, Path(filePath), options)

/**
 * Eager `List` overload that delegates to the [Sequence] string-path writer.
 *
 * @see write
 */
fun CsvWriter.write(
    rows: List<List<String>>,
    filePath: String,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), filePath, options)
