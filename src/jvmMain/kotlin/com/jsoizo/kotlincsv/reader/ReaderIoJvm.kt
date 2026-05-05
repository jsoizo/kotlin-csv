package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException
import com.jsoizo.kotlincsv.exceptions.CsvParseFormatException
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException

private const val BOM_CHAR = '\uFEFF'

/**
 * Read CSV rows from [file] using the given [charset] and pass them to [block].
 * The underlying file stream is opened, decoded one Java `Char` at a time
 * through a lazy `Sequence<Char>`, and closed when [block] returns or throws.
 *
 * Java charset aliases (e.g. `"SJIS"` for `Shift_JIS`) are resolved through
 * [Charset.forName].
 *
 * The `commonMain` overloads (`read(source: Source, ...)` /
 * `read(path: Path, ...)`) decode UTF-8 only; use this JVM overload when a
 * non-UTF-8 charset is required.
 *
 * @return whatever [block] returns.
 * @throws FileNotFoundException at call time, when [file] does not exist or
 *   cannot be opened for reading.
 * @throws UnsupportedCharsetException at call time, when [charset] is not a
 *   supported character set in this JVM.
 * @throws IllegalCharsetNameException at call time, when [charset] is not a
 *   legal charset name.
 * @throws IOException on terminal operation, when the file fails to deliver
 *   bytes during decoding.
 * @throws CsvParseFormatException on terminal operation, when the file
 *   contents violate the CSV format.
 * @throws CsvFieldNumDifferentException on terminal operation, when a row's
 *   field count violates the configured row-count behaviour.
 */
fun <T> CsvReader.read(
    file: File,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = file.inputStream().use { stream ->
    read(stream, charset, options, block)
}

/**
 * Read CSV rows from [stream] using the given [charset] and pass them to
 * [block].
 *
 * Resource ownership of [stream] stays with the caller; this overload neither
 * closes the stream nor the internally created [InputStreamReader] /
 * [BufferedReader]. Java charset aliases (e.g. `"SJIS"` for `Shift_JIS`) are
 * resolved through [Charset.forName].
 *
 * If [CsvReadIoOptions.stripBom] is `true` and the very first decoded `Char`
 * is U+FEFF, it is dropped. The match runs after charset decoding, so it
 * works for UTF-8, UTF-16, and any other encoding that surfaces the BOM as
 * U+FEFF in the decoded stream.
 *
 * The `commonMain` overload `read(source: Source, ...)` is UTF-8 only; use
 * this JVM overload when a non-UTF-8 charset is required.
 *
 * @return whatever [block] returns.
 * @throws UnsupportedCharsetException at call time, when [charset] is not a
 *   supported character set in this JVM.
 * @throws IllegalCharsetNameException at call time, when [charset] is not a
 *   legal charset name.
 * @throws IOException on terminal operation, when [stream] fails to deliver
 *   bytes during decoding.
 * @throws CsvParseFormatException on terminal operation, when the decoded
 *   character stream violates the CSV format.
 * @throws CsvFieldNumDifferentException on terminal operation, when a row's
 *   field count violates the configured row-count behaviour.
 */
fun <T> CsvReader.read(
    stream: InputStream,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T {
    val reader = InputStreamReader(stream, Charset.forName(charset)).buffered()
    return block(read(reader.toCharSequence(options.stripBom)))
}

private fun BufferedReader.toCharSequence(stripBom: Boolean): Sequence<Char> = sequence {
    var first = true
    while (true) {
        val ch = read()
        if (ch == -1) break
        val c = ch.toChar()
        if (first) {
            first = false
            if (stripBom && c == BOM_CHAR) continue
        }
        yield(c)
    }
}
