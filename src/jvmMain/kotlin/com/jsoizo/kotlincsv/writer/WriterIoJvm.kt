package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.appendRows
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

private const val BOM_STRING = "\uFEFF"

/**
 * Encode [rows] and write to [file] using [charset]. The file is truncated,
 * written, flushed and closed inside this call.
 */
fun CsvWriter.writeToFile(
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
 * Encode [rows] and write to [stream] using [charset]. [stream] is
 * caller-owned and is flushed but not closed by this call.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    val writer = BufferedWriter(OutputStreamWriter(stream, Charset.forName(charset)))
    if (options.prependBom) {
        writer.write(BOM_STRING)
    }
    appendRows(rows, config, writer)
    writer.flush()
}

/** @see writeToFile */
fun CsvWriter.writeToFile(
    rows: List<List<String>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows.asSequence(), file, charset, options)

/** @see write */
fun CsvWriter.write(
    rows: List<List<String>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), stream, charset, options)
