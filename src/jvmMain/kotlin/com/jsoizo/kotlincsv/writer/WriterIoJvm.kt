package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.appendRows
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

private const val BOM_STRING = "\uFEFF"

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

fun CsvWriter.writeToFile(
    rows: List<List<String>>,
    file: File,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows.asSequence(), file, charset, options)

fun CsvWriter.writeToFile(
    rows: Sequence<List<String>>,
    filePath: String,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows, File(filePath), charset, options)

fun CsvWriter.writeToFile(
    rows: List<List<String>>,
    filePath: String,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows.asSequence(), filePath, charset, options)

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

fun CsvWriter.write(
    rows: List<List<String>>,
    stream: OutputStream,
    charset: String = "UTF-8",
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), stream, charset, options)
