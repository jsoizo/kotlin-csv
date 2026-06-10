package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.reader.internal.parseRowsFromChunks
import com.jsoizo.kotlincsv.reader.internal.parseRowsWithMetadataFromChunks
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

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

/** Read nullable CSV rows from [file] decoded with [charset]. */
fun <T> CsvReader.readNullableFromFile(
    file: File,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String?>>) -> T,
): T = file.inputStream().use { stream ->
    readNullable(stream, charset, options, block)
}

/** Eagerly read all nullable CSV rows from [file] decoded with [charset]. */
fun CsvReader.readAllNullableFromFile(
    file: File,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String?>> = readNullableFromFile(file, charset, options) { it.toList() }

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
    val parsed = parseRowsFromChunks(reader.asChunkReader(), config.dialect, options.stripBom)
    return block(applyPipeline(parsed))
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

/**
 * Read nullable CSV rows from [stream] decoded with [charset].
 * [stream] is caller-owned and is not closed by this call.
 */
fun <T> CsvReader.readNullable(
    stream: InputStream,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
    block: (Sequence<List<String?>>) -> T,
): T {
    val reader = InputStreamReader(stream, Charset.forName(charset)).buffered()
    val parsed = parseRowsWithMetadataFromChunks(reader.asChunkReader(), config.dialect, options.stripBom)
    return block(applyNullablePipeline(parsed))
}

/** Eagerly read all nullable CSV rows from [stream] decoded with [charset]. */
fun CsvReader.readAllNullable(
    stream: InputStream,
    charset: String = "UTF-8",
    options: CsvReadIoOptions = CsvReadIoOptions(),
): List<List<String?>> = readNullable(stream, charset, options) { it.toList() }

private fun Reader.asChunkReader(): (CharArray) -> Int = { buffer ->
    val charsRead = read(buffer)
    if (charsRead == -1) 0 else charsRead
}
