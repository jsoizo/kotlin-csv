package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.appendRows
import com.jsoizo.kotlincsv.writer.internal.appendNullableRows
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
 * Encode nullable [rows] and write to [file] using [charset]. Null fields are
 * emitted as unquoted empty fields.
 */
fun CsvWriter.writeNullableToFile(
    rows: Sequence<List<String?>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    file.outputStream().use { stream ->
        writeNullable(rows, stream, charset, options)
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

/**
 * Encode nullable [rows] and write to [stream] using [charset]. [stream] is
 * caller-owned and is flushed but not closed by this call.
 */
fun CsvWriter.writeNullable(
    rows: Sequence<List<String?>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    val writer = BufferedWriter(OutputStreamWriter(stream, Charset.forName(charset)))
    if (options.prependBom) {
        writer.write(BOM_STRING)
    }
    appendNullableRows(rows, config, writer)
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

/** @see writeNullableToFile */
fun CsvWriter.writeNullableToFile(
    rows: List<List<String?>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeNullableToFile(rows.asSequence(), file, charset, options)

/** @see writeNullable */
fun CsvWriter.writeNullable(
    rows: List<List<String?>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeNullable(rows.asSequence(), stream, charset, options)
