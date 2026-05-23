package com.jsoizo.kotlincsv.writer

import com.jsoizo.kotlincsv.writer.internal.appendRows
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

private const val BOM_STRING = "\uFEFF"
private const val WRITE_CHUNK_SIZE = 8192

/**
 * Encode [rows] and write to [sink] as UTF-8. [sink] is caller-owned and is
 * flushed but not closed by this call. With [CsvWriteIoOptions.prependBom],
 * a U+FEFF is written before the body.
 */
fun CsvWriter.write(
    rows: Sequence<List<String>>,
    sink: Sink,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    if (options.prependBom) {
        sink.writeString(BOM_STRING)
    }
    val out = SinkAppendable(sink)
    appendRows(rows, config, out)
    out.flush()
    sink.flush()
}

/** @see write */
fun CsvWriter.write(
    rows: List<List<String>>,
    sink: Sink,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = write(rows.asSequence(), sink, options)

/**
 * Encode [rows] and write to the file at [path] as UTF-8. The file is
 * truncated, written, flushed and closed inside this call.
 */
fun CsvWriter.writeToFile(
    rows: Sequence<List<String>>,
    path: Path,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) {
    SystemFileSystem.sink(path).buffered().use { bufferedSink ->
        write(rows, bufferedSink, options)
    }
}

/** @see writeToFile */
fun CsvWriter.writeToFile(
    rows: List<List<String>>,
    path: Path,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows.asSequence(), path, options)

/** String-path overload of [writeToFile]. */
fun CsvWriter.writeToFile(
    rows: Sequence<List<String>>,
    filePath: String,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows, Path(filePath), options)

/** @see writeToFile */
fun CsvWriter.writeToFile(
    rows: List<List<String>>,
    filePath: String,
    options: CsvWriteIoOptions = CsvWriteIoOptions(),
) = writeToFile(rows.asSequence(), filePath, options)

private class SinkAppendable(
    private val sink: Sink,
) : Appendable {
    private val buffer = StringBuilder(WRITE_CHUNK_SIZE)

    override fun append(value: Char): Appendable {
        buffer.append(value)
        flushIfFull()
        return this
    }

    override fun append(value: CharSequence?): Appendable {
        val text = value ?: "null"
        return append(text, 0, text.length)
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        val text = value ?: "null"
        for (index in startIndex until endIndex) {
            buffer.append(text[index])
            flushIfFull()
        }
        return this
    }

    fun flush() {
        if (buffer.isNotEmpty()) {
            sink.writeString(buffer.toString())
            buffer.clear()
        }
    }

    private fun flushIfFull() {
        if (buffer.length >= WRITE_CHUNK_SIZE) {
            flush()
        }
    }
}
