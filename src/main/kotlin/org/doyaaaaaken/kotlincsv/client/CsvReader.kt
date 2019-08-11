package org.doyaaaaaken.kotlincsv.client

import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import org.doyaaaaaken.kotlincsv.dsl.context.ICsvReaderContext
import org.doyaaaaaken.kotlincsv.parser.CsvParser
import java.io.BufferedReader
import java.io.File
import kotlin.streams.asSequence

class CsvReader(ctx: CsvReaderContext = CsvReaderContext()) : ICsvReaderContext by ctx {

    private val parser = CsvParser()

    fun read(data: String): List<List<String>> {
        return readAsSequence(data).toList()
    }

    fun read(file: File): List<List<String>> {
        return readAsSequence(file).toList()
    }

    fun readAsSequence(data: String): Sequence<List<String>> {
        val br = data.byteInputStream(charset).bufferedReader(charset)
        return readWithBufferedReader(br)
    }

    fun readAsSequence(file: File): Sequence<List<String>> {
        val br = file.inputStream().bufferedReader(charset)
        return readWithBufferedReader(br)
    }

    private fun readWithBufferedReader(br: BufferedReader): Sequence<List<String>> {
        var leftOver = ""
        return br.lines().asSequence().mapNotNull { line ->
            val parsedLine = parser.parseLine("${leftOver}${line}")
            if (parsedLine == null) {
                leftOver = "${leftOver}${line}"
            }
            parsedLine
        }
    }
}
