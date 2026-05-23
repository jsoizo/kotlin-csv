package com.jsoizo.kotlincsv.reader

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

fun <T> CsvReader.readFromFile(
    file: File,
    charset: String = "UTF-8",
    block: (Sequence<List<String>>) -> T,
): T = file.inputStream().use { stream ->
    read(stream, charset, block)
}

fun CsvReader.readAllFromFile(
    file: File,
    charset: String = "UTF-8",
): List<List<String>> = readFromFile(file, charset) { it.toList() }

fun <T> CsvReader.readFromFile(
    filePath: String,
    charset: String = "UTF-8",
    block: (Sequence<List<String>>) -> T,
): T = readFromFile(File(filePath), charset, block)

fun CsvReader.readAllFromFile(
    filePath: String,
    charset: String = "UTF-8",
): List<List<String>> = readFromFile(filePath, charset) { it.toList() }

fun <T> CsvReader.read(
    stream: InputStream,
    charset: String = "UTF-8",
    block: (Sequence<List<String>>) -> T,
): T {
    val reader = InputStreamReader(stream, Charset.forName(charset)).buffered()
    return read(reader, block)
}

fun CsvReader.readAll(
    stream: InputStream,
    charset: String = "UTF-8",
): List<List<String>> = read(stream, charset) { it.toList() }

private fun <T> CsvReader.read(
    reader: Reader,
    block: (Sequence<List<String>>) -> T,
): T = block(read(reader.asCharSequence()))

private fun Reader.asCharSequence(): Sequence<Char> = sequence {
    val buffer = CharArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        for (index in 0 until read) {
            yield(buffer[index])
        }
    }
}
