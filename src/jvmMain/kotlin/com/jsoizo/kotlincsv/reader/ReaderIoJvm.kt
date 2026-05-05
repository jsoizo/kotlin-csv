package com.jsoizo.kotlincsv.reader

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

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
