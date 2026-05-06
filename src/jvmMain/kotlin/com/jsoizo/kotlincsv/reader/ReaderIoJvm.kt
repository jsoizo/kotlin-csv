package com.jsoizo.kotlincsv.reader

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

private const val BOM_CHAR = '\uFEFF'

/**
 * Read CSV rows from [file] decoded with [charset] and pass them to [block].
 * The file stream is closed when [block] returns or throws. The `Sequence`
 * passed to [block] must be consumed inside the block.
 */
fun <T> CsvReader.readFromFile(
    file: File,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String>>) -> T,
): T = file.inputStream().use { stream ->
    read(stream, charset, options, block)
}

/** Eagerly read all CSV rows from [file] decoded with [charset]. */
fun CsvReader.readAllFromFile(
    file: File,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = readFromFile(file, charset, options) { it.toList() }

/**
 * Read CSV rows from [stream] decoded with [charset] and pass them to
 * [block]. [stream] is caller-owned — this overload does not close it.
 * The `Sequence` passed to [block] must be consumed inside the block.
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

/**
 * Eagerly read all CSV rows from [stream] decoded with [charset].
 * [stream] is caller-owned — this overload does not close it.
 */
fun CsvReader.readAll(
    stream: InputStream,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String>> = read(stream, charset, options) { it.toList() }

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
