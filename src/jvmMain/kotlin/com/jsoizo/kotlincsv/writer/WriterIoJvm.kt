package com.jsoizo.kotlincsv.writer

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException

private const val BOM_STRING = "\uFEFF"
private const val WRITE_CHUNK_SIZE = 8192

/**
 * Encode [rows] and write the resulting characters to [file] using the given
 * [charset]. The underlying file stream is opened in truncate mode (matching
 * the v1 default), is flushed, and is closed when this function returns or
 * throws.
 *
 * Java charset aliases (e.g. `"SJIS"` for `Shift_JIS`) are resolved through
 * [Charset.forName].
 *
 * The `commonMain` overloads (`write(rows, sink: Sink, ...)` /
 * `write(rows, path: Path, ...)`) emit UTF-8 only; use this JVM overload when
 * a non-UTF-8 charset is required.
 *
 * @throws FileNotFoundException when [file] cannot be opened for writing
 *   (parent directory missing, permission denied, ...).
 * @throws UnsupportedCharsetException when [charset] is not a supported
 *   character set in this JVM.
 * @throws IllegalCharsetNameException when [charset] is not a legal charset
 *   name.
 * @throws IOException when writing or flushing fails during encoding.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    file.outputStream().use { stream ->
        write(rows, stream, charset, options)
    }
}

/**
 * Encode [rows] and write the resulting characters to [stream] using the
 * given [charset].
 *
 * Resource ownership of [stream] stays with the caller; this overload neither
 * closes the stream nor the internally created [OutputStreamWriter]. The
 * writer is flushed at the end of the call so written bytes are visible to
 * downstream consumers without waiting for [stream] to be closed.
 *
 * If [CsvWriteIoOptions.prependBom] is `true`, a single U+FEFF is written
 * before the encoded body. The exact bytes emitted depend on the charset
 * encoder: `"UTF-8"` produces `EF BB BF`; charsets without a BOM concept
 * (e.g. `"Shift_JIS"`, `"ISO-8859-1"`) typically replace U+FEFF with the
 * encoder's unmappable-character substitution (often `?`). Charsets such as
 * `"UTF-16"` already emit their own BOM during encoding, so `prependBom`
 * combined with them produces a double BOM — pick `"UTF-16BE"` / `"UTF-16LE"`
 * when only one BOM is wanted. Callers wanting a BOM only when meaningful
 * are responsible for filtering by charset.
 *
 * The `commonMain` overload `write(rows, sink: Sink, ...)` emits UTF-8 only;
 * use this JVM overload when a non-UTF-8 charset is required.
 *
 * @throws UnsupportedCharsetException when [charset] is not a supported
 *   character set in this JVM.
 * @throws IllegalCharsetNameException when [charset] is not a legal charset
 *   name.
 * @throws IOException when [stream] fails to accept bytes during encoding or
 *   on the final flush.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    val osw = OutputStreamWriter(stream, Charset.forName(charset))
    if (options.prependBom) {
        osw.write(BOM_STRING)
    }
    val buffer = StringBuilder(WRITE_CHUNK_SIZE)
    for (ch in write(rows)) {
        buffer.append(ch)
        if (buffer.length >= WRITE_CHUNK_SIZE) {
            osw.write(buffer.toString())
            buffer.clear()
        }
    }
    if (buffer.isNotEmpty()) {
        osw.write(buffer.toString())
    }
    osw.flush()
}

/**
 * Eager `List` overload that delegates to the [Sequence] file writer.
 *
 * @see write
 */
fun CsvWriter.write(
    rows: List<List<String>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), file, charset, options)

/**
 * Eager `List` overload that delegates to the [Sequence] stream writer.
 *
 * @see write
 */
fun CsvWriter.write(
    rows: List<List<String>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), stream, charset, options)
